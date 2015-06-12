package mesf.readmodel;

import java.util.List;

import org.thingworld.persistence.Commit;
import org.thingworld.persistence.ICommitDAO;
import org.thingworld.persistence.IStreamDAO;
import org.thingworld.persistence.PersistenceContext;


public class ReadModelLoader
{
	private ICommitDAO dao;
	private IStreamDAO streamDAO;
	private long maxId; //per current epoch

	public ReadModelLoader(PersistenceContext persistenceCtx, long maxId)
	{
		this.dao = persistenceCtx.getDao();
		this.streamDAO = persistenceCtx.getStreamDAO();
		this.maxId = maxId;
	}
	
	public List<Commit> loadCommits(long startId)
	{
		long n = maxId - startId + 1;
		List<Commit> L = dao.loadRange(startId, n);
		return L;
	}

	public long getMaxId() 
	{
		return maxId;
	}
}