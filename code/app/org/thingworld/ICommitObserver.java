package org.thingworld;

import mesf.persistence.Commit;
import mesf.persistence.Stream;


public interface ICommitObserver
{
	boolean willAccept(Stream stream, Commit commit);
	void observe(MContext mtx, Stream stream, Commit commit);
}