package dnal;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.core.IDNALLoader;
import org.mef.dnal.parser.ParseErrorTracker;

import dnal.DNALLoadValidatorTests.DNALLoadValidator;
import dnal.RegistryTests.RegistryBuilder;
import dnal.RegistryTests.TypeRegistry;
import dnal.myformat.DNALLoaderTests.DNALLoader;

public class APITests {

	public static class DNALException extends Exception {
		public String reason;

		public DNALException(String string) {
			reason = string;
		}
	}
	
	public static class DNALAPI {
		
		private List<DValue> dataL;
		private Map<String,DValue> map = new HashMap<>();

		public DNALAPI(IDNALLoader loader) throws DNALException {
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
		public boolean getBoolean(String id) throws DNALException {
			DValue dval = getVal(id, "boolean");
			return (Boolean)dval.finalValue;
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

//	@Test
//	public void test() throws Exception {
//		DNALLoader loader = buildLoader();
//		DNALAPI api = new DNALAPI(loader);
//		
//		int k = api.getInt("a.b.c.size");
//		assertEquals(100, k);
//		String s = api.getString("a.b.c.firstName");
//		assertEquals("sue mary", s);
//		assertEquals(true, api.getBoolean("a.b.c.flag"));
//	}
	@Test
	public void testBox() {
		Boolean bb = Boolean.TRUE;
		Object obj = bb;
		boolean b = (Boolean)obj;
		assertEquals(true, b);
//
//		obj = null;
//		b = (Boolean)obj;
//		assertEquals(true, b);
	}
	
	private DNALLoader buildLoader() {
		String path = "./test/testfiles/file2.dnal";
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		DNALLoader loader = new DNALLoader(errorTracker);
		boolean b = loader.load(path);
		assertEquals(true, b);
		if (b) {
			DNALLoadValidator loadValidator = new DNALLoadValidator(errorTracker);
			loadValidator.registry = buildRegistry();
			b = loadValidator.validate(loader.getDataL());
		}
		return loader;
	}
	
	private TypeRegistry buildRegistry() {
		RegistryTests.RegistryBuilder builder = new RegistryBuilder();
		return builder.buildRegistry();
	}
}
