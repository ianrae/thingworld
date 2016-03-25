package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.mef.dnal.parser.ParseErrorTracker;

import dnal.DNALLoaderTests.DNALLoader;
import dnal.TypeParserTests.DType;
import dnal.TypeParserTests.DTypeEntry;
import dnal.TypeParserTests.TypeFileScanner;
import dnal.TypeParserTests.TypeLineScanner;
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
		private TypeFileScanner scanner;

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
			
			if (currentSubset != null) {
				log("loader..");
				DNALLoaderTests.DNALLoader loader = new DNALLoader();
				boolean b = loader.load(currentSubset);
				if (! b) {
					state = OTState.ERROR;
				}
			}

			return (state == OTState.INSIDE_DATA) && (errorTracker.areNoErrors());
		}
		
		private OTState doLine(OTState state, String line) {
			Scanner scan = new Scanner(line);

			while(scan.hasNext()) {
				String tok = scan.next();
				log(tok);
				tok = tok.trim();
				switch(state) {
				case WANT_START:
					state = handleStart(tok);
					break;
				case INSIDE_TYPES:
					state = handleInsideTypes(tok);
					break;
				case INSIDE_DATA:
					state = handleInside(tok);
					break;
				default:
					if (tok.startsWith("//")) {
						return state;
					} else {
						errorTracker.addError("unexpected token:" + tok + " state:" + state);
					}
					break;
				}
			}
			
			scan.close();
			return state;
		}


		private OTState handleStart(String tok) {
			if (tok.startsWith("TYPES")) {
				currentSubset = new ArrayList<>();
				return OTState.INSIDE_TYPES;
			}
			return OTState.INSIDE_DATA;
		}
		private OTState handleInsideTypes(String tok) {
			if (tok.startsWith("ENDTYPES")) {
				
				TypeParserTests.TypeFileScanner scanner = new TypeFileScanner();
				boolean b = scanner.scan(currentSubset);
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
//		checkSize(1, scanner.typeL);
////		checkEntrySize(0, scanner.typeL.get(0).entries);
//		checkDType(scanner.typeL.get(0), "int", "Timeout");
	}
//	@Test
//	public void testF2() {
//		List<String> fileL = buildFile(2);
//
//		OverallFileScanner scanner = new OverallFileScanner();
//		boolean b = scanner.scan(fileL);
//		assertEquals(true, b);
//		checkSize(1, scanner.typeL);
////		checkDTypeEntry(scanner.typeL.get(0).entries.get(0), "int", "size");
//		checkDType(scanner.typeL.get(0), "int", "Timeout");
//	}
//	
//	@Test
//	public void testF4() {
//		List<String> fileL = buildFile(4);
//
//		OverallFileScanner scanner = new OverallFileScanner();
//		boolean b = scanner.scan(fileL);
//		assertEquals(true, b);
//		checkSize(1, scanner.typeL);
//		checkEntrySize(2, scanner.typeL.get(0).entries);
//		checkDTypeEntry(scanner.typeL.get(0).entries.get(0), "int", "size");
//		checkDTypeEntry(scanner.typeL.get(0).entries.get(1), "int", "col");
//		checkDType(scanner.typeL.get(0), "int", "Timeout");
//	}
//	@Test
//	public void testF6() {
//		List<String> fileL = buildFile(6);
//
//		OverallFileScanner scanner = new OverallFileScanner();
//		boolean b = scanner.scan(fileL);
//		assertEquals(true, b);
//		checkSize(2, scanner.typeL);
//		checkEntrySize(1, scanner.typeL.get(0).entries);
//		checkDTypeEntry(scanner.typeL.get(0).entries.get(0), "int", "size");
//		checkDType(scanner.typeL.get(0), "int", "Timeout");
//
//		checkEntrySize(1, scanner.typeL.get(1).entries);
//		checkDTypeEntry(scanner.typeL.get(1).entries.get(0), "int", "wid");
//		checkDType(scanner.typeL.get(1), "int", "ZTimeout");
//	}
//	@Test
//	public void testF7() {
//		List<String> fileL = buildFile(7);
//
//		OverallFileScanner scanner = new OverallFileScanner();
//		boolean b = scanner.scan(fileL);
//		assertEquals(true, b);
//		checkSize(1, scanner.typeL);
//		checkDTypeEntry(scanner.typeL.get(0).entries.get(0), "int", "size");
//		checkDType(scanner.typeL.get(0), "int", "Timeout");
//	}
	
	
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
			L.add("");
			L.add("type Timeout extends int");
			L.add(" int size");
			L.add("end");
			L.add("");
			break;
//		case 3:
//			L.add("");
//			L.add("package a.b.c");
//			L.add(" int size: {");
//			L.add(" int wid: 45 }");
////			L.add(" }");
//			L.add("end");
//			L.add("");
//			break;
		case 4:
			L.add("");
			L.add("type Timeout extends int");
			L.add(" int size");
			L.add(" int col");
			L.add("end");
			L.add("");
			break;
//		case 5:
//			L.add("");
//			L.add("package a.b.c");
//			L.add(" int size: {");
//			L.add(" int height: 66 ");
//			L.add(" int wid: 45 }");
////			L.add(" }");
//			L.add("end");
//			L.add("");
//			break;
		case 6:
			L.add("");
			L.add("type Timeout extends int");
			L.add(" int size");
			L.add("end");
			L.add("type ZTimeout extends int");
			L.add(" int wid");
			L.add("end");
			L.add("");
			break;
		case 7:
			L.add("//a comment");
			L.add("type Timeout extends int //another one");
			L.add(" int size //third one");
			L.add("end");
			L.add(""); 
			break;
		}
		return L;
	}
	
}
