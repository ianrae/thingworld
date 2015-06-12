package mesf;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.thingworld.persistence.IStreamDAO;
import org.thingworld.persistence.MockStreamDAO;
import org.thingworld.persistence.Stream;

public class ObjTableTests extends BaseMesfTest 
{

	@Test
	public void test() throws Exception
	{
		log("sdf");
		IStreamDAO dao = new MockStreamDAO();
		assertEquals(0, dao.size());
		List<Stream> L = dao.all();
		assertEquals(0, L.size());
		
		Stream obj = new Stream();
		obj.setSnapshotId(10L);
		
		dao.save(obj);
		assertEquals(1, dao.size());
		L = dao.all();
		assertEquals(1, L.size());
		assertEquals(10L, L.get(0).getSnapshotId().longValue());
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
