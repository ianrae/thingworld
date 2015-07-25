package org.thingworld.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

//use Google Guava caches to implement an LRU cache of segments

public class EntityCache 
{
	//	private Map<Long, List<T>> segmentMap = new HashMap<>();
	private Cache<Long, ESpec> gcache;

	public void init()
	{
		gcache = CacheBuilder.newBuilder()
				.maximumSize(1000)
				.build(); // look Ma, no CacheLoader	
	}

	public void put(Long entityId, ESpec spec)
	{
		gcache.put(entityId, spec);
	}
	public ESpec getSpec(Long entityId)
	{
		ESpec spec = gcache.asMap().get(entityId);
		return spec;
	}
	public boolean containsKey(Long entityId)
	{
		ESpec spec = gcache.asMap().get(entityId);
		return (spec != null);
	}
	
	public long size()
	{
		return gcache.size();
	}

}