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
		return getIntValue(item, true);
	}
	public static int getIntValue(ConfigItem item, boolean logIt) 
	{
		initIfNeeded();
		int n = theSingleton.getIntValue(item);
		if (logIt)
		{
			Logger.logDebug("Config.%s = %d", item.name(), n);
		}
		return n;
	}
	
	public static boolean getBoolValue(ConfigItem item)
	{
		return getBoolValue(item, true);
	}
	public static boolean getBoolValue(ConfigItem item, boolean logIt) 
	{
		initIfNeeded();
		boolean b = theSingleton.getBoolValue(item);
		if (logIt)
		{
			Logger.logDebug("Config.%s = %b", item.name(), b);
		}
		return b;
	}
	
	public static String getStringValue(ConfigItem item) 
	{
		return getStringValue(item, true);
	}
	public static String getStringValue(ConfigItem item, boolean logIt) 
	{
		initIfNeeded();
		String s = theSingleton.getStringValue(item);
		if (logIt)
		{
			Logger.logDebug("Config.%s = '%s'", item.name(), s);
		}
		return s;
	}
}