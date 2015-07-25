package org.thingworld.cache;

import java.util.List;

import org.thingworld.config.Config;
import org.thingworld.config.IConfig;
import org.thingworld.log.Logger;
import org.thingworld.persistence.IStreamDAO;
import org.thingworld.persistence.Stream;

//thread-safe long running cache of commit DTOs
public class StreamCache
{
	class StreamTableLoader implements ISegCacheLoader<Stream>
	{
		public StreamTableLoader() 
		{
		}

		@Override
		public List<Stream> loadRange(long startId, long n) 
		{
//			List<Stream> L = dao.loadRange(startId + 1, n);
			List<Stream> L = dao.loadRange(startId, n);
			Logger.logDebug("SLD %d.%d (got %d)", startId,n, L.size());
			return L;
		}
		
	}
	
	ISegmentedCache<Stream> segcache;
	private IStreamDAO dao;
	
	public StreamCache(IStreamDAO dao)
	{
		this.dao = dao;
		int n = Config.getIntValue(IConfig.ConfigItem.STREAM_CACHE_SEGMENT_SIZE);
		segcache = new SegmentedGuavaCache<Stream>();
		segcache.init(n, new StreamTableLoader());
	}
	
	public synchronized long findSnapshotId(long entityId) 
	{
		List<Stream> L = segcache.getRange(entityId, 1);
		if (L == null || L.size() < 1)
		{
			return 0L;
		}
		return L.get(0).getSnapshotId();
	}
	public synchronized Stream findStream(long entityId) 
	{
		List<Stream> L = segcache.getRange(entityId, 1);
		if (L == null || L.size() < 1)
		{
			return null;
		}
		return L.get(0);
	}
	
	public synchronized void clearLastSegment(long maxId)
	{
		segcache.clearLastSegment(maxId);
	}
	
}