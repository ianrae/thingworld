package org.thingworld.config;

public interface IConfig {

	public enum ConfigItem
	{
		//--int values
		COMMIT_CACHE_SEGMENT_SIZE,
		EVENT_CACHE_SEGMENT_SIZE,
		STREAM_CACHE_SEGMENT_SIZE,

		//--bool values
		CLONE_ENTITY_WHEN_HYDRATE,
		APP_PLATFORM_IS_LINUX,
		
		//--string values
		DEPLOY_MODE
	};
	
	//STEPS when adding new config value
	//-add to enum above
	//-add to dev.conf, prod.conf, etc
	
	
	int getIntValue(ConfigItem item);
	boolean getBoolValue(ConfigItem item);
	String getStringValue(ConfigItem item);
}
