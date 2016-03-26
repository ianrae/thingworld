package dnal.myformat;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mef.dnal.parser.JSONStringParser;

import testhelper.BaseMesfTest;
import testhelper.BaseTest;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONParsingTests extends BaseTest 
{
	@JsonFilter("myFilter")
	public static class Taxi
	{
		public String s;
	}
	
	@Test
	public void test() throws Exception
	{
		log("sdf");
		String json = "{'s':'abc'}";
		ObjectMapper mapper = new ObjectMapper();
		Taxi taxi = mapper.readValue(fix(json), Taxi.class);	
		assertEquals("abc", taxi.s);
	}
	
	@Test
	public void test2() {
		String s = "name: \"bob smith\"";
		JSONStringParser jparser = new JSONStringParser();
		String json = jparser.findJSONString(s, 0);
		log(json);
		assertEquals("bob smith", json);
		
		check("name: \"bob smith\"", "bob smith");
		
		s = fix("name: 'bob \\'jim\\' smith'");
		check(s, "bob \\\"jim\\\" smith");

		s = fix("name: 'bob \\'jim\\' smith';");
		check(s, "bob \\\"jim\\\" smith");

		s = fix("name: 'bob \\'jim\\' smith' //comment");
		check(s, "bob \\\"jim\\\" smith");
	}
	private void check(String input, String expected) {
		JSONStringParser jparser = new JSONStringParser();
		int pos = input.indexOf(':');
		String json = jparser.findJSONString(input, pos);
		log(json);
		assertEquals(expected, json);
	}

	//-----------------------------
	private void chkTaxi(Taxi taxi, String expectedStr)
	{
		assertEquals(expectedStr, taxi.s);

	}
	protected static String fix(String s)
	{
		s = s.replace('\'', '"');
		return s;
	}

}
