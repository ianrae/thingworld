package org.thingworld.cache;

import java.util.List;

import org.thingworld.config.Config;
import org.thingworld.config.IConfig;
import org.thingworld.log.Logger;
import org.thingworld.persistence.EventRecord;
import org.thingworld.persistence.IEventRecordDAO;

//thread-safe long running cache of commit DTOs
public class EventCache
{
	class EventLoader implements ISegCacheLoader<EventRecord>
	{
		public EventLoader() 
		{
		}

		@Override
		public List<EventRecord> loadRange(long startIndex, long n) 
		{
			//there is no el[0] so shift down
			//0,4 means load records 1..4
			List<EventRecord> L = dao.loadRange(startIndex, n);
			Logger.logDebug("LD %d.%d (got %d)", startIndex,n, L.size());
			return L;
		}
		
	}
	
	ISegmentedCache<EventRecord> segcache;
	private IEventRecordDAO dao;
	
	public EventCache(IEventRecordDAO dao)
	{
		this.dao = dao;
		int n = Config.getIntValue(IConfig.ConfigItem.EVENT_CACHE_SEGMENT_SIZE);
		segcache = new SegmentedGuavaCache<EventRecord>();
		segcache.init(n, new EventLoader());
	}
	
	public synchronized List<EventRecord> loadRange(long startIndex, long n) 
	{
		List<EventRecord> L = segcache.getRange(startIndex, n);
		return L;
	}
	
	public synchronized void clearLastSegment(long maxId)
	{
		segcache.clearLastSegment(maxId);
	}
}