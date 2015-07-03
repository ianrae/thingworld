package org.thingworld.config;

import org.thingworld.config.IConfig.ConfigItem;
import org.thingworld.log.Logger;


public class Config
{
	private static IConfig theSingleton;
	
	public static void setConfig(IConfig log)
	{
		theSingleton = log;
	}
	public static IConfig getConfig()
	{
		initIfNeeded();
		return theSingleton;
	}
	private synchronized static void initIfNeeded() 
	{
		if (theSingleton == null)
		{
			theSingleton = new DefaultConfig();
		}
	}
	
	
	public static int getIntValue(ConfigItem item) 
	{
		initIfNeeded();
		int n = theSingleton.getIntValue(item);
		Logger.logDebug("Config.%s: %d", item.name(), n);
		return n;
	}
	public static boolean getBoolValue(ConfigItem item) 
	{
		initIfNeeded();
		boolean b = theSingleton.getBoolValue(item);
		Logger.logDebug("bConfig.%s: %b", item.name(), b);
		return b;
	}
}