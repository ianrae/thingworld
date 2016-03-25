package dnal;

import static org.junit.Assert.*;

import org.junit.Test;

import dnal.OverallParserTests.OverallFileScanner;

public class FinalTests {

	@Test
	public void testPrimitives() {
		String path = "./test/testfiles/final/primitives.dnal";
		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
	}
	@Test
	public void testPrimitivesBad() {
		String path = "./test/testfiles/final/primitivesBad.dnal";
		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.load(path);
		assertEquals(false, b);
		scanner.dumpErrors();
	}

}
