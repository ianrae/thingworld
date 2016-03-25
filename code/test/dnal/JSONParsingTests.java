package dnal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

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
		String json = findJSONString(s, 0);
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
		String json = findJSONString(input, 0);
		log(json);
		assertEquals(expected, json);
	}
//	http://stackoverflow.com/questions/19176024/how-to-escape-special-characters-in-building-a-json-string
	private String findJSONString(String s, int startPos) {
		int pos = s.indexOf('"', startPos);
		
		int endpos = 0;
		boolean inQuote = false;
		for(int i = pos + 1; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '\\') {
				if (! inQuote) {
					inQuote = true;
				} else {
					inQuote = false;
				}
			}
			
			if (ch == '"') {
				if (! inQuote) {
					endpos = i;
					break;
				} else {
					inQuote = false;
				}
			} else if ("/bfnrtu".indexOf(ch) >= 0 && inQuote) {
				inQuote = false;
			}
		}
		
		return s.substring(pos + 1, endpos);
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
