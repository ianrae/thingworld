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
	
	public static class DNALAPI {
		
		private DNALLoader loader;
		private List<DValue> dataL;
		private Map<String,DValue> map = new HashMap<>();

		public DNALAPI(DNALLoader loader) {
			this.loader = loader;
			this.dataL = loader.getDataL();
			for(DValue dval: dataL) {
				String key = dval.packageName + "." + dval.name;
				map.put(key, dval);
			}
		}
		
		public String getString(String id) {
			DValue dval = map.get(id);
			if (dval == null) {
				return null;
			}
			return dval.finalValue.toString();
		}
	}

	@Test
	public void test() {
		DNALLoader loader = buildLoader();
		DNALAPI api = new DNALAPI(loader);
		
		String s = api.getString("a.b.c.size");
		assertEquals("100", s);
		s = api.getString("a.b.c.firstName");
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
		return registry;
	}
	

}
