package org.thingworld.readmodel;

import org.thingworld.ICommitObserver;
import org.thingworld.MContext;

public interface IReadModel {

	void setLastCommitId(long id);
	void setLastEventId(long id);
	void freshen(MContext mtx, ICommitObserver extraObserver);
}
