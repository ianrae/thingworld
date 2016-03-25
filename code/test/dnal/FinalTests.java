package dnal;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.mef.dnal.core.DValue;

import dnal.OverallParserTests.OverallFileScanner;

public class FinalTests {

	@Test
	public void testPrimitives() {
		String path = "./test/testfiles/final/primitives.dnal";
		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.dloader.getDataL();
		assertEquals(3, dataL.size());
		assertEquals("firstName", dataL.get(1).name);
	}
	@Test
	public void testPrimitivesBad() {
		String path = "./test/testfiles/final/primitivesBad.dnal";
		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.load(path);
		assertEquals(false, b);
		scanner.dumpErrors();
	}
	@Test
	public void testSimple() {
		String path = "./test/testfiles/final/simple.dnal";
		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
		List<DValue> dataL = scanner.dloader.getDataL();
		assertEquals(2, dataL.size());
		assertEquals("time", dataL.get(1).name);
		
	}

}
