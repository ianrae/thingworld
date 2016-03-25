package dnal;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.mef.dnal.core.DValue;

import testhelper.BaseTest;
import dnal.OverallParserTests.OverallFileScanner;

public class FinalTests extends BaseTest {

	@Test
	public void testPrimitives() {
		String path = buildPath("primitives.dnal");
		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.dloader.getDataL();
		assertEquals(3, dataL.size());
		assertEquals("firstName", dataL.get(1).name);
	}
	@Test
	public void testPrimitivesBad() {
		String path = buildPath("primitivesBad.dnal");
		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.load(path);
		assertEquals(false, b);
		scanner.dumpErrors();
	}
	@Test
	public void testSimple() {
		String path = buildPath("simple.dnal");
		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.dloader.getDataL();
		assertEquals(2, dataL.size());
		assertEquals("time", dataL.get(1).name);
	}

	@Test
	public void testStruct() {
		String path = buildPath("struct.dnal");
		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.dloader.getDataL();
		assertEquals(1, dataL.size());
		assertEquals("pos", dataL.get(0).name);
		log(dataL.get(0).finalValue.toString()); //!! remove later
	}
	
	private String buildPath(String filename) {
		return "./test/testfiles/final/" + filename;
	}
}
