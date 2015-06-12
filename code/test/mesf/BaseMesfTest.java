package mesf;

import org.thingworld.log.LogLevel;
import org.thingworld.log.Logger;

import mef.framework.helpers.BaseTest;


public class BaseMesfTest extends BaseTest
{
	@Override
	public void init()
	{
		super.init();
		Logger.getLogger().setLevel(LogLevel.DEBUG);
	}

}
