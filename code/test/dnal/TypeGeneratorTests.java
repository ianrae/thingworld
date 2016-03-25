package dnal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mef.dnal.core.DValue;

import dnal.dio.PositionDIO;
import dnal.dio.PositionMutator;

public class TypeGeneratorTests {
	
	public interface ITypeGenerator {
		void register(String type, Class<?> clazz);
		public Object createImmutableObject(DValue dval);
	}
	
	public static class MockTypeGenerator implements ITypeGenerator {

		@Override
		public void register(String type, Class<?> clazz) {
		}

		@Override
		public Object createImmutableObject(DValue dval) {
			return "MARKER:" + dval.type;
		}
		
	}

	public static class TypeGenerator implements ITypeGenerator{
		private Map<String, Class<?>> map = new HashMap<>();

		@Override
		public void register(String type, Class<?> clazz) {
			map.put(type, clazz);
		}

		@Override
		public Object createImmutableObject(DValue dval) {
			Class<?> clazz = map.get(dval.type);
			if (clazz == null) {
				return null;
			}

			PositionMutator mutator = null;
			try {
				mutator = (PositionMutator) clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			return mutator.createFromDValue(dval);
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

		Object obj = gen.createImmutableObject(dval);
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
