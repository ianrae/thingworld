package testhelper;

import org.thingworld.IDomainIntializer;
import org.thingworld.Permanent;
import org.thingworld.cmd.ProcRegistry;
import org.thingworld.entity.EntityManagerRegistry;
import org.thingworld.entity.EntityMgr;
import org.thingworld.event.EventManagerRegistry;
import org.thingworld.event.EventMgr;

import tests.PresenterTests.UserAddedEvent;
import tests.UserTests.MyUserProc;
import tests.UserTests.User;

public class UserInitializer implements IDomainIntializer
{

	@Override
	public void init(Permanent perm)
	{
		//create long-running objects

		EntityManagerRegistry registry = perm.getEntityManagerRegistry();
		registry.register(User.class, new EntityMgr<User>(User.class));

		ProcRegistry procRegistry = perm.getProcRegistry();
		procRegistry.register(User.class, MyUserProc.class);

		EventManagerRegistry evReg = perm.getEventManagerRegistry();
		evReg.register(UserAddedEvent.class, new EventMgr<UserAddedEvent>(UserAddedEvent.class));
	}

}
