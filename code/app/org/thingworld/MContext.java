package org.thingworld;

import org.thingworld.cache.CommitCache;
import org.thingworld.cache.EventCache;
import org.thingworld.cache.StreamCache;
import org.thingworld.cmd.CommandProcessor;
import org.thingworld.cmd.ProcRegistry;
import org.thingworld.entity.Entity;
import org.thingworld.entity.EntityHydrater;
import org.thingworld.entity.EntityLoader;
import org.thingworld.entity.EntityManagerRegistry;
import org.thingworld.entity.EntityRepository;
import org.thingworld.event.EventManagerRegistry;
import org.thingworld.event.IEventBus;
import org.thingworld.persistence.IEventRecordDAO;
import org.thingworld.persistence.PersistenceContext;
import org.thingworld.readmodel.IReadModel;
import org.thingworld.readmodel.ReadModelLoader;
import org.thingworld.readmodel.ReadModelRepository;

public class MContext 
{
	protected CommitMgr commitMgr;
	protected EntityRepository objcache;
	protected EntityHydrater hydrater;
	protected EntityManagerRegistry registry;
	protected EntityLoader oloader;

	private ReadModelRepository readmodelMgr;
	private ReadModelLoader vloader;
	private ProcRegistry procRegistry;
	private CommitCache commitCache;
	private StreamCache strcache;
	private PersistenceContext persistenceCtx;
	private EventManagerRegistry evReg;
	private EventCache eventCache;
	private long maxEventId;
	private IEventBus eventBus;
	
	public MContext(CommitMgr commitMgr, EntityManagerRegistry registry, EventManagerRegistry evReg, EntityRepository objcache, 
			ReadModelRepository readmodelMgr, ReadModelLoader vloader, CommitCache commitCache, StreamCache strcache, EventCache eventCache, 
			PersistenceContext persistenceCtx, IEventBus eventBus)
	{
		this.commitMgr = commitMgr;
		this.registry = registry;
		this.evReg = evReg;
		this.objcache = objcache;
		this.hydrater = new EntityHydrater(objcache);
		this.oloader = commitMgr.createEntityLoader();
		this.readmodelMgr = readmodelMgr;
		this.vloader = vloader;
		this.commitCache = commitCache;
		this.strcache = strcache;
		this.eventCache = eventCache;
		this.persistenceCtx = persistenceCtx;
		this.eventBus = eventBus;
	}
	
	public IEventRecordDAO getEventDAO()
	{
		return this.persistenceCtx.getEventDAO();
	}
	public PersistenceContext getPersistenceContext()
	{
		return this.persistenceCtx;
	}
	
	//is optional
	public void setProcRegistry(ProcRegistry procRegistry)
	{
		this.procRegistry = procRegistry;
	}
	public ProcRegistry getProcRegistry()
	{
		return procRegistry;
	}

	public CommitMgr getCommitMgr() {
		return commitMgr;
	}

	public EntityRepository getObjcache() {
		return objcache;
	}

	public EntityHydrater getHydrater() {
		return hydrater;
	}

	public EntityManagerRegistry getRegistry() {
		return registry;
	}
	public EventManagerRegistry getEventRegistry() {
		return evReg;
	}

	public EntityLoader getOloader() {
		return oloader;
	}

	public ReadModelRepository getReadmodelMgr() {
		return readmodelMgr;
	}

	public ReadModelLoader getVloader() {
		return vloader;
	}
	
	public Entity loadEntitySafe(Class clazz, long entityId) 
	{
		Entity entity = null;
		try {
			entity = this.loadEntity(clazz, entityId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entity;
	}
	public Entity loadEntity(Class clazz, long entityId) throws Exception 
	{
		String type = this.getRegistry().findTypeForClass(clazz);
		Entity obj = this.getHydrater().loadEntity(type, entityId, this.getOloader());
		return obj;
	}
	
	public CommandProcessor findProc(Class clazz)
	{
		return getProcRegistry().find(clazz, this);
	}

	public long getMaxId() 
	{
		return commitMgr.getMaxId();
	}

	public Projector createProjector() 
	{
		return new Projector(commitCache, strcache);
	}
	
	public IReadModel acquire(Class clazz)
	{
		return readmodelMgr.acquire(this, clazz);
	}

	public EventProjector createEventProjector() 
	{
		return new EventProjector(eventCache);
	}

	public long getEventMaxId() 
	{
		if (maxEventId != 0L)
		{
			return maxEventId;
		}
		
		IEventRecordDAO evdao = this.persistenceCtx.getEventDAO();
		maxEventId = evdao.findMaxId();
		return maxEventId;
	}

	public IEventBus getEventBus() {
		return eventBus;
	}
	
}
