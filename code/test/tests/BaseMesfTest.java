package tests;

import org.thingworld.log.LogLevel;
import org.thingworld.log.Logger;

import testhelper.BaseTest;


public class BaseMesfTest extends BaseTest
{
	@Override
	public void init()
	{
		super.init();
		Logger.getLogger().setLevel(LogLevel.DEBUG);
	}

}
