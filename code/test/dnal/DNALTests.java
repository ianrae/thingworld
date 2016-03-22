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
			map.put("city1.name", "halifax");
			map.put("city1.age", "30");
		}

		public Map<String, String> getMap() {
			return map;
		}
	}
	
	public static class Person {
		public Person(String name, String age) {
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
	
	public interface ILoader<T> {
		T getXObj(String objId);
	}
	
	public static class PersonLoader implements ILoader<Person>{
		public BackingStore store;
		
		public Person getXObj(String objId) {
			String x1 = store.getMap().get(objId + ".name");
			String x2 = store.getMap().get(objId + ".age");
			if (x1 == null || x2 == null) {
				return null;
			}
			Person name = new Person(x1,x2);
			return name;
		}
	}
	
	public static class City {
		public City(String name, String age) {
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
	
	public static class CityLoader implements ILoader<City>{
		public BackingStore store;
		
		public City getXObj(String objId) {
			String x1 = store.getMap().get(objId + ".name");
			String x2 = store.getMap().get(objId + ".age");
			if (x1 == null || x2 == null) {
				return null;
			}
			City name = new City(x1,x2);
			return name;
		}
	}
	
	
	public static class API {
		public ILoader<Person> nameLoader;
		public ILoader<City> cityLoader;
		
		public Person getPerson(String objId) {
			return nameLoader.getXObj(objId);
		}
		
		public <T> T getObject(String objId) {
			if (objId.startsWith("city")) {
				return (T) cityLoader.getXObj(objId);
			}
			return (T) nameLoader.getXObj(objId);
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
	
	public static class PersonMutator {
		private String name;
		private String age;
		
		public PersonMutator() {
		}
		public PersonMutator(Person obj) {
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
		
		public Person toImmutable() throws ValidationException {
			List<ValidationError> errors = validate();
			if (errors.size() > 0) {
				throw new ValidationException(errors);
			}
			Person obj = new Person(name, age);
			return obj;
		}
	}

	@Test
	public void test() {
		PersonLoader loader = createLoader();
		Person person = loader.getXObj("obj1");
		assertEquals("bob", person.getName());
		assertEquals("30", person.getAge());
		
		Person person2 = loader.getXObj("nosuchname");
		assertEquals(null, person2);
	}

	@Test
	public void test2() {
		PersonLoader loader = createLoader();
		Person person = loader.getXObj("obj1");
		PersonMutator mutator = new PersonMutator(person);
		
		mutator.setAge("33");
		mutator.setName("bobby");
		
		person = getNameObj(mutator);
		assertEquals("bobby", person.getName());
		assertEquals("33", person.getAge());
		
		Person person2 = loader.getXObj("nosuchname");
		assertEquals(null, person2);
	}
	
	@Test
	public void testValidation() {
		PersonLoader loader = createLoader();
		Person person = loader.getXObj("obj1");
		PersonMutator mutator = new PersonMutator(person);
		
		mutator.setAge("133");
		mutator.setName("bobby");
		
		assertEquals(false, mutator.isValid());
		
		person = getNameObj(mutator);
		assertEquals(null, person);
	}
	
	@Test
	public void testAPI() {
		API api = createAPI();
		
		Person person = api.getPerson("obj1");
		assertEquals("bob", person.getName());
		assertEquals("30", person.getAge());
		
		Person person2 = api.getPerson("nosuchname");
		assertEquals(null, person2);
		
		person = api.getObject("obj1");
		assertEquals("bob", person.getName());
		assertEquals("30", person.getAge());
		
		City city = api.getObject("city1");
		assertEquals("halifax", city.getName());
		assertEquals("30", city.getAge());
	}

	
	
	//--helpers
	private PersonLoader createLoader() {
		BackingStore store = new BackingStore();
		PersonLoader loader = new PersonLoader();
		loader.store = store;
		return loader;
	}
	
	private API createAPI() {
		BackingStore store = new BackingStore();
		PersonLoader loader = new PersonLoader();
		loader.store = store;
		API api = new API();
		api.nameLoader = loader;
		
		CityLoader cityLoader = new CityLoader();
		cityLoader.store = store;
		api.cityLoader = cityLoader;
		
		return api;
	}
	
	private Person getNameObj(PersonMutator mutator) {
		Person name = null;
		try {
			name = mutator.toImmutable();
		} catch (ValidationException e) {
			e.dump();
		}
		return name;
	}
	
}
