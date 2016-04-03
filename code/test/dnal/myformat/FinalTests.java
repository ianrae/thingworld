package dnal.myformat;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.core.IDNALLoader;
import org.mef.dnal.core.IOverallFileScanner;
import org.mef.dnal.core.ITypeFileScanner;
import org.mef.dnal.parser.ParseErrorTracker;

import testhelper.BaseTest;
import dnal.TypeGeneratorTests;
import dnal.TypeGeneratorTests.ITypeGenerator;
import dnal.TypeGeneratorTests.MockTypeGenerator;
import dnal.dio.PositionMutator;
import dnal.myformat.OverallParserTests.OverallFileScanner;

public class FinalTests extends BaseTest {

	@Test
	public void testPrimitives() {
		String path = buildPath("primitives.dnal");
		IOverallFileScanner scanner = createScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.getDloader().getDataL();
		assertEquals(3, dataL.size());
		assertEquals("firstName", dataL.get(1).name);
	}
	@Test
	public void testPrimitivesBad() {
		String path = buildPath("primitivesBad.dnal");
		IOverallFileScanner scanner = createScanner();
		boolean b = scanner.load(path);
		assertEquals(false, b);
		scanner.dumpErrors();
	}
	@Test
	public void testList() {
		String path = buildPath("lists.dnal");
		IOverallFileScanner scanner = createScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.getDloader().getDataL();
		assertEquals(1, dataL.size());
		assertEquals("prov", dataL.get(0).name);
		assertEquals("ont", dataL.get(0).valueList.get(0));
	}
	
	@Test
	public void testSimple() {
		String path = buildPath("simple.dnal");
		IOverallFileScanner scanner = createScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.getDloader().getDataL();
		assertEquals(2, dataL.size());
		assertEquals("time", dataL.get(1).name);
	}
	@Test
	public void testSimpleList() {
		String path = buildPath("simplelist.dnal");
		IOverallFileScanner scanner = createScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.getDloader().getDataL();
		assertEquals(1, dataL.size());
		assertEquals("prov", dataL.get(0).name);
		assertEquals("on", dataL.get(0).valueList.get(0));
	}
	
//was working. fix later!!	
//	@Test
//	public void testSimpleEnum() {
//		String path = buildPath("simpleenum.dnal");
//		IOverallFileScanner scanner = createScanner();
//		boolean b = scanner.load(path);
//		assertEquals(true, b);
//		List<DValue> dataL = scanner.getDloader().getDataL();
//		assertEquals(1, dataL.size());
//		assertEquals("col", dataL.get(0).name);
//		assertEquals("RED", dataL.get(0).finalValue);
//	}

	@Test
	public void testStruct() {
		String path = buildPath("struct.dnal");
		IOverallFileScanner scanner = createScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.getDloader().getDataL();
		assertEquals(1, dataL.size());
		assertEquals("pos", dataL.get(0).name);
		
		//we want to be able to load and validate before doing codegen, so
		//don't have classes yet - use mock-type-gen
//		PositionDIO pos = (PositionDIO) dataL.get(0).finalValue;
//		assertEquals(10, pos.getX());
//		assertEquals(20, pos.getY());
		String fakepos = (String) dataL.get(0).finalValue;
		log(fakepos);
	}
	
	private IOverallFileScanner createScanner() {
		ITypeGenerator gen = createGenerator();
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		IDNALLoader dloader = new DNALLoaderTests.DNALLoader(errorTracker);
		ITypeFileScanner tscanner = new TypeParserTests.TypeFileScanner(errorTracker);
		OverallFileScanner scanner = new OverallParserTests.OverallFileScanner(errorTracker, dloader, gen, tscanner);
		return scanner;
	}

	private ITypeGenerator createGenerator() {
//		ITypeGenerator gen = new TypeGenerator();
		ITypeGenerator gen = new TypeGeneratorTests.MockTypeGenerator();
		gen.register("Position", PositionMutator.class);
		return gen;
	}
	private String buildPath(String filename) {
		return "./test/testfiles/final/" + filename;
	}
}
