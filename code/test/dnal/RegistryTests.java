package dnal;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import dnal.TypeTests.ITypeValidator;
import dnal.TypeTests.MockIntValidator;

public class RegistryTests {
	
	public static class TypeRegistry {
		private Map<String,ITypeValidator> map = new HashMap<>();
		
		public void add(String type, ITypeValidator validator) {
			map.put(type, validator);
		}

		public ITypeValidator find(String type) {
			return map.get(type);
		}
	}
	
	public static class RegistryBuilder {
		
		public TypeRegistry buildRegistry() {
			TypeRegistry registry = new TypeRegistry();
			registry.add("int", new MockIntValidator());
			registry.add("string", new TypeTests.MockStringValidator());
			registry.add("boolean", new TypeTests.MockBooleanValidator());
			return registry;
		}
	}

	@Test
	public void test() {
	}

}
