package mesf;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.thingworld.CommitMgr;
import org.thingworld.cache.CommitCache;
import org.thingworld.cache.StreamCache;
import org.thingworld.persistence.Commit;
import org.thingworld.persistence.ICommitDAO;
import org.thingworld.persistence.IEventRecordDAO;
import org.thingworld.persistence.IStreamDAO;
import org.thingworld.persistence.MockCommitDAO;
import org.thingworld.persistence.MockEventRecordDAO;
import org.thingworld.persistence.MockStreamDAO;
import org.thingworld.persistence.PersistenceContext;

public class CommitCacheTests extends BaseMesfTest 
{
	@Test
	public void test() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		IEventRecordDAO eventDAO = new MockEventRecordDAO();
		PersistenceContext persistenceCtx = new PersistenceContext(dao, streamDAO, eventDAO);
		CommitMgr mgr = new CommitMgr(null, persistenceCtx, new CommitCache(dao), new StreamCache(streamDAO));
		int n = 6;
		for(int i = 0; i < n; i++)
		{
			mgr.writeNoOp();
		}
		
		mgr.dump();
		assertEquals(n, dao.size());
		
		CommitCache cache = new CommitCache(dao);
		List<Commit> L = cache.loadRange(0, n);
		for(Commit commit : L)
		{
			log(commit.getId().toString());
		}
		log("again..");
		L = cache.loadRange(0, n);
		for(Commit commit : L)
		{
			log(commit.getId().toString());
		}
	}

	//-----------------------------
	protected static String fix(String s)
	{
		s = s.replace('\'', '"');
		return s;
	}

	@Before
	public void init()
	{
		super.init();
	}
}
