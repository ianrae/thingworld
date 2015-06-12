package tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.thingworld.errortracker.DefaultErrorTracker;
import org.thingworld.errortracker.ErrorTracker;

public class ErrorTrackerTests extends BaseMesfTest
{
	@Test
	public void test() 
	{
		DefaultErrorTracker tracker = new DefaultErrorTracker();
		assertEquals(0, tracker.getErrorCount());
		
		tracker.errorOccurred("oops");
		assertEquals(1, tracker.getErrorCount());
		tracker.errorOccurred("oops");
		assertEquals(2, tracker.getErrorCount());
	}

	@Test
	public void test2() 
	{
		assertEquals(0, ErrorTracker.getErrorCount());
		
		ErrorTracker.errorOccurred("oops");
		assertEquals(1, ErrorTracker.getErrorCount());
		ErrorTracker.errorOccurred("oops");
		assertEquals(2, ErrorTracker.getErrorCount());
	}
}
