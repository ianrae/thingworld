package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mef.twixt.StringValue;
import org.mef.twixt.Value;
import org.mef.twixt.binder.TwixtForm;
import org.mef.twixt.validate.ValContext;
import org.mef.twixt.validate.ValidationErrors;
import org.thingworld.BindingIntercept;
import org.thingworld.EventProjector;
import org.thingworld.ICommitObserver;
import org.thingworld.IDomainIntializer;
import org.thingworld.IFormBinder;
import org.thingworld.IRequestInterceptor;
import org.thingworld.InterceptorContext;
import org.thingworld.MContext;
import org.thingworld.Permanent;
import org.thingworld.Presenter;
import org.thingworld.Reply;
import org.thingworld.Request;
import org.thingworld.cmd.ProcRegistry;
import org.thingworld.entity.EntityManagerRegistry;
import org.thingworld.entity.EntityMgr;
import org.thingworld.event.Event;
import org.thingworld.event.EventManagerRegistry;
import org.thingworld.event.EventMgr;
import org.thingworld.log.Logger;
import org.thingworld.persistence.PersistenceContext;
import org.thingworld.readmodel.ReadModel;
import org.thingworld.util.SfxTrail;

import testhelper.BaseMesfTest;
import testhelper.FactoryGirl;
import testhelper.LocalMockBinder;
import testhelper.UserInitializer;
import tests.UserTests.MyUserPerm;
import tests.UserTests.MyUserProc;
import tests.UserTests.User;

/*
 * done TaskTests and add a UserTaskRM, cascading delete
 * presenter, QryCmd
 * guava cache in scache,objcache,commitcache
 * metrics
 * done logger and error tracker singletons
 * play 2.4
 * computerDatabase sample
 * snapshots
 * learn <T> fn trick
 */

public class PresenterTests extends BaseMesfTest 
{
	public static class MyReply extends Reply
	{
		public int a;
	}

	public static class UserTwixt extends TwixtForm
	{
		public StringValue s;

		public UserTwixt()
		{
			s = new StringValue();

			s.setValidator( (ValContext valctx, Value obj) -> {
				StringValue val = (StringValue) obj;
				if (! val.get().contains("a"))
				{
					valctx.addError("sdfdfs");
				}
			});
		}
		//		private class MyValidator implements Validator
		//		{
		//
		//			@Override
		//			public void validate(ValContext valctx, Value obj) 
		//			{
		//				StringValue val = (StringValue) obj;
		//				String s = val.get();
		//				if (! s.contains("a"))
		//				{
		//					valctx.addError("sdfdfs");
		//				}
		//			}
		//		}
	}

	public static class UserAddedEvent extends Event
	{
		public UserAddedEvent()
		{}
		public UserAddedEvent(long entityid)
		{
			super(entityid);
		}
	}


	public static class MyPres extends Presenter
	{
		public class InsertCmd extends Request
		{
			public int a;
			public String s;
		}
		public static class UpdateCmd extends Request
		{
			public UpdateCmd(long id, IFormBinder binder)
			{
				this.entityId = id;
				this.binder = binder;
			}
		}

		private MyReply reply = new MyReply();
		public SfxTrail trail = new SfxTrail();

		public MyPres(MContext mtx)
		{
			super(mtx);
		}
		protected Reply createReply()
		{
			return reply;
		}

