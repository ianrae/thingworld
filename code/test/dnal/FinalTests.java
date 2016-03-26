package dnal;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.mef.dnal.core.DValue;

import testhelper.BaseTest;
import dnal.OverallParserTests.OverallFileScanner;
import dnal.TypeGeneratorTests.ITypeGenerator;
import dnal.TypeGeneratorTests.TypeGenerator;
import dnal.dio.PositionDIO;
import dnal.dio.PositionMutator;

public class FinalTests extends BaseTest {

	@Test
	public void testPrimitives() {
		String path = buildPath("primitives.dnal");
		ITypeGenerator gen = createGenerator();
		OverallFileScanner scanner = new OverallFileScanner(gen);
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.dloader.getDataL();
		assertEquals(3, dataL.size());
		assertEquals("firstName", dataL.get(1).name);
	}
	@Test
	public void testPrimitivesBad() {
		String path = buildPath("primitivesBad.dnal");
		ITypeGenerator gen = createGenerator();
		OverallFileScanner scanner = new OverallFileScanner(gen);
		boolean b = scanner.load(path);
		assertEquals(false, b);
		scanner.dumpErrors();
	}
	@Test
	public void testList() {
		String path = buildPath("lists.dnal");
		ITypeGenerator gen = createGenerator();
		OverallFileScanner scanner = new OverallFileScanner(gen);
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.dloader.getDataL();
		assertEquals(1, dataL.size());
		assertEquals("prov", dataL.get(0).name);
		assertEquals("ont", dataL.get(0).tmplist.get(0));
	}
	
	@Test
	public void testSimple() {
		String path = buildPath("simple.dnal");
		ITypeGenerator gen = createGenerator();
		OverallFileScanner scanner = new OverallFileScanner(gen);
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.dloader.getDataL();
		assertEquals(2, dataL.size());
		assertEquals("time", dataL.get(1).name);
	}
	@Test
	public void testSimpleList() {
		String path = buildPath("simplelist.dnal");
		ITypeGenerator gen = createGenerator();
		OverallFileScanner scanner = new OverallFileScanner(gen);
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.dloader.getDataL();
		assertEquals(1, dataL.size());
		assertEquals("prov", dataL.get(0).name);
		assertEquals("on", dataL.get(0).tmplist.get(0));
	}

	@Test
	public void testStruct() {
		String path = buildPath("struct.dnal");
		ITypeGenerator gen = createGenerator();
		OverallFileScanner scanner = new OverallFileScanner(gen);
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.dloader.getDataL();
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
