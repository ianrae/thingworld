package org.thingworld.config;

public interface IConfig {

	public enum ConfigItem
	{
		COMMIT_CACHE_SEGMENT_SIZE,
		EVENT_CACHE_SEGMENT_SIZE,
		STREAM_CACHE_SEGMENT_SIZE,

		CLONE_ENTITY_SKIP
	};
	
	
	int getIntValue(ConfigItem item);
}
