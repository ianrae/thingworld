package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import testhelper.BaseTest;

public class DNALTests extends BaseTest {

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
	
	public static class ValidationError {
		public String fieldName;
		public String error;
	}
	
	public static class ValidationException extends Exception {
		public List<ValidationError> errors = new ArrayList<>();

		public ValidationException(List<ValidationError> errors) {
			this.errors = errors;
		}

		public void dump() {
			for(ValidationError err: errors) {
				String s = String.format("field %s - %s", err.fieldName, err.error);
				System.out.println(s);
			}
		}
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
			List<ValidationError> errors = validate();
			return (errors.size() == 0);
		}
		
		public List<ValidationError> validate() {
			List<ValidationError> errors = new ArrayList<>();
			int nAge = Integer.parseInt(age);
			if (nAge > 100) {
				ValidationError error = new ValidationError();
				error.fieldName = "age";
				error.error = "out of range";
				errors.add(error);
			}
			return errors;
		}
		
		public Name toImmutable() throws ValidationException {
			List<ValidationError> errors = validate();
			if (errors.size() > 0) {
				throw new ValidationException(errors);
			}
			Name obj = new Name(name, age);
			return obj;
		}
	}

	@Test
	public void test() {
		NameLoader loader = createLoader();
		Name name = loader.getName("obj1");
		assertEquals("bob", name.getName());
		assertEquals("30", name.getAge());
		
		Name name2 = loader.getName("nosuchname");
		assertEquals(null, name2);
	}

	@Test
	public void test2() {
		NameLoader loader = createLoader();
		Name name = loader.getName("obj1");
		NameMutator mutator = new NameMutator(name);
		
		mutator.setAge("33");
		mutator.setName("bobby");
		
		name = null;
		try {
			name = mutator.toImmutable();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals("bobby", name.getName());
		assertEquals("33", name.getAge());
		
		Name name2 = loader.getName("nosuchname");
		assertEquals(null, name2);
	}
	
	@Test
	public void testValidation() {
		NameLoader loader = createLoader();
		Name name = loader.getName("obj1");
		NameMutator mutator = new NameMutator(name);
		
		mutator.setAge("133");
		mutator.setName("bobby");
		
		assertEquals(false, mutator.isValid());
		
		name = null;
		try {
			name = mutator.toImmutable();
		} catch (ValidationException e) {
			e.dump();
		}
		assertEquals(null, name);
		
	}
	
	//--helpers
	private NameLoader createLoader() {
		BackingStore store = new BackingStore();
		NameLoader loader = new NameLoader();
		loader.store = store;
		return loader;
	}
	
}
