package dnal;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class DNALTests {

	public static class BackingStore {
		private Map<String,String> map = new HashMap<String,String>();

		public BackingStore() {
			map.put("obj1.name", "bob");
			map.put("obj1.age", "30");
			map.put("obj2.name", "bob");
			map.put("obj2.age", "30");
		}

		public Map<String, String> getMap() {
			return map;
		}
	}
	
	public static class Name {
		public Name(String name, String age) {
			super();
			this.name = name;
			this.age = age;
		}
		private final String name;
		private final String age;
		public String getName() {
			return name;
		}
		public String getAge() {
			return age;
		}
	}
	
	public static class NameLoader {
		public BackingStore store;
		
		public Name getName(String objId) {
			String x1 = store.getMap().get(objId + ".name");
			String x2 = store.getMap().get(objId + ".age");
			if (x1 == null || x2 == null) {
				return null;
			}
			Name name = new Name(x1,x2);
			return name;
		}
	}
	
	public static class ValidationException extends Exception {
		public String x;
	}
	
	public static class NameMutator {
		private String name;
		private String age;
		
		public NameMutator() {
		}
		public NameMutator(Name obj) {
			name = obj.getName();
			age = obj.getAge();
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getAge() {
			return age;
		}
		public void setAge(String age) {
			this.age = age;
		}
		
		public boolean isValid() {
			return true;
		}
		
		public Name toImmutable() {
			Name obj = new Name(name, age);
			return obj;
		}
	}

	@Test
	public void test() {
		BackingStore store = new BackingStore();
		NameLoader loader = new NameLoader();
		loader.store = store;
		
		Name name = loader.getName("obj1");
		assertEquals("bob", name.getName());
		assertEquals("30", name.getAge());
		
		Name name2 = loader.getName("nosuchname");
		assertEquals(null, name2);
	}

	@Test
	public void test2() {
		
		BackingStore store = new BackingStore();
		NameLoader loader = new NameLoader();
		loader.store = store;
		Name name = loader.getName("obj1");
		NameMutator mutator = new NameMutator(name);
		
		mutator.setAge("33");
		mutator.setName("bobby");
		
		name = mutator.toImmutable();
		
		assertEquals("bobby", name.getName());
		assertEquals("33", name.getAge());
		
		Name name2 = loader.getName("nosuchname");
		assertEquals(null, name2);
	}
	
	@Test
	public void testValidation() {
		
		BackingStore store = new BackingStore();
		NameLoader loader = new NameLoader();
		loader.store = store;
		Name name = loader.getName("obj1");
		NameMutator mutator = new NameMutator(name);
		
		mutator.setAge("33");
		mutator.setName("bobby");
		
		name = mutator.toImmutable();
		
		assertEquals("bobby", name.getName());
		assertEquals("33", name.getAge());
		
		Name name2 = loader.getName("nosuchname");
		assertEquals(null, name2);
	}
	
}
