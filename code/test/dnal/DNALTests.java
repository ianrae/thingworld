package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mef.dnal.validation.ValidationBase;
import org.mef.dnal.validation.ValidationError;
import org.mef.dnal.validation.ValidationException;

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
			map.put("obj2.name", "sue");
			map.put("obj2.age", "30");
			map.put("city1.name", "halifax");
			map.put("city1.age", "30");
			map.put("city1.personREF", "obj2");
			map.put("integer1.value", "10");
			map.put("employee1.name", "rich");
			map.put("employee1.age", "30");
			map.put("employee1.code", "xyz");
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
	
	public static class Employee extends Person {
		public Employee(String name, String age, String code) {
			super(name, age);
			this.code = code;
		}
		private final String code;
		
		public String getCode() {
			return code;
		}
	}

	public interface ILoaderRegistry {
		Object getObject(String objId);
	}
	

	public interface ILoader<T> {
		T getXObj(String objId, ILoaderRegistry registry);
	}

	public static class PersonLoader implements ILoader<Person>{
		public IBackingStore store;

		public Person getXObj(String objId, ILoaderRegistry registry) {
			String x1 = store.getStringValue(objId + ".name");
			String x2 = store.getStringValue(objId + ".age");
			if (x1 == null || x2 == null) {
				return null;
			}
			Person name = new Person(x1,x2);
			return name;
		}
	}
	public static class EmployeeLoader implements ILoader<Employee>{
		public IBackingStore store;

		public Employee getXObj(String objId, ILoaderRegistry registry) {
			String x1 = store.getStringValue(objId + ".name");
			String x2 = store.getStringValue(objId + ".age");
			String x3 = store.getStringValue(objId + ".code");
			if (x1 == null || x2 == null || x3 == null) {
				return null;
			}
			Employee name = new Employee(x1,x2,x3);
			return name;
		}
	}

	public static class City {
		private final String name;
		private final Integer age;
		private final Person person;
		
		public City(String name, Integer age, Person person) {
			this.name = name;
			this.age = age;
			this.person = person;
		}
		public String getName() {
			return name;
		}
		public Integer getAge() {
			return age;
		}
		public Person getPerson() {
			return person;
		}
	}

	public static class CityLoader implements ILoader<City>{
		public IBackingStore store;

		public City getXObj(String objId, ILoaderRegistry registry) {
			String x1 = store.getStringValue(objId + ".name");
			Integer x2 = store.getIntegerValue(objId + ".age");
			
			String subId = store.getStringValue(objId + ".personREF");
			Person x3 = (Person) registry.getObject(subId);
			
			if (x1 == null || x2 == null || x3 == null) {
				return null;
			}
			City name = new City(x1,x2, x3);
			return name;
		}
	}
	public static class IntegerLoader implements ILoader<Integer>{
		public IBackingStore store;

		public Integer getXObj(String objId, ILoaderRegistry registry) {
			String x1 = store.getStringValue(objId + ".value");
			if (x1 == null) {
				return null;
			}
			Integer name = new Integer(x1);
			return name;
		}
	}

	public static class LoaderRegistry implements ILoaderRegistry {
		public ILoader<Person> nameLoader;
		public ILoader<City> cityLoader;
		public ILoader<Integer> integerLoader;
		public ILoader<Employee> employeeLoader;

		@Override
		public Object getObject(String objId) {
			if (objId.startsWith("city")) {
				return cityLoader.getXObj(objId, this);
			} else if (objId.startsWith("integer")) {
				return integerLoader.getXObj(objId, this);
			} else if (objId.startsWith("employee")) {
				return employeeLoader.getXObj(objId, this);
			}
			return nameLoader.getXObj(objId, this);
		}
	}


	public static class API {
		public ILoaderRegistry registry;
		
		public <T> T getObject(String objId) {
			return (T) registry.getObject(objId);
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
		private Person person;

		public CityMutator() {
		}
		public CityMutator(City obj) {
			name = obj.getName();
			age = obj.getAge();
			person = obj.getPerson();
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
			City city = new City(name, age, person);
			return city;
		}
		public Integer getAge() {
			return age;
		}
		public void setAge(Integer age) {
			this.age = age;
		}
		public Person getPerson() {
			return person;
		}
		public void setPerson(Person person) {
			this.person = person;
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
		Person person = loader.getXObj("obj1", null);
		assertEquals("bob", person.getName());
		assertEquals("30", person.getAge());

		Person person2 = loader.getXObj("nosuchname", null);
		assertEquals(null, person2);
	}

	@Test
	public void test2() {
		PersonLoader loader = createLoader();
		Person person = loader.getXObj("obj1", null);
		PersonMutator mutator = new PersonMutator(person);

		mutator.setAge("33");
		mutator.setName("bobby");

		person = getNameObj(mutator);
		assertEquals("bobby", person.getName());
		assertEquals("33", person.getAge());

		Person person2 = loader.getXObj("nosuchname", null);
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
		Person person = loader.getXObj("obj1", null);
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
		assertEquals("sue", city.getPerson().getName());
		
		Employee emp = api.getObject("employee1");
		assertEquals("xyz", emp.getCode());
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
		LoaderRegistry registry = new LoaderRegistry();
		api.registry = registry;
		registry.nameLoader = loader;

		CityLoader cityLoader = new CityLoader();
		cityLoader.store = store;
		registry.cityLoader = cityLoader;

		IntegerLoader integerLoader = new IntegerLoader();
		integerLoader.store = store;
		registry.integerLoader = integerLoader;
		
		EmployeeLoader empLoader = new EmployeeLoader();
		empLoader.store = store;
		registry.employeeLoader = empLoader;
		
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
