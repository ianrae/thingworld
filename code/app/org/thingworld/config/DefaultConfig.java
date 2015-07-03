package org.thingworld.config;

import java.util.HashMap;
import java.util.Map;

public class DefaultConfig implements IConfig
{
	private Map<String, Integer> intMap = new HashMap<>();
	
	public DefaultConfig()
	{
		intMap.put(ConfigItem.COMMIT_CACHE_SEGMENT_SIZE.name(), 4);
		intMap.put(ConfigItem.EVENT_CACHE_SEGMENT_SIZE.name(), 4);
		intMap.put(ConfigItem.STREAM_CACHE_SEGMENT_SIZE.name(), 4);
		
		//bools stored here as 0=false,1=true
		intMap.put(ConfigItem.CLONE_ENTITY_WHEN_HYDRATE.name(), 1); //default true
	}

	@Override
	public int getIntValue(ConfigItem item) 
	{
		String key = item.name();
		Integer n = intMap.get(key);
		if (n == null)
		{
			return Integer.MIN_VALUE;
		}
		return n;
	}

	@Override
	public boolean getBoolValue(ConfigItem item) 
	{
		String key = item.name();
		Integer n = intMap.get(key);
		if (n == null)
		{
			return false;
		}
		return (n != 0);
	}
}
