package dnal.toml;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.moandjiezana.toml.Toml;

public class TomlTests {

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
		
		List<String> membL = toml.getList("TYPE.Position.MEMBERS");
		assertEquals(2, membL.size());
		assertEquals("int x", membL.get(0));
	}

	//----
	private StringBuilder sb;
	private String buildFile(int scenario) {
		sb = new StringBuilder();
		switch(scenario) {
		case 0:
			add("[TYPE.Position]");
			add("a = 1");
			add("MEMBERS = [");
			add("'int x',");
			add("'int y'");
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
