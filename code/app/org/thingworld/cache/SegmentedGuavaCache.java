package org.thingworld.cache;

import java.util.ArrayList;
import java.util.List;

import org.thingworld.log.Logger;
import org.thingworld.persistence.HasId;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

//use Google Guava caches to implement an LRU cache of segments
//Cache of Long,List<T>. cache key is entity id of first entity in L. L is segSize elements, but last one will be shorter.
//Entities don't have contiguous ids (1,2,3,..). May be (1,2,10,23). monotonically increasing.
//Requests to load entities may come in any order so we will define each segment to have a fixed range:
////		long segmentId = (entityId / segSize) * segSize;


public class SegmentedGuavaCache<T extends HasId> implements ISegmentedCache<T>
{
	//a segment holds N entities, whose ids are monotonically increasing
	public static class Range
	{
		private long firstId;
		private long lastId;
		private long segSize;
		
		public Range(long segSize)
		{
			this.segSize = segSize;
		}
		public boolean inRange(long id)
		{
			return (id >= firstId && id <= lastId);
		}
		public boolean willFit(long id)
		{
			long finalId = firstId + segSize - 1;
			return (id >= firstId && id <= finalId);
		}
	}
	
	private Cache<Long, List<T>> segmentMap;
	private long segSize;
	private ISegCacheLoader<T> loader;
	private boolean getOneDiscoveredNoMore; //totally not-thread-safe. fix later!1
	
	@Override
	public void init(long segSize, ISegCacheLoader<T> loader)
	{
		segSize = (segSize <= 0) ? 4 : segSize; //avoid divide by zero error
		
		this.segSize = segSize;
		this.loader = loader;

		segmentMap = CacheBuilder.newBuilder()
				.maximumSize(segSize)
				.build(); // look Ma, no CacheLoader	
	}

	//for unit tests only
	@Override
	public void putList(long startIndex, List<T> L)
	{
		segmentMap.put(new Long(startIndex), L);
	}

	@Override
	public void clearLastSegment(long maxId)
	{
		long max = -1;
		List<T> finalSegmentL = null;
		for(Long segmentId : segmentMap.asMap().keySet())
		{
			if (segmentId > max)
			{
				max = segmentId; //max is entityId of first element in last segment
				finalSegmentL = segmentMap.asMap().get(segmentId);
			}
		}

		//max=4  5,6,7 so n=3
		//maxId=8 (we added one commit)

		if (max >= 0) //found last segment
		{
			long startId = max; //if of L[0] in last segment
			int n = finalSegmentL.size();
			Range range = calcRange(finalSegmentL);
			if (maxId >= startId && ! range.inRange(maxId))
			{
				Logger.logDebug("LAST %d.%d", startId, n);
				//				map.remove(max);

				//calculate # new commits we haven't yet loaded and load them
//				long missing = maxId - (startId + n);
				long numMissing = this.segSize - n; //if segment not full this will be >  0

//				if (missing + n <= this.segSize)
				if (numMissing > 0)
				{
					List<T> newL = loader.loadRange(range.lastId + 1, numMissing); //should get at least one
					finalSegmentL.addAll(newL);
					//not sure if guava returns copy so let's explicitly put it back
					segmentMap.put(new Long(max), finalSegmentL);					
				}
			}
		}
	}
	private Range calcRange(List<T> segmentL) 
	{
		Range range = new Range(segSize);
		if (segmentL.size() > 0)
		{
			range.firstId = segmentL.get(0).getId();
			range.lastId = segmentL.get(segmentL.size() - 1).getId();
		}
		return range;
	}
	
	private Long findSegmentToLiveIn(long entityId)
	{
		List<T> L = null;
		for(Long segmentId : segmentMap.asMap().keySet())
		{
			L = segmentMap.asMap().get(segmentId);
			Range range = calcRange(L);
			
			if (range.willFit(entityId))
			{
				return segmentId;
			}
		}
		return -1L;
	}

	@Override
	public T getOne(long entityId)
	{
		getOneDiscoveredNoMore = false;
//		long seg = (index / segSize) * segSize;
		long seg = findSegmentToLiveIn(entityId);
		if (seg < 0) //not found?
		{
			seg = (entityId / segSize) * segSize; 
		}

		List<T> L = segmentMap.asMap().get(seg);
		if (L == null) //not in cache?
		{
			L = loader.loadRange(seg, segSize);
			if (L != null)
			{
				segmentMap.put(new Long(seg), L);
			}
			else //not in db?
			{
				getOneDiscoveredNoMore = true;
				return null;
			}
		}

		//L is the segment of up to segSize entities
//		long k = index % segSize;
//		if (k >= L.size())
		if (! isInSegment(L, entityId)) //not yet loaded into its segment?
		{
			//for now reload entire seg. later only reload missing ones!!
			List<T> newL = loader.loadRange(seg, segSize);
			if (newL == null)
			{
				Logger.log("UNEXPECTED loadRange FAIL seg %d", seg);
				return null;
			}
			else 
			{
				segmentMap.put(new Long(seg), newL);
			}
			L = newL;
		}
		
		Range range = calcRange(L);
		if (! range.inRange(entityId)) //entity not in segment?
		{
			getOneDiscoveredNoMore = true;
			return null;
		}
		
		for(T entity : L)
		{
			if (entity.getId().longValue() == entityId)
			{
				return entity;
			}
		}
		
		throw new IllegalStateException(String.format("Entity %d not in segment %d (size:%d!)", entityId, seg, segSize));
	}

	private boolean isInSegment(List<T> L, long entityId) 
	{
		for(T entity : L)
		{
			if (entity.getId().longValue() == entityId)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public List<T> getRange(long startIndex, long n)
	{
		List<T> resultL = new ArrayList<>();

//		for(long i = startIndex; i < (startIndex + n); i++)
//		{
//			T val = getOne(i);
//			if (val == null)
//			{
//				return resultL;
//			}
//			resultL.add(val);
//		}
		
		long i = startIndex;
		while(resultL.size() < n)
		{
			T entity = getOne(i);
			if (entity != null)
			{
				resultL.add(entity);
			}
			else if (getOneDiscoveredNoMore)
			{
				break;
			}
			i++;
		}
		
		return resultL;
	}
}