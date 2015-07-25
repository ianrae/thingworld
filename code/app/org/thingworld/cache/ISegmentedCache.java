package org.thingworld.cache;

import java.util.List;

import org.thingworld.persistence.HasId;

public interface ISegmentedCache<T extends HasId> 
{
	void init(long segSize, ISegCacheLoader<T> loader);
	public void putList(long startIndex, List<T> L);
	public void clearLastSegment(long maxId);
	public T getOne(long index);
	public List<T> getRange(long startIndex, long n);
}
