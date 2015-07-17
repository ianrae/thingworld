package org.thingworld.errortracker;

import org.thingworld.log.ILogger;

public interface IErrorTracker
{
	void setListener(IErrorListener listener);
	void setLogger(ILogger logger);
	public void errorOccurred(String errMsg);
	public int getErrorCount();
	public String getLastError();
}