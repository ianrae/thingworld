package org.thingworld.readmodel;

import org.thingworld.ICommitObserver;
import org.thingworld.IEventObserver;
import org.thingworld.MContext;
import org.thingworld.event.Event;
import org.thingworld.persistence.Commit;
import org.thingworld.persistence.EventRecord;
import org.thingworld.persistence.Stream;

public abstract class ReadModel implements ICommitObserver, IEventObserver, IReadModel
{
	public long lastCommitId;
	public long lastEventId;
	public Object obj;
	
	public ReadModel()
	{
	}
	
	@Override
	public boolean willAccept(Stream stream, Commit commit) 
	{
		return false;
	}

	@Override
	public void observe(MContext mtx, Stream stream, Commit commit) 
	{
	}

	@Override
	public void setLastCommitId(long id) 
	{
		this.lastCommitId = id;
	}
	
	@Override
	public void setLastEventId(long id) 
	{
		this.lastEventId = id;
	}
	
	@Override
	public boolean willAcceptEvent(Event event) 
	{
		return false;
	}

	@Override
	public void observeEvent(Event event) 
	{
	}
	
	
	@Override
	public abstract void freshen(MContext mtx);
}