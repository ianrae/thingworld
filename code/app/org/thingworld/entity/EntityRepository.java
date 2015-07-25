package org.thingworld.entity;

import java.util.List;

import org.thingworld.ICommitObserver;
import org.thingworld.MContext;
import org.thingworld.log.Logger;
import org.thingworld.persistence.Commit;
import org.thingworld.persistence.IStreamDAO;
import org.thingworld.persistence.Stream;
import org.thingworld.util.SfxTrail;


public class EntityRepository implements ICommitObserver
{
//	Map<Long, Entity> map = new ConcurrentHashMap<>(); //!!needs to be thread-safe
//	Map<Long, Long> whenMap = new ConcurrentHashMap<>(); 
	EntityCache cache;
//	private IStreamDAO streamDAO;
	private EntityManagerRegistry registry;
	private long numHits;
	private long numMisses;
	private SfxTrail trail = new SfxTrail();

	public EntityRepository(IStreamDAO streamDAO, EntityManagerRegistry registry)
	{
//		this.streamDAO = streamDAO;
		this.registry = registry;
		cache = new EntityCache();
		cache.init();
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
		ESpec spec = cache.getSpec(entityId);
//		Entity obj = (spec != null) ? spec.entity : null;
		Long startId = null;
		long epoch = 0L;
		if (spec != null)
		{
//			long when = whenMap.get(entityId);
			long when = spec.when;
			epoch = oloader.getMaxId(); //epoch
			if(when >= epoch)
			{
				Logger.logDebug("ER(%d) hit!", entityId);
				numHits++;
				return spec.entity;
			}
			startId = when + 1L;
			//Logger.logDebug("ER(%d) stale!", entityId);
		}
		
		if (spec == null)
		{
			spec = new ESpec();
		}

		Entity obj = doLoadEntity(type, entityId, oloader, startId, epoch, spec);
		return obj;
	}
	private Entity doLoadEntity(String type, Long entityId, EntityLoader oloader, Long startId, long epoch, ESpec spec) throws Exception
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
//			whenMap.put(entityId, epoch); //we're up to date wrt to epoch
			spec.when = epoch;
			cache.put(entityId, spec);
			Logger.logDebug("ER(%d) hitd! epoch=%d", entityId, epoch);
			numHits++;
			return spec.entity;
		}
		numMisses++;
		Logger.logDebug("ER(%d) miss startId=%d", entityId, startId);
		
		
		IEntityMgr mgr = registry.findByType(type);

		for(Commit commit : L)
		{
			try {
				spec.entity = doObserve(entityId, commit, mgr, spec);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return spec.entity;
	}

	public synchronized long getSize() 
	{
		return cache.size();
	}

	@Override
	public synchronized boolean willAccept(Stream stream, Commit commit) 
	{
		if (stream == null)
		{
			return false;
		}
		return cache.containsKey(stream.getId()); //only care about entity we have already in cache
	}

	@Override
	public synchronized void observe(MContext mtx, Stream stream, Commit commit) 
	{
		Long entityId = stream.getId();
		ESpec spec = cache.getSpec(entityId);
//		Entity obj = (spec != null) ? spec.entity : null;

		IEntityMgr mgr = registry.findByType(stream.getType());
		try {
			Entity obj = doObserve(entityId, commit, mgr, spec);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private Entity doObserve(Long entityId, Commit commit, IEntityMgr mgr, ESpec spec) throws Exception
	{
		this.trail.add(commit.getId().toString()); //remove later!!

		switch(commit.getAction())
		{
		case 'I':
		case 'S':
			spec.entity = mgr.rehydrate(commit.getJson());
			if (spec.entity != null)
			{
				spec.entity.setId(entityId);
				cache.put(entityId, spec);
			}
			break;
		case 'U':
			mgr.mergeHydrate(spec.entity, commit.getJson());
			cache.put(entityId, spec); //update object in cache
			break;
		case 'D':
			spec.entity = null;
			break;
		default:
			break;
		}

		if (spec.entity != null)
		{
			spec.entity.clearSetList();
			long current = commit.getId();
//			whenMap.put(entityId, current); 
			spec.when = current;
			cache.put(entityId, spec);
			Logger.logDebug("ER(%d) when=%d", entityId, current);
		}
		return spec.entity;
	}

	public synchronized Entity getIfLoaded(Long entityId) 
	{
		ESpec spec = cache.getSpec(entityId);
		Entity obj = (spec != null) ? spec.entity : null;
		return obj;
	}
}