package org.thingworld.config;

public interface IConfig {

	public enum ConfigItem
	{
		//--int values
		COMMIT_CACHE_SEGMENT_SIZE,
		EVENT_CACHE_SEGMENT_SIZE,
		STREAM_CACHE_SEGMENT_SIZE,

		//--bool values
		CLONE_ENTITY_WHEN_HYDRATE
	};
	
	
	int getIntValue(ConfigItem item);
	boolean getBoolValue(ConfigItem item);
}