		public void onRequest(Request cmd)
		{
			Logger.log("i n d e xx");
			trail.add("index");
			reply.setDestination(Reply.VIEW_INDEX);
		}
		public void onInsertCmd(InsertCmd cmd)
		{
			Logger.log("insert");
			trail.add("index");

			User scooter = new User();
			scooter.setA(cmd.a);
			scooter.setB(10);
			scooter.setS(cmd.s);

			insertEntity(scooter);
			publishEvent(new UserAddedEvent(scooter.getId()));
			reply.setDestination(Reply.VIEW_INDEX);
		}
		//		public void onUpdateCmd(UpdateCmd cmd) throws Exception
		//		{
		//			Logger.log("update");
		//			trail.add("update");
		//
		//			if (cmd.getFormBinder().bind())
		//			{
		//				UserTwixt twixt = (UserTwixt) cmd.getFormBinder().get();
		//				Logger.log("twixt a=%s", twixt.s);
		//				User scooter = (User) mtx.loadEntity(User.class, cmd.getEntityId());
		//				twixt.copyTo(scooter);
		//				updateObject(scooter);
		//				reply.setDestination(Reply.VIEW_INDEX);
		//			}
		//			else
		//			{
		//				reply.setDestination(Reply.VIEW_EDIT);
		//			}
		//		}

		public void onUpdateCmd(UpdateCmd cmd) throws Exception 
		{
			Logger.log("update");
			trail.add("update");
			//binding fails handled in interceptor
			UserTwixt twixt = (UserTwixt) cmd.getFormBinder().get();
			Logger.log("twixt a=%s", twixt.s);
			User scooter = loadEntity(cmd);
			twixt.copyTo(scooter);
			updateEntity(scooter);
			reply.setDestination(Reply.VIEW_INDEX);
		}

		private User loadEntity(Request cmd) throws Exception
		{
			User scooter = null;
			scooter = (User) mtx.loadEntity(User.class, cmd.getEntityId());
			return scooter;
		}

		protected void beforeRequest(Request request, InterceptorContext itx)
		{
			trail.add("before");
		}
		protected void afterRequest(Request request)
		{
			trail.add("after");
		}
	}
	public static class MyEventSub extends ReadModel
	{
		public SfxTrail trail = new SfxTrail();

		@Override
		public boolean willAcceptEvent(Event event) 
		{
			return true;
		}

		@Override
		public void observeEvent(Event event) 
		{
			if (event instanceof UserAddedEvent)
			{
				Logger.log("wooohoo");
			}
		}

		@Override
		public void freshen(MContext mtx, ICommitObserver extraObserver) 
		{
			EventProjector projector = mtx.createEventProjector();
			projector.run(mtx, this, this.lastEventId); //extraObserver not used here
		}
	}


	@Test
	public void test() throws Exception
	{
		MyUserPerm perm = this.createPerm();
		MContext mtx = perm.createMContext();
		MyPres pres = new MyPres(mtx);

		Request request = new Request();
		Reply reply = pres.process(request);

		assertNotNull(reply);
		assertTrue(reply instanceof MyReply);
		assertEquals(Reply.VIEW_INDEX, reply.getDestination());

		log(pres.trail.getTrail());
	}	

	@Test
	public void test22() throws Exception
	{
		MyUserPerm perm = this.createPerm();

		int n = 2; 
		for(int i = 0; i < n; i++)
		{
			log(String.format("%d..	", i));
			MContext mtx = perm.createMContext();
			MyPres pres = new MyPres(mtx);
			MyPres.InsertCmd cmd = pres.new InsertCmd();
			cmd.a = 101+i;
			cmd.s = String.format("bob%d", i+1);

			Reply reply = pres.process(cmd);

			long id = perm.createMContext().getMaxId();
			assertEquals(i+1, id); 

			mtx = perm.createMContext();
			eventSub.freshen(mtx, null); //run event publishing 
		}
	}

	@Test
	public void test3() throws Exception
	{
		MyUserPerm perm = this.createPerm();

		int n = 1; 
		for(int i = 0; i < n; i++)
		{
			log(String.format("%d..	", i));
			MContext mtx = perm.createMContext();
			MyPres pres = new MyPres(mtx);
			MyPres.InsertCmd cmd = pres.new InsertCmd();
			cmd.a = 101+i;
			cmd.s = String.format("bob%d", i+1);
			Reply reply = pres.process(cmd);

			mtx = perm.createMContext();
			pres = createMyPres(mtx, perm, Reply.VIEW_EDIT);
			LocalMockBinder<UserTwixt> binder = new LocalMockBinder<UserTwixt>(UserTwixt.class, buildMap(true));

			MyPres.UpdateCmd ucmd = new MyPres.UpdateCmd(1L, binder);
			reply = pres.process(ucmd);
		}
	}

