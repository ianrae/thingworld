package tests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.thingworld.persistence.Commit;
import org.thingworld.persistence.ICommitDAO;
import org.thingworld.persistence.MockCommitDAO;

import testhelper.BaseMesfTest;

public class CommitTblTests extends BaseMesfTest 
{
	@Test
	public void test() throws Exception
	{
		log("sdf");
		ICommitDAO dao = new MockCommitDAO();
		assertEquals(0, dao.size());
		List<Commit> L = dao.all();
		assertEquals(0, L.size());
		
		Commit obj = new Commit();
		obj.setStreamId(10L);
		obj.setJson("{}");
		
		dao.save(obj);
		assertEquals(1, dao.size());
		L = dao.all();
		assertEquals(1, L.size());
		assertEquals(10L, L.get(0).getStreamId().longValue());
		assertEquals(1L, L.get(0).getId().longValue());
	}

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
