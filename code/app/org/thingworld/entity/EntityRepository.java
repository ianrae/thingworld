package org.thingworld.entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.thingworld.ICommitObserver;
import org.thingworld.MContext;
import org.thingworld.log.Logger;
import org.thingworld.persistence.Commit;
import org.thingworld.persistence.IStreamDAO;
import org.thingworld.persistence.Stream;
import org.thingworld.util.SfxTrail;


public class EntityRepository implements ICommitObserver
{
	Map<Long, Entity> map = new ConcurrentHashMap<>(); //!!needs to be thread-safe
	Map<Long, Long> whenMap = new ConcurrentHashMap<>(); 
	private IStreamDAO streamDAO;
	private EntityManagerRegistry registry;
	private long numHits;
	private long numMisses;
	private SfxTrail trail = new SfxTrail();

	public EntityRepository(IStreamDAO streamDAO, EntityManagerRegistry registry)
	{
		this.streamDAO = streamDAO;
		this.registry = registry;
	}

	public synchronized String dumpStats()
	{
		String s = String.format("OVC: hits:%d, misses:%d", numHits, numMisses);
		Logger.log(s);
		Logger.log(trail.getTrail());
		return s;
	}

	public synchronized Entity loadEntity(String type, Long entityId, EntityLoader oloader) throws Exception
	{
		Entity obj = map.get(entityId);
		Long startId = null;
		long epoch = 0L;
		if (obj != null)
		{
			long when = whenMap.get(entityId);
			epoch = oloader.getMaxId(); //epoch
			if(when >= epoch)
			{
				Logger.logDebug("ER(%d) hit!", entityId);
				numHits++;
				return obj;
			}
			startId = when + 1L;
			//Logger.logDebug("ER(%d) stale!", entityId);
		}

		obj = doLoadEntity(type, entityId, oloader, startId, obj, epoch);
		return obj;
	}
	private Entity doLoadEntity(String type, Long entityId, EntityLoader oloader, Long startId, Entity obj, long epoch) throws Exception
	{
		//we can't use commit cache here because we are searching for whole stream of commits. !!later could use commit cache with
		List<Commit> L = null;
		if (startId == null)
		{
			L = oloader.loadStream(type, entityId);
		}
		else 
		{
			L = oloader.loadPartialStream(entityId, startId);
		}
		
		if (L == null)
		{
			return null;
		}

		//if no changes have occurred then update the when map because obj is up-to-date
		if (L.size() == 0)
		{
			whenMap.put(entityId, epoch); //we're up to date wrt to epoch
			Logger.logDebug("ER(%d) hitd! epoch=%d", entityId, epoch);
			numHits++;
			return obj;
		}
		numMisses++;
		Logger.logDebug("ER(%d) miss startId=%d", entityId, startId);
		
		
		IEntityMgr mgr = registry.findByType(type);

		for(Commit commit : L)
		{
			try {
				obj = doObserve(entityId, commit, mgr, obj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return obj;
	}

	public synchronized Object getSize() 
	{
		return map.size();
	}

	@Override
	public synchronized boolean willAccept(Stream stream, Commit commit) 
	{
		if (stream == null)
		{
			return false;
		}
		return map.containsKey(stream.getId()); //only care about entity we have already in cache
	}

	@Override
	public synchronized void observe(MContext mtx, Stream stream, Commit commit) 
	{
		Long entityId = stream.getId();
		Entity obj = map.get(entityId);

		IEntityMgr mgr = registry.findByType(stream.getType());
		try {
			obj = doObserve(entityId, commit, mgr, obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private Entity doObserve(Long entityId, Commit commit, IEntityMgr mgr, Entity obj) throws Exception
	{
		this.trail.add(commit.getId().toString()); //remove later!!

		switch(commit.getAction())
		{
		case 'I':
		case 'S':
			obj = mgr.rehydrate(commit.getJson());
			if (obj != null)
			{
				obj.setId(entityId);
				map.put(entityId, obj);
			}
			break;
		case 'U':
			mgr.mergeHydrate(obj, commit.getJson());
			map.put(entityId, obj); //update object in cache
			break;
		case 'D':
			obj = null;
			break;
		default:
			break;
		}

		if (obj != null)
		{
			obj.clearSetList();
			long current = commit.getId();
			whenMap.put(entityId, current); 
		}
		return obj;
	}

	public synchronized Entity getIfLoaded(Long entityId) 
	{
		Entity obj = map.get(entityId);
		return obj;
	}
}