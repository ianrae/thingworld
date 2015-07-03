package integration;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import play.Application;
import play.Configuration;
import play.test.*;

public class ConfigurationTests 
{


	@Test
	public void test()
	{
		//Note. application.conf is not read here. Only config is what we pass in
		java.util.Map<String,String> additionalConfiguration = new HashMap<>();
		String val = "value1";
		additionalConfiguration.put("ebean.default", val);
		
		Application fakeApp = Helpers.fakeApplication(additionalConfiguration);
		Configuration config = fakeApp.configuration();
		String s = config.getString("ebean.default");
		assertEquals("value1", s);
	}

}