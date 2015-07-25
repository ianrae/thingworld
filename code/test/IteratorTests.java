

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.thingworld.cache.ISegCacheLoader;
import org.thingworld.cache.SegmentedGuavaCache;
import org.thingworld.persistence.HasId;

import testhelper.BaseMesfTest;

public class IteratorTests extends BaseMesfTest
{
	public static class MyIter implements Iterable<String>{
	    public String[] a=null; //make this final if you can
	    public MyIter(String[] arr){
	        a=arr; //maybe you should copy this array, for fear of external modification
	    }

	    //the interface is sufficient here, the outside world doesn't need to know
	    //about your concrete implementation.
	    public Iterator<String> iterator(){
	        //no point implementing a whole class for something only used once
	        return new Iterator<String>() {
	            private int count=0;
	            //no need to have constructor which takes MyIter, (non-static) inner class has access to instance members
	            public boolean hasNext(){
	                //simplify
	                return count < a.length;
	            }
	            public String next(){
	                return a[count++]; //getting clever
	            }

	            public void remove(){
	                throw new UnsupportedOperationException();
	            }
	        };
	    }
	}
	
	public static class MyLoader implements ISegCacheLoader<MyString>
	{
		public List<MyString> list = new ArrayList<>();
		
		public List<MyString> loadRange(long startId, long n)
		{
			System.out.println(String.format("LD %d.%d", startId,n));
			List<MyString> resultL = new ArrayList<>();
			startId = (startId == 0L) ? 1 : startId;
			long i = startId;
			while(resultL.size() < n)
			{
				boolean found = false;
				for(MyString ss : list)
				{
					if (ss.getId().longValue() == i)
					{
						resultL.add(ss);
						found = true;
						break;
					}
				}
				
				if (! found)
				{
					break;
				}
				i++;
			}
			
			return resultL;
		}
	}
	
	
	@Test
	public void test() 
	{
		String[] ar = new String[] { "ab", "cd", "ef" };
		
		MyIter iter = new MyIter(ar);
		
		for(String s : iter)
		{
			log(s);
		}
		
	}
	
	public static class MyString implements HasId
	{
		public String s;
		public long id;
		@Override
		public Long getId() {
			return id;
		}
	}
	
	@Test
	public void testSeg() 
	{
		MyLoader loader = new MyLoader();
		SegmentedGuavaCache<MyString> cache = new SegmentedGuavaCache<MyString>();
		cache.init(4, loader);
		String[] ar = new String[] { "1", "2", "3"}; //first seg always has one less since is no id 0
		List<MyString> ssL = buildList(ar, 1);
		cache.putList(0, ssL);
		
		MyString s = cache.getOne(1);
		chkCache(cache, "1", 1);
		chkCache(cache, "2", 2);
		chkCache(cache, "3", 3);
		chkCache(cache, null, 4);

		String[] ar2 = new String[] {"4", "5", "6", "7"};
		ssL = buildList(ar2, 4);
		cache.putList(4, ssL);
		
		for(long i = 4; i < 8; i++)
		{
			Long n = i;
			chkCache(cache, n.toString(), i);
		}
		chkCache(cache, null, 9);
		
		String[] ar3 = new String[] { "8"};
		ssL = buildList(ar3, 8);
		cache.putList(8, ssL);
		
		for(long i = 8; i < 9; i++)
		{
			Long n = i;
			chkCache(cache, n.toString(), i);
		}
		chkCache(cache, null, 10);
		
		List<MyString> tmpL = cache.getRange(6, 5);
		for(MyString ss : tmpL)
		{
			log(ss.s);
		}
	}
	
	private List<MyString> buildList(String[] ar, int startId)
	{
		List<MyString> ssL = new ArrayList<>();
		for(int i = 0; i < ar.length; i++)
		{
			MyString ss = new MyString();
			ss.id = i + startId; //start at 1
			ss.s = ar[i];
			ssL.add(ss);
		}
		return ssL;
	}
	
	@Test
	public void testSeg2() 
	{
		MyLoader loader = new MyLoader();
		SegmentedGuavaCache<MyString> cache = new SegmentedGuavaCache<MyString>();
		cache.init(4, loader);
		String[] ar = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
		List<MyString> ssL = buildList(ar, 1);
		loader.list = ssL;
		
		for(long i = 1; i <= 10; i++)
		{
			Long n = i;
			log(n.toString());
			chkCache(cache, n.toString(), i);
		}
		chkCache(cache, null, 11);
		
		List<MyString> tmpL = cache.getRange(6, 5);
		for(MyString ss : tmpL)
		{
			log(ss.s);
		}
	}
	
	//--helpers--
	private void chkCache(SegmentedGuavaCache<MyString> cache, String expected, long entitId)
	{
		MyString ss = cache.getOne(entitId);
		if (expected == null)
		{
			assertEquals(null, ss);
		}
		else
		{
			assertEquals(expected, ss.s);
		}
		
	}

	@Before
	public void init()
	{
		super.init();
	}
}
