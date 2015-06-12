package org.thingworld;

import org.thingworld.event.Event;
import org.thingworld.persistence.EventRecord;
import org.thingworld.persistence.Stream;


public interface IEventObserver
{
	boolean willAcceptEvent(Event event);
	void observeEvent(Event event);
}