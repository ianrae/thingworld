package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.validation.ValidationException;

import dnal.TypeParserTests.DType;
import dnal.dio.PositionDIO;
import dnal.dio.PositionMutator;

public class TypeGeneratorTests {
	
	public static class TypeGenerator {
		private Map<String, Class<?>> map = new HashMap<>();
		
		public void register(String type, Class<?> clazz) {
			map.put(type, clazz);
		}
		
		public Object createObject(DValue dval) {
			Class<?> clazz = map.get(dval.type);
			if (clazz == null) {
				return null;
			}
			
			//!!!!
			if (dval.type.equals("Position")) {
				PositionMutator mutator = null;
				try {
					mutator = (PositionMutator) clazz.newInstance();
				} catch (InstantiationException | IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Integer x = (Integer) findVal(dval, "x");
				Integer y = (Integer) findVal(dval, "y");
				mutator.setX(x);
				mutator.setY(y);
				
				Object dio = null;
				try {
					dio = mutator.toImmutable();
				} catch (ValidationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return dio;
			}
			
			return null;
		}
		
		private Object findVal(DValue dval, String field) {
			for(DValue sub: dval.valueList) {
				if (sub.name.equals(field)) {
					return sub.finalValue;
				}
			}
			return null;
		}
	}

	@Test
	public void test() {
		DValue dval = new DValue();
		dval.name = "pos";
		dval.type = "Position";
		dval.valueList = new ArrayList<>();
		dval.valueList.add(createSub("x", 100));
		dval.valueList.add(createSub("y", 200));
		
		TypeGenerator gen = new TypeGenerator();
		gen.register("Position", PositionMutator.class);
		
		Object obj = gen.createObject(dval);
		PositionDIO pos = (PositionDIO) obj;
		assertEquals(100, pos.getX());
		assertEquals(200, pos.getY());
	}
	
	private DValue createSub(String field, int n) {
		DValue dval = new DValue();
		dval.name = field;
		dval.type = "int";
		dval.finalValue = Integer.valueOf(n);
		return dval;

	}

}
