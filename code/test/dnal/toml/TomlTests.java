package dnal.toml;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import testhelper.BaseTest;

import com.moandjiezana.toml.Toml;

public class TomlTests extends BaseTest {

	@Test
	public void test() {
		Toml toml = new Toml().read("a=1");
		long n = toml.getLong("a");
		assertEquals(1, n);
		Long n2 = toml.getLong("b");
		assertEquals(null, n2);
	}

	@Test
	public void test0() {
		Toml toml = new Toml().read(buildFile(0));
		long n = toml.getLong("TYPE.Position.a");
		assertEquals(1, n);
		
		List<String> membL = toml.getList("TYPE.Position.CONTAINS");
		assertEquals(2, membL.size());
		assertEquals("int x", membL.get(0));
	}
	@Test
	public void test1() {
		Toml toml = new Toml().read(buildFile(1));
		long n = toml.getLong("TYPE.Position.a");
		assertEquals(1, n);
		
		List<String> membL = toml.getList("TYPE.Position.CONTAINS");
		assertEquals(2, membL.size());
		assertEquals("int x", membL.get(0));
		
		membL = toml.getList("TYPE.Person.CONTAINS");
		assertEquals(2, membL.size());
		assertEquals("string firstName", membL.get(0));

		for(Entry<String, Object> key: toml.entrySet()) {
			log(key.getKey());
		}
		
		Toml toml2 = toml.getTable("TYPE");
		for(Entry<String, Object> key: toml2.entrySet()) {
			log("2: " + key.getKey());
		}
		
		Toml toml3 = toml.getTable("TYPE.Person");
		Map<String, Object> map = toml3.to(Map.class);
		for(String key: map.keySet()) {
			log("key:" + key);
		}
	}

	//----
	private StringBuilder sb;
	private String buildFile(int scenario) {
		sb = new StringBuilder();
		switch(scenario) {
		case 0:
			add("[TYPE.Position]");
			add("a = 1");
			add("BASE = 'struct'");
			add("CONTAINS = [");
			add("'int x',");
			add("'int y'");
			add("]");
			break;
		case 1:
//			add("[TYPE]");
			add("[TYPE.Position]");
			add("a = 1");
			add("BASE = 'struct'");
			add("CONTAINS = [");
			add("'int x',");
			add("'int y'");
			add("]");
			add("");
			add("[TYPE.Person]");
			add("BASE = 'struct'");
			add("CONTAINS = [");
			add("'string firstName',");
			add("'string lastName'");
			add("]");
			break;
		default:
			break;
		}
		
		return sb.toString();
	}
	private void add(String string) {
		sb.append(string);
		sb.append("\n");
	}

	protected static String fix(String s)
	{
		s = s.replace('\'', '"');
		return s;
	}

}
