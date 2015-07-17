package org.thingworld.errortracker;

import org.thingworld.log.DefaultLogger;
import org.thingworld.log.ILogger;
import org.thingworld.log.Logger;

public class DefaultErrorTracker implements IErrorTracker
{
	private int errorCount;
	private String lastError;
	private IErrorListener listener;
	private ILogger logger;
	
	public DefaultErrorTracker()
	{
		this.logger = new DefaultLogger();
	}

	@Override
	public synchronized void setListener(IErrorListener listener) 
	{
		this.listener = listener;
	}

	@Override
	public synchronized void errorOccurred(String errMsg) 
	{
		lastError = errMsg;
		errorCount++;
		Logger.log("ERROR: " + errMsg);
		if (listener != null)
		{
			listener.onError(errMsg);
		}			
	}

	@Override
	public int getErrorCount() 
	{
		return errorCount;
	}

	@Override
	public synchronized String getLastError() 
	{
		return this.lastError;
	}

	@Override
	public void setLogger(ILogger logger) 
	{
		this.logger = logger;
	}
	
}