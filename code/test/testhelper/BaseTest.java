package testhelper;

import static org.junit.Assert.assertEquals;


public class BaseTest
{
	
	//-----------------------------
	public void init()
	{
	}
	
	protected void log(String s)
	{
		//ctx.log(s);
		System.out.println(s);
	}
	
	
	protected void chkLong(Long expected, Long actual)
	{
		assertEquals(expected, actual);
	}
	
}
