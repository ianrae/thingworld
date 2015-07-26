

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.thingworld.IDomainIntializer;
import org.thingworld.MContext;
import org.thingworld.Permanent;
import org.thingworld.cmd.CommandProcessor;
import org.thingworld.cmd.ProcRegistry;
import org.thingworld.entity.EntityManagerRegistry;
import org.thingworld.entity.EntityMgr;
import org.thingworld.persistence.Commit;
import org.thingworld.persistence.ICommitDAO;
import org.thingworld.persistence.PersistenceContext;
import org.thingworld.persistence.Stream;
import org.thingworld.readmodel.ReadModel;
import org.thingworld.readmodel.ReadModelRepository;

import testhelper.BaseMesfTest;
import testhelper.FactoryGirl;
import tests.CommitMgrTests.InsertScooterCmd;
import tests.CommitMgrTests.MyCmdProc;
import tests.CommitMgrTests.UpdateScooterCmd;
import tests.CommitMgrTests.ShowScooterCmd;
import tests.ObjManagerTests.Scooter;

public class EntityRepositoryTests extends BaseMesfTest 
{
	public static class MyPerm extends Permanent
	{
		public TopLevelTests.MyReadModel readModel1;
		
		public MyPerm(PersistenceContext persistenceCtx) 
		{
			super(persistenceCtx);//, registry, procRegistry, evReg);
			
			readModel1 = new TopLevelTests.MyReadModel();
			registerReadModel(readModel1);
		}
	}
	
	@Test
	public void test() throws Exception
	{
		//create long-running objects
		PersistenceContext persistenceCtx = FactoryGirl.createPersistenceContext();
		ICommitDAO dao = persistenceCtx.getDao();
		
		MyPerm perm = new MyPerm(persistenceCtx);
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
		assertEquals(1L, cmd.entityId); //!! we set this in proc (only on insert)
		
		log("1st update");
		mtx = perm.createMContext();
		proc = mtx.findProc(Scooter.class);
		UpdateScooterCmd ucmd = new UpdateScooterCmd();
		ucmd.s = "more";
		ucmd.entityId = 1L;
		proc.process(ucmd);
		
		
		log(String.format("other object"));
		mtx = perm.createMContext();
		cmd = new InsertScooterCmd();
		cmd.a = 100;
		cmd.s = "abigail";
		proc = mtx.findProc(Scooter.class);
		proc.process(cmd);
		assertEquals(0, perm.readModel1.size()); //haven't done yet
		assertEquals(2L, cmd.entityId); //!! we set this in proc (only on insert)
		
		
		//we don't have an event bus. so cmd processing does not update objcache
		//do this for two reasons
		// -so objects don't change partially way through a web request
		// -objcache is synchronized so is perf issue
		chkScooterStr(perm, ucmd.entityId, "bob");
		
		log("show");
		mtx = perm.createMContext();
		proc = mtx.findProc(Scooter.class);
		ShowScooterCmd sscmd = new ShowScooterCmd();
		sscmd.entityId = 1L;
		proc.process(sscmd);
		chkScooterStr(perm, ucmd.entityId, "more");
		
		log("show2");
		mtx = perm.createMContext();
		proc = mtx.findProc(Scooter.class);
		sscmd = new ShowScooterCmd();
		sscmd.entityId = 1L;
		proc.process(sscmd);
		chkScooterStr(perm, ucmd.entityId, "more");
		
		
		log(String.format("2nd"));
		mtx = perm.createMContext();
		proc = mtx.findProc(Scooter.class);
		ucmd = new UpdateScooterCmd();
		ucmd.s = "more2";
		ucmd.entityId = 1L;
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

	
	@Test
	public void test10() throws Exception
	{
		//create long-running objects
		PersistenceContext persistenceCtx = FactoryGirl.createPersistenceContext();
		ICommitDAO dao = persistenceCtx.getDao();
		
		MyPerm perm = new MyPerm(persistenceCtx);
		TopLevelTests.ScooterInitializer init = new TopLevelTests.ScooterInitializer();
		init.init(perm);
		perm.start();
		assertEquals(0, perm.readModel1.size());
		
		for(int i = 0; i < 10; i++)
		{
			log(String.format("1st"));
			MContext mtx = perm.createMContext();
			InsertScooterCmd cmd = new InsertScooterCmd();
			cmd.a = 15;
			cmd.s = "bob";
			CommandProcessor proc = mtx.findProc(Scooter.class);
			proc.process(cmd);
		}
		assertEquals(0, perm.readModel1.size()); //haven't done yet
		
		//commit log way ahead of readmodel, so we load chunks 4 at a time
		log("acquire");
		MContext mtx = perm.createMContext();
		mtx.acquire(TopLevelTests.MyReadModel.class);
		log("acquire..again");
		mtx = perm.createMContext();
		mtx.acquire(TopLevelTests.MyReadModel.class);
		
		//add 2 more commits
		for(int i = 0; i < 2; i++)
		{
			log(String.format("1st"));
			mtx = perm.createMContext();
			InsertScooterCmd cmd = new InsertScooterCmd();
			cmd.a = 15;
			cmd.s = "bob";
			CommandProcessor proc = mtx.findProc(Scooter.class);
			proc.process(cmd);
		}
		
		//only loads 2 chunks to catch up
		log("acquire..again3");
		mtx = perm.createMContext();
		mtx.acquire(TopLevelTests.MyReadModel.class);
		
		//EntityRepo is completely separate from readmodel so it's still empty
		//but if the entityrepo already had some objects in it, they would be freshened
		log("show");
		mtx = perm.createMContext();
		CommandProcessor proc = mtx.findProc(Scooter.class);
		ShowScooterCmd sscmd = new ShowScooterCmd();
		sscmd.entityId = 1L;
		proc.process(sscmd);
		
		log("done");
	}
	
	private void chkScooterStr(MyPerm perm, long entityId, String string) 
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
