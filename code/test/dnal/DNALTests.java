package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import testhelper.BaseTest;

public class DNALTests extends BaseTest {

	public interface IBackingStore {
		String getStringValue(String objId);
		Integer getIntegerValue(String objId);
		//add validateAfterLoad -- need to validate all objects in the store
	}
	public static class BackingStore implements IBackingStore {
		private Map<String,String> map = new HashMap<String,String>();

		public BackingStore() {
			map.put("obj1.name", "bob");
			map.put("obj1.age", "30");
			map.put("obj2.name", "bob");
			map.put("obj2.age", "30");
			map.put("city1.name", "halifax");
			map.put("city1.age", "30");
			map.put("integer1.value", "10");
		}

		@Override
		public String getStringValue(String objId) {
			return map.get(objId);
		}
		@Override
		public Integer getIntegerValue(String objId) {
			String s = getStringValue(objId);
			return new Integer(s);
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
		public IBackingStore store;

		public Person getXObj(String objId) {
			String x1 = store.getStringValue(objId + ".name");
			String x2 = store.getStringValue(objId + ".age");
			if (x1 == null || x2 == null) {
				return null;
			}
			Person name = new Person(x1,x2);
			return name;
		}
	}

	public static class City {
		private final String name;
		private final Integer age;
		
		public City(String name, Integer age) {
			this.name = name;
			this.age = age;
		}
		public String getName() {
			return name;
		}
		public Integer getAge() {
			return age;
		}
	}

	public static class CityLoader implements ILoader<City>{
		public IBackingStore store;

		public City getXObj(String objId) {
			String x1 = store.getStringValue(objId + ".name");
			Integer x2 = store.getIntegerValue(objId + ".age");
			if (x1 == null || x2 == null) {
				return null;
			}
			City name = new City(x1,x2);
			return name;
		}
	}
	public static class IntegerLoader implements ILoader<Integer>{
		public IBackingStore store;

		public Integer getXObj(String objId) {
			String x1 = store.getStringValue(objId + ".value");
			if (x1 == null) {
				return null;
			}
			Integer name = new Integer(x1);
			return name;
		}
	}

	public interface ILoaderRegistry {
		Object getObject(String objId);
	}
	
	public static class LoaderRegistry implements ILoaderRegistry {
		public ILoader<Person> nameLoader;
		public ILoader<City> cityLoader;
		public ILoader<Integer> integerLoader;

		@Override
		public Object getObject(String objId) {
			if (objId.startsWith("city")) {
				return cityLoader.getXObj(objId);
			}
			else if (objId.startsWith("integer")) {
				return integerLoader.getXObj(objId);
			}
			return nameLoader.getXObj(objId);
		}
	}


	public static class API {
		public LoaderRegistry registry;
		
//		public Person getPerson(String objId) {
//			return registry.nameLoader.getXObj(objId);
//		}

		public <T> T getObject(String objId) {
			return (T) registry.getObject(objId);
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
	
	public static abstract class ValidationBase {
		protected String fieldName;
		protected String value;
		
		public ValidationBase(String fieldName, String value) {
			this.fieldName = fieldName;
			this.value = value;
		}
		
		public abstract void validate(List<ValidationError> list);

		protected void addError(List<ValidationError> errors, String fieldName, String err) {
			ValidationError error = new ValidationError();
			error.fieldName = fieldName;
			error.error = err;
			errors.add(error);
		}
	}
	public static class IntRangeValidation extends ValidationBase {
		public IntRangeValidation(String fieldName, String value) {
			super(fieldName, value);
		}
		public IntRangeValidation(String fieldName, Integer value) {
			super(fieldName, value.toString());
		}

		public void validate(List<ValidationError> list) {
			int nAge = Integer.parseInt(value);
			if (nAge > 100) {
				this.addError(list, fieldName, "out of range");
			}
		}
	}

	public static abstract class MutatorBase<T> {

		public boolean isValid() {
			List<ValidationError> errors = validate();
			return (errors.size() == 0);
		}

		public List<ValidationError> validate() {
			List<ValidationError> errors = new ArrayList<>();
			List<ValidationBase> validators = new ArrayList<>();
			addValidators(validators);
			
			for(ValidationBase validator: validators) {
				validator.validate(errors);
			}
			return errors;
		}

		protected abstract void addValidators(List<ValidationBase> validators);

		public T toImmutable() throws ValidationException {
			List<ValidationError> errors = validate();
			if (errors.size() > 0) {
				throw new ValidationException(errors);
			}
			return createObject();
		}
		
		protected abstract T createObject();
	}

	public static class PersonMutator extends MutatorBase<Person> {
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

		@Override
		protected void addValidators(List<ValidationBase> validators) {
			validators.add(new IntRangeValidation("age", age));
		}

		@Override
		protected Person createObject() {
			Person obj = new Person(name, age);
			return obj;
		}
	}
	public static class CityMutator extends MutatorBase<City> {
		private String name;
		private Integer age;

		public CityMutator() {
		}
		public CityMutator(City obj) {
			name = obj.getName();
			age = obj.getAge();
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		@Override
		protected void addValidators(List<ValidationBase> validators) {
			validators.add(new IntRangeValidation("age", age));
		}

		@Override
		protected City createObject() {
			City city = new City(name, age);
			return city;
		}
		public Integer getAge() {
			return age;
		}
		public void setAge(Integer age) {
			this.age = age;
		}
	}

	public static class IntegerMutator extends MutatorBase<Integer> {
		private String value;

		public IntegerMutator() {
		}
		public IntegerMutator(Integer obj) {
			value = obj.toString();
		}
		public String getValue() {
			return value;
		}
		public void setValue(String name) {
			this.value = name;
		}

		@Override
		protected void addValidators(List<ValidationBase> validators) {
			validators.add(new IntRangeValidation("value", value));
		}

		@Override
		protected Integer createObject() {
			Integer val = new Integer(value);
			return val;
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
	public void test3() {
		API api = createAPI();
		Integer n = api.getObject("integer1");
		assertEquals(10, n.intValue());
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
		Person person = api.getObject("obj1");
		assertEquals("bob", person.getName());
		assertEquals("30", person.getAge());

		Person person2 = api.getObject("nosuchname");
		assertEquals(null, person2);

		person = api.getObject("obj1");
		assertEquals("bob", person.getName());
		assertEquals("30", person.getAge());

		City city = api.getObject("city1");
		assertEquals("halifax", city.getName());
		assertEquals(30, city.getAge().intValue());
	}



	//--helpers
	private PersonLoader createLoader() {
		IBackingStore store = new BackingStore();
		PersonLoader loader = new PersonLoader();
		loader.store = store;
		return loader;
	}

	private API createAPI() {
		IBackingStore store = new BackingStore();
		PersonLoader loader = new PersonLoader();
		loader.store = store;
		API api = new API();
		api.registry = new LoaderRegistry();
		api.registry.nameLoader = loader;

		CityLoader cityLoader = new CityLoader();
		cityLoader.store = store;
		api.registry.cityLoader = cityLoader;

		IntegerLoader integerLoader = new IntegerLoader();
		integerLoader.store = store;
		api.registry.integerLoader = integerLoader;
		
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
