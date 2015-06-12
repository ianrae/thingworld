package org.thingworld;

import org.thingworld.persistence.Commit;
import org.thingworld.persistence.Stream;


public interface ICommitObserver
{
	boolean willAccept(Stream stream, Commit commit);
	void observe(MContext mtx, Stream stream, Commit commit);
}