	@Test
	public void testValFail() throws Exception
	{
		MyUserPerm perm = this.createPerm();

		MContext mtx = perm.createMContext();
		MyPres pres = createMyPres(mtx, perm);
		MyPres.InsertCmd cmd = pres.new InsertCmd();
		cmd.a = 101;
		cmd.s = String.format("bob%d", 1);
		Reply reply = pres.process(cmd);

		mtx = perm.createMContext();
		pres = createMyPres(mtx, perm, Reply.VIEW_EDIT);
		LocalMockBinder<UserTwixt> binder = new LocalMockBinder<UserTwixt>(UserTwixt.class, buildMap(false));

		MyPres.UpdateCmd ucmd = new MyPres.UpdateCmd(1L, binder);
		reply = pres.process(ucmd);
		assertEquals(Reply.VIEW_EDIT, reply.getDestination());
	}

	private Map<String,String> buildMap(boolean okValues)
	{
		Map<String,String> map = new TreeMap<String,String>();
		if (okValues)
		{
			map.put("s", "abc");
		}
		else
		{
			map.put("s", "bb");
		}

		return map;
	}

	private static class MyIntercept implements IRequestInterceptor
	{
		public SfxTrail trail;
		public int interceptorType;

		@Override
		public void process(Request request, Reply reply, InterceptorContext itx) 
		{
			trail.add("MYINTERCEPT");
			if (interceptorType == 2)
			{
				itx.haltProcessing = true;
			}
		}
	}

	@Test
	public void testFullChain() throws Exception
	{
		runOnce("before;index;after", 1L, 0);
		runOnce("MYINTERCEPT;before;index;after", 1L, 1);
		runOnce("MYINTERCEPT", 0L, 2);
	}

	private void runOnce(String expected, long expectedMaxId, int interceptorType) throws Exception
	{
		MyUserPerm perm = this.createPerm();

		MContext mtx = perm.createMContext();
		MyPres pres = new MyPres(mtx);
		if (interceptorType > 0)
		{
			MyIntercept intercept = new MyIntercept();
			intercept.trail = pres.trail;
			intercept.interceptorType = interceptorType;
			pres.addInterceptor(intercept);
		}

		MyPres.InsertCmd cmd = pres.new InsertCmd();
		cmd.a = 101;
		cmd.s = String.format("bob");

		Reply reply = pres.process(cmd);

		assertEquals(expected, pres.trail.getTrail());
		long id = perm.createMContext().getMaxId();
		assertEquals(expectedMaxId, id); 
	}

	private MyEventSub eventSub;

	//-----------------------
	MyPres createMyPres(MContext mtx, Permanent perm)
	{
		MyPres pres = new MyPres(mtx);
		return pres;
	}
	MyPres createMyPres(MContext mtx, Permanent perm, int failDestination)
	{
		MyPres pres = new MyPres(mtx);
		pres.addInterceptor(new BindingIntercept(failDestination));
		return pres;
	}


	private MyUserPerm createPerm() throws Exception
	{
		//create long-running objects
		PersistenceContext persistenceCtx = FactoryGirl.createPersistenceContext();
		MyUserPerm perm = new MyUserPerm(persistenceCtx);

		UserInitializer userinit = new UserInitializer();
		userinit.init(perm);

		eventSub = new MyEventSub();
		perm.registerReadModel(eventSub);
		perm.start();
		return perm;
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
		ValidationErrors.inUnitTest = true;
	}

	@After
	public void shutdown()
	{
		ValidationErrors.inUnitTest = false;
	}
}
