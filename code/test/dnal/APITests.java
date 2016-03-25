package dnal;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dnal.DNALLoaderTests.DNALLoader;
import dnal.DNALLoaderTests.TypeRegistry;
import dnal.TypeTests.MockIntValidator;

import org.junit.Test;
import org.mef.dnal.core.DValue;

public class APITests {

	public static class DNALException extends Exception {
		public String reason;

		public DNALException(String string) {
			reason = string;
		}
	}
	
	public static class DNALAPI {
		
		private DNALLoader loader;
		private List<DValue> dataL;
		private Map<String,DValue> map = new HashMap<>();

		public DNALAPI(DNALLoader loader) throws DNALException {
			this.loader = loader;
			if (! loader.isValid()) {
				throw new DNALException("loader has invalid data");
			}
			
			this.dataL = loader.getDataL();
			for(DValue dval: dataL) {
				String key = dval.packageName + "." + dval.name;
				map.put(key, dval);
			}
		}
		
		public String getString(String id) throws DNALException {
			DValue dval = getVal(id, "string");
			return dval.finalValue.toString();
		}
		public int getInt(String id) throws DNALException {
			DValue dval = getVal(id, "int");
			return (Integer)dval.finalValue;
		}
		private DValue getVal(String id, String type) throws DNALException {
			DValue dval = map.get(id);
			if (dval == null) {
				throw new DNALException("can't find property: " + id);
			} else if (! type.equals(dval.type)) {
				throw new DNALException(String.format("property '%s' is type %s, not %s", id, dval.type, type));
			}
			return dval;
		}
	}

	@Test
	public void test() throws Exception {
		DNALLoader loader = buildLoader();
		DNALAPI api = new DNALAPI(loader);
		
		int k = api.getInt("a.b.c.size");
		assertEquals(100, k);
		String s = api.getString("a.b.c.firstName");
		assertEquals("sue", s);
	}
	
	private DNALLoader buildLoader() {
		String path = "./test/testfiles/file2.dnal";
		DNALLoader loader = new DNALLoader();
		loader.registry = buildRegistry();
		boolean b = loader.load(path);
		assertEquals(true, b);
		return loader;
	}
	
	private TypeRegistry buildRegistry() {
		TypeRegistry registry = new TypeRegistry();
		registry.add("int", new MockIntValidator());
		registry.add("string", new TypeTests.MockStringValidator());
		return registry;
	}
}
