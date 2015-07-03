package org.thingworld.config;
import play.Configuration;

public class PlayConfig implements IConfig
{
	private Configuration config;

	public PlayConfig(Configuration config)
	{
		this.config = config;
	}
	@Override
	public int getIntValue(ConfigItem item) 
	{
		String name = makeName(item);
		Integer n = 0;
		
		try {
			String s = config.getString(name);
			n = Integer.parseInt(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return n;
	}

	@Override
	public boolean getBoolValue(ConfigItem item) 
	{
		int val = this.getIntValue(item);
		return (val != 0);
	}
	
	
	private String makeName(ConfigItem item)
	{
		String s = item.name();
		return s;
	}

}
