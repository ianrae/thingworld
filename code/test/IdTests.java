import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.thingworld.MContext;
import org.thingworld.cmd.CommandProcessor;
import org.thingworld.persistence.ICommitDAO;
import org.thingworld.persistence.MockStreamDAO;
import org.thingworld.persistence.PersistenceContext;
import org.thingworld.persistence.Stream;
import org.thingworld.readmodel.ReadModelRepository;

import testhelper.BaseMesfTest;
import testhelper.FactoryGirl;
import tests.CommitMgrTests.InsertScooterCmd;
import tests.CommitMgrTests.ShowScooterCmd;
import tests.CommitMgrTests.UpdateScooterCmd;
import tests.ObjManagerTests.Scooter;


public class IdTests extends BaseMesfTest
{

	@Test
	public void testIds() 
	{
		MockStreamDAO dao = new MockStreamDAO();
		dao.useNonContiguousIds = true;
		for(int i = 0; i < 4; i++)
		{
			Stream stream = new Stream();
			stream.setSnapshotId(44L);
			stream.setType("bacon");
			dao.save(stream);
			
			Long id = stream.getId();
			log(id.toString());
		}
	}

	@Test
	public void test() throws Exception
	{
		MockStreamDAO.useNonContiguousIds = true;
		//create long-running objects
		PersistenceContext persistenceCtx = FactoryGirl.createPersistenceContext();
		ICommitDAO dao = persistenceCtx.getDao();
		
		EntityRepositoryTests.MyPerm perm = new EntityRepositoryTests.MyPerm(persistenceCtx);
		TopLevelTests.ScooterInitializer init = new TopLevelTests.ScooterInitializer();
		init.init(perm);
		perm.start();
		assertEquals(0, perm.readModel1.size());
		
		log(String.format("1st"));
		MContext mtx = perm.createMContext();
		InsertScooterCmd cmd = new InsertScooterCmd();
		cmd.a = 15;
		cmd.s = "bob";
		CommandProcessor proc = mtx.findProc(Scooter.class);
		proc.process(cmd);
		assertEquals(0, perm.readModel1.size()); //haven't done yet
		assertEquals(12L, cmd.entityId); //!! we set this in proc (only on insert)
		
		log("1st update");
		mtx = perm.createMContext();
		proc = mtx.findProc(Scooter.class);
		UpdateScooterCmd ucmd = new UpdateScooterCmd();
		ucmd.s = "more";
		ucmd.entityId = 12L;
		proc.process(ucmd);
		
		
		log(String.format("other object"));
		mtx = perm.createMContext();
		cmd = new InsertScooterCmd();
		cmd.a = 100;
		cmd.s = "abigail";
		proc = mtx.findProc(Scooter.class);
		proc.process(cmd);
		assertEquals(0, perm.readModel1.size()); //haven't done yet
		assertEquals(14L, cmd.entityId); //!! we set this in proc (only on insert)
		
		
		//we don't have an event bus. so cmd processing does not update objcache
		//do this for two reasons
		// -so objects don't change partially way through a web request
		// -objcache is synchronized so is perf issue
		chkScooterStr(perm, ucmd.entityId, "bob");
		
		log("show");
		mtx = perm.createMContext();
		proc = mtx.findProc(Scooter.class);
		ShowScooterCmd sscmd = new ShowScooterCmd();
		sscmd.entityId = 12L;
		proc.process(sscmd);
		chkScooterStr(perm, ucmd.entityId, "more");
		
		log("show2");
		mtx = perm.createMContext();
		proc = mtx.findProc(Scooter.class);
		sscmd = new ShowScooterCmd();
		sscmd.entityId = 12L;
		proc.process(sscmd);
		chkScooterStr(perm, ucmd.entityId, "more");
		
		
		log(String.format("2nd"));
		mtx = perm.createMContext();
		proc = mtx.findProc(Scooter.class);
		ucmd = new UpdateScooterCmd();
		ucmd.s = "more2";
		ucmd.entityId = 12L;
		proc.process(ucmd);
		chkScooterStr(perm, ucmd.entityId, "more");
		
		assertEquals(0, perm.readModel1.size()); //haven't done yet
		assertEquals(4, dao.size());
		ReadModelRepository readmodelMgr = perm.getreadmodelMgr();
//		Object obj = readmodelMgr.loadReadModel(perm.readModel1, mtx.getVloader());
//		assertEquals(1, perm.readModel1.size()); 
		
		log("acquire");
		mtx.acquire(TopLevelTests.MyReadModel.class);
		log("done");
	}

	
	private void chkScooterStr(EntityRepositoryTests.MyPerm perm, long entityId, String string) 
	{
		Scooter scooter = (Scooter) perm.loadEntityFromRepo(entityId);
		assertEquals(string, scooter.getS());
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
