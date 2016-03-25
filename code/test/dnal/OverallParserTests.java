package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.mef.dnal.parser.ParseErrorTracker;

import dnal.DNALLoaderTests.DNALLoader;
import dnal.RegistryTests.RegistryBuilder;
import dnal.TypeParserTests.DType;
import dnal.TypeParserTests.DTypeEntry;
import dnal.TypeParserTests.TypeFileScanner;
import testhelper.BaseTest;


public class OverallParserTests extends BaseTest {

	public static enum OTState {
		WANT_START,
		INSIDE_TYPES,
		INSIDE_DATA,
		ERROR
	}

	public static class OverallFileScanner {
		private ParseErrorTracker errorTracker = new ParseErrorTracker();
		private int lineNum;
		private List<String> currentSubset;
		public TypeFileScanner tscanner;
		public DNALLoader dloader;

		public boolean scan(List<String> fileL) {
			OTState state = OTState.WANT_START;

			lineNum = 0;
			for(String line : fileL) {
				log(String.format("[%s] line%d: %s",  state.toString(), lineNum++, line));

				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}

				if (line.startsWith("//")) {
					continue;
				}

				state = doLine(state, line);
			}

			if (currentSubset != null && currentSubset.size() > 0) {
				log("loader..");
				dloader = new DNALLoader();
				RegistryTests.RegistryBuilder builder = new RegistryBuilder();
				dloader.registry = builder.buildRegistry();
				boolean b = dloader.load(currentSubset);
				if (! b) {
					state = OTState.ERROR;
				}
			}

			return (state == OTState.INSIDE_DATA) && (errorTracker.areNoErrors());
		}

		private OTState doLine(OTState state, String line) {
			switch(state) {
			case WANT_START:
				state = handleStart(line);
				break;
			case INSIDE_TYPES:
				state = handleInsideTypes(line);
				break;
			case INSIDE_DATA:
				state = handleInside(line);
				break;
			default:
				if (line.startsWith("//")) {
					return state;
				} else {
					errorTracker.addError("unexpected token:" + line + " state:" + state);
				}
				break;
			}
			return state;
		}


		private OTState handleStart(String tok) {
			if (tok.startsWith("TYPES")) {
				currentSubset = new ArrayList<>();
				return OTState.INSIDE_TYPES;
			}
			currentSubset = new ArrayList<>();
			currentSubset.add(tok);
			return OTState.INSIDE_DATA;
		}
		private OTState handleInsideTypes(String tok) {
			if (tok.startsWith("ENDTYPES")) {

				tscanner = new TypeFileScanner();
				boolean b = tscanner.scan(currentSubset);
				currentSubset = new ArrayList<>();
				if (! b) {
					return OTState.ERROR;
				}
				return OTState.INSIDE_DATA;
			}
			currentSubset.add(tok);
			return OTState.INSIDE_TYPES;
		}
		private OTState handleInside(String tok) {
			if (tok.startsWith("//")) {
				return OTState.INSIDE_DATA;
			}
			currentSubset.add(tok);

			return OTState.INSIDE_DATA;
		}

		private void log(String s) {
			System.out.println(s);
		}
	}

	@Test
	public void testF0() {
		List<String> fileL = buildFile(0);

		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(false, b);
	}

	@Test
	public void testF1() {
		List<String> fileL = buildFile(1);

		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.tscanner.typeL);
		checkEntrySize(0, scanner.tscanner.typeL.get(0).entries);
		checkDType(scanner.tscanner.typeL.get(0), "int", "Timeout");
		assertEquals(null, scanner.dloader);
	}
	@Test
	public void testF2() {
		List<String> fileL = buildFile(2);

		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.tscanner.typeL);
		checkEntrySize(0, scanner.tscanner.typeL.get(0).entries);
		checkDType(scanner.tscanner.typeL.get(0), "int", "Timeout");
		
		assertEquals(1, scanner.dloader.getDataL().size());
		assertEquals("size", scanner.dloader.getDataL().get(0).name);
	}
	@Test
	public void testF3() {
		List<String> fileL = buildFile(3);

		OverallFileScanner scanner = new OverallFileScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		assertEquals(null, scanner.tscanner);
//		checkSize(1, scanner.tscanner.typeL);
//		checkEntrySize(0, scanner.tscanner.typeL.get(0).entries);
//		checkDType(scanner.tscanner.typeL.get(0), "int", "Timeout");
		
		assertEquals(1, scanner.dloader.getDataL().size());
		assertEquals("size", scanner.dloader.getDataL().get(0).name);
	}

	private void checkSize(int expectedSize, List<DType> list) {
		assertEquals(expectedSize, list.size());
	}
	private void checkEntrySize(int expectedSize, List<DTypeEntry> list) {
		assertEquals(expectedSize, list.size());
	}
	private void checkPackage(DType dValue, String string) {
		assertEquals(string, dValue.packageName);
	}
	private void checkDType(DType dtype, String type, String name) {
		assertEquals(type, dtype.baseType);
		assertEquals(name, dtype.name);
	}


	private List<String> buildFile(int scenario) {
		List<String> L = new ArrayList<>();
		switch(scenario) {
		case 0:
			L.add("");
			break;
		case 1:
			L.add("TYPES");
			L.add("type Timeout extends int");
			L.add(" ");
			L.add("end");
			L.add("ENDTYPES");
			break;
		case 2:
			L.add("TYPES");
			L.add("type Timeout extends int");
			L.add(" ");
			L.add("end");
			L.add("ENDTYPES");
			L.add("");
			L.add("package a.b.c");
			L.add(" int size: 45");
			L.add("end");
			L.add("");
			break;
		case 3:
			L.add("");
			L.add("package a.b.c");
			L.add(" int size: 45");
			L.add("end");
			L.add("");
			break;
		}
		return L;
	}

}
