package testhelper;

import org.thingworld.persistence.ICommitDAO;
import org.thingworld.persistence.IEventRecordDAO;
import org.thingworld.persistence.IStreamDAO;
import org.thingworld.persistence.MockCommitDAO;
import org.thingworld.persistence.MockEventRecordDAO;
import org.thingworld.persistence.MockStreamDAO;
import org.thingworld.persistence.PersistenceContext;

public class FactoryGirl 
{
	public static PersistenceContext createPersistenceContext()
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		IEventRecordDAO eventDAO = new MockEventRecordDAO();
		
		PersistenceContext persistenceCtx = new PersistenceContext(dao, streamDAO, eventDAO);
		return persistenceCtx;
	}

}
