package org.thingworld;

import java.util.ArrayList;
import java.util.List;

import org.thingworld.cache.CommitCache;
import org.thingworld.cache.StreamCache;
import org.thingworld.log.Logger;
import org.thingworld.persistence.Commit;
import org.thingworld.persistence.Stream;
import org.thingworld.readmodel.IReadModel;

public class Projector
{
	private CommitCache cache;
	private StreamCache scache;

	public Projector(CommitCache cache, StreamCache scache)
	{
		this.cache = cache;
		this.scache = scache;
	}
	
	public void run(MContext mtx, ICommitObserver observer, long startId, ICommitObserver extraObserver)
	{
		if (startId > mtx.getMaxId())
		{
			return; //nothing to do
		}
		Logger.log("Projector start %d (max %d)", startId, mtx.getMaxId());
		cache.clearLastSegment(mtx.getMaxId());
		scache.clearLastSegment(mtx.getMaxId());
		List<ICommitObserver> obsL = new ArrayList<>();
		
		//extraObs first because its the EntityRepository, and improves performance if its fresh
		if (extraObserver != null)
		{
			obsL.add(extraObserver);
		}
		obsL.add(observer);
		run(mtx, obsL, startId);
	}
	public void run(MContext mtx, List<ICommitObserver> observerL, long startId)
	{
		List<Commit> L = cache.loadRange(startId, mtx.getMaxId() - startId + 1);
		for(Commit commit : L)	
		{
			doObserve(mtx, commit, observerL);
		}
		
		for(ICommitObserver observer : observerL)
		{
			if (observer instanceof IReadModel)
			{
				IReadModel rm = (IReadModel) observer;
				rm.setLastCommitId(mtx.getMaxId());
			}
		}
	}
	
	private void doObserve(MContext mtx, Commit commit, List<ICommitObserver> observerL)
	{
		Long streamId = commit.getStreamId();
		Stream stream = null;
		if (streamId != null && streamId != 0L)
		{
			stream = scache.findStream(streamId);
		}

		for(ICommitObserver observer : observerL)
		{
			if (observer.willAccept(stream, commit))
			{
				observer.observe(mtx, stream, commit);
			}
		}
	}

}