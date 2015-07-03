import static org.junit.Assert.*;

import org.junit.Test;
import org.thingworld.config.Config;
import org.thingworld.config.IConfig;
import org.thingworld.config.IConfig.ConfigItem;


public class ConfigTests {

	@Test
	public void test() 
	{
		int n = Config.getIntValue(IConfig.ConfigItem.COMMIT_CACHE_SEGMENT_SIZE);
		assertEquals(4, n);
		
		assertEquals(true, Config.getBoolValue(ConfigItem.CLONE_ENTITY_WHEN_HYDRATE));
	}

}
