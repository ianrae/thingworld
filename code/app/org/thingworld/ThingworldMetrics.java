package org.thingworld;

import java.util.concurrent.atomic.AtomicLong;

public class ThingworldMetrics 
{
	static private AtomicLong numFailedPresenterMethodCalls = new AtomicLong(0L);
	
	public static void incFailedPresenterMethodCalls()
	{
		numFailedPresenterMethodCalls.incrementAndGet();
	}
	
}
