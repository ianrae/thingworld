

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import testhelper.BaseMesfTest;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JacksonTests extends BaseMesfTest 
{
	@JsonFilter("myFilter")
	public static class Taxi
	{
		public int a;
		public int b;
		public String s;
	}
	
	public static class Uber
	{
		public int a;
		public Object obj;
	}
	

	@Test
	public void test() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'b':26,'s':'abc'}";
		ObjectMapper mapper = new ObjectMapper();
		Taxi taxi = mapper.readValue(fix(json), Taxi.class);	
		assertEquals(15, taxi.a);
		assertEquals(26, taxi.b);
		assertEquals("abc", taxi.s);
	}

	@Test
	public void testPartial() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'s':'def'}";
		ObjectMapper mapper = new ObjectMapper();
		Taxi taxi = mapper.readValue(fix(json), Taxi.class);	
		assertEquals(15, taxi.a);
		assertEquals(0, taxi.b);
		assertEquals("def", taxi.s);
	}

	@Test
	public void testOverlay() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'b':26,'s':'abc'}";
		ObjectMapper mapper = new ObjectMapper();
		Taxi taxi = mapper.readValue(fix(json), Taxi.class);	
		assertEquals(15, taxi.a);
		assertEquals(26, taxi.b);
		assertEquals("abc", taxi.s);
		chkTaxi(taxi, 15,26,"abc");

		ObjectReader r = mapper.readerForUpdating(taxi);
		json = "{'a':150,'s':'def'}";
		r.readValue(fix(json));
		chkTaxi(taxi, 150,26,"def");
	}

	@Test
	public void testWriteFilter() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'b':26,'s':'abc'}";
		ObjectMapper mapper = new ObjectMapper();
		Taxi taxi = mapper.readValue(fix(json), Taxi.class);	
		chkTaxi(taxi, 15,26,"abc");

		SimpleFilterProvider sfp = new SimpleFilterProvider();
		// create a  set that holds name of User properties that must be serialized
		Set userFilterSet = new HashSet<String>();
		userFilterSet.add("a");
		userFilterSet.add("s");

		sfp.addFilter("myFilter",SimpleBeanPropertyFilter.filterOutAllExcept(userFilterSet));

		// create an objectwriter which will apply the filters 
		ObjectWriter writer = mapper.writer(sfp);

		String json2 = writer.writeValueAsString(taxi);
		String s = fix("{'a':15,'s':'abc'}");
		assertEquals(s, json2);
	}
	
	@Test
	public void testUber() throws Exception
	{
		log("string");
		String json = "{'a':15,'obj':'abc'}";
		ObjectMapper mapper = new ObjectMapper();
		Uber taxi = mapper.readValue(fix(json), Uber.class);	
		assertEquals(15, taxi.a);
		assertEquals("abc", taxi.obj.toString());
		assertEquals(true, taxi.obj instanceof String);
		
		log("integer");
		json = "{'a':15,'obj':1503}";
		mapper = new ObjectMapper();
		taxi = mapper.readValue(fix(json), Uber.class);	
		assertEquals(15, taxi.a);
		assertEquals(1503, taxi.obj);
		assertEquals(true, taxi.obj instanceof Integer);
		
		log("long");
		Long lval = Long.MAX_VALUE - 20;
		json = String.format("{'a':15,'obj':%s}", lval);
		mapper = new ObjectMapper();
		taxi = mapper.readValue(fix(json), Uber.class);	
		assertEquals(15, taxi.a);
		assertEquals(lval.longValue(), taxi.obj);
		assertEquals(true, taxi.obj instanceof Long);
		
		log("boolean");
		json = String.format("{'a':15,'obj':false}");
		mapper = new ObjectMapper();
		taxi = mapper.readValue(fix(json), Uber.class);	
		assertEquals(15, taxi.a);
		assertEquals(false, taxi.obj);
		assertEquals(true, taxi.obj instanceof Boolean);
		
		log("date");
		Date dt = new Date();
		json = String.format("{'a':15,'obj':%d}", dt.getTime());
		mapper = new ObjectMapper();
		taxi = mapper.readValue(fix(json), Uber.class);	
		assertEquals(15, taxi.a);
		assertEquals(dt.getTime(), taxi.obj);
		assertEquals(true, taxi.obj instanceof Long);
	}

	
	

	//-----------------------------
	private void chkTaxi(Taxi taxi, int expectedA, int expectedB, String expectedStr)
	{
		assertEquals(expectedA, taxi.a);
		assertEquals(expectedB, taxi.b);
		assertEquals(expectedStr, taxi.s);

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
