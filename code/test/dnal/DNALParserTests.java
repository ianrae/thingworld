package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.parser.JSONStringParser;
import org.mef.dnal.parser.LSState;
import org.mef.dnal.parser.ParseErrorTracker;

import testhelper.BaseTest;


public class DNALParserTests extends BaseTest {

	public static class LineScanner {
		private DValue currentDValue;
		private DValue finalDvalue;
		private ParseErrorTracker errorTracker = new ParseErrorTracker();
		private boolean continueFlag;
		private String packageName;

		public LineScanner(String packageName, ParseErrorTracker errorTracker2) {
			this.packageName = packageName;
			this.errorTracker = errorTracker2;
		}
		public LineScanner(String packageName, DValue dval, ParseErrorTracker errorTracker2) {
			this.packageName = packageName;
			this.finalDvalue = dval;
			this.errorTracker = errorTracker2;
			continueFlag = true;
		}

		public boolean scan(String line) {
			LSState state = LSState.WANT_TYPE;
			if (continueFlag) {
				state = handleType(null);
			}
			Scanner scan = new Scanner(line);

			while(scan.hasNext()) {
				String tok = scan.next();
				log(tok);
				tok = tok.trim();
				switch(state) {
				case WANT_TYPE:
					state = handleType(tok);
					break;
				case WANT_NAME:
					state = handleName(tok);
					break;
				case WANT_VAL:
					state = handleVal(tok, line);
					break;
				case LIST:
					state = handleList(tok);
					break;
				case END:
					state = handleEnd(tok);
					break;
				default:
					errorTracker.addError("unexpected token:" + tok + " state:" + state);
					break;
				}
				
				if (state == LSState.NO_MORE) {
					break; //we're done
				}
			}
			scan.close();

			return (state == LSState.END || state == LSState.PARTIAL || state == LSState.NO_MORE) && (errorTracker.areNoErrors());
		}

		private LSState handleList(String tok) {
			if (tok.equals("]")) {
				finalDvalue = currentDValue;
				return LSState.END;
			}
			
			if (! tok.startsWith("\"")) {
				errorTracker.addError("list elements must use quotes: " + tok);
				return LSState.ERROR;
			}

			JSONStringParser jparser = new JSONStringParser();
			String s = jparser.findJSONString(tok, 0);
			
			currentDValue.tmplist.add(s);
			return LSState.LIST;
		}
		public DValue getDValue() {
			if (errorTracker.hasErrors()) {
				return null;
			}
			return finalDvalue;
		}

		private LSState handleType(String tok) {
			currentDValue = new DValue();
			currentDValue.type = tok;
			currentDValue.packageName = this.packageName;
			return LSState.WANT_NAME;
		}
		private LSState handleName(String tok) {
			if (! tok.endsWith(":")) {
				errorTracker.addError("missing ':'");
				return LSState.ERROR;
			}
			currentDValue.name = tok.substring(0, tok.length() - 1);
			return LSState.WANT_VAL;
		}
		private LSState handleVal(String tok, String line) {
			if (tok.equals("{")) {
				if (currentDValue.valueList == null) {
					currentDValue.valueList = new ArrayList<>();
				}
				finalDvalue = currentDValue;
				continueFlag = true;
				return LSState.PARTIAL;
			}
			
			//new impl. if is quoted then parse quoted bit as value.
			LSState newState = LSState.END;
			if (tok.startsWith("\"")) {
//				int pos = line.indexOf('"');
//				int endpos = line.indexOf('"', pos + 1);
//				currentDValue.rawValue = line.substring(pos + 1, endpos);
				
				JSONStringParser jparser = new JSONStringParser();
				currentDValue.rawValue = jparser.findJSONString(line, line.indexOf(':'));
				
				newState = LSState.NO_MORE;
			} else {
				//old
//				if (tok.endsWith(",") || tok.endsWith(";")) {
//					tok = tok.substring(0, tok.length() - 1);
//				}
				
				if (tok.equals("[")) {
					currentDValue.tmplist = new ArrayList<>();
					return LSState.LIST;
				}
				
				//new is to take rest of line as value but remove trailing comment or ,;
				line = line.trim();
				int pos = line.indexOf(':');
				line = stripEndOfLineDelimitersIfNeeded(line);
				int endpos = line.indexOf("//", pos + 1);
				if (endpos > 0) {
					line = line.substring(0, endpos).trim();
					line = stripEndOfLineDelimitersIfNeeded(line);
				}
				
				if (line.trim().endsWith("}")) {
					continueFlag = false;
					line = line.substring(0, line.length() - 1);
					currentDValue.rawValue = line.substring(pos + 1).trim();
					finalDvalue.valueList.add(currentDValue);
					return LSState.NO_MORE;
				}
				
				currentDValue.rawValue = line.substring(pos + 1).trim();
				newState = LSState.NO_MORE;
			}

			if (continueFlag) {
				finalDvalue.valueList.add(currentDValue);
			} else {
				finalDvalue = currentDValue;
			}
			return newState;
		}
		private String stripEndOfLineDelimitersIfNeeded(String line) {
			if (line.endsWith(",") || line.endsWith(";")) {
				line = line.substring(0, line.length() - 1);
			}
			return line;
		}
		private LSState handleEnd(String tok) {
			if (continueFlag) {
				if (tok.equals("}"));
				continueFlag = false;
				return LSState.END;
			}
			
			if (tok.startsWith("//")) {
				return LSState.NO_MORE;
			}
			return LSState.ERROR;
		}


		private void log(String s) {
			System.out.println(s);
		}
		public boolean isContinueFlag() {
			return continueFlag;
		}

	}

	@Test
	public void test() {
		Scanner scan = new Scanner("a b cd e;\r\nf");
		while(scan.hasNext()) {
			String tok = scan.next();
			log(tok);
		}
		scan.close();
	}

	@Test
	public void testLineScanner() {
		DValue dval = doScan("int size: 5");
		checkDVal(dval, "int", "size", "5");

		//, and ; allowed but ignored
		dval = doScan("int size: 5,");
		checkDVal(dval, "int", "size", "5");

		dval = doScan("int size: 5;");
		checkDVal(dval, "int", "size", "5");
	}
	@Test
	public void testStringWithQuotes() {
		DValue dval = doScan("string name: \"a big taxi\"");
		checkDVal(dval, "string", "name", "a big taxi");

		dval = doScan("string name: \"a big taxi\";");
		checkDVal(dval, "string", "name", "a big taxi");

		String s = fix("string name: 'bob \\'jim\\' smith' //comment");
		dval = doScan(s);
		checkDVal(dval, "string", "name", "bob \\\"jim\\\" smith");
	}
	@Test
	public void testStringWithoutQuotes() {
		DValue dval = doScan("string name: a big taxi");
		checkDVal(dval, "string", "name", "a big taxi");

		dval = doScan("string name:   a big  taxi ");
		checkDVal(dval, "string", "name", "a big  taxi");

		dval = doScan("string name:   a big  taxi, ");
		checkDVal(dval, "string", "name", "a big  taxi");

		dval = doScan("string name:   a big  taxi, //a comment ");
		checkDVal(dval, "string", "name", "a big  taxi");
	}

	@Test
	public void testStringList() {
		//for now list must be on one list with [ ] as separate tokens
		//list elements must use quotes and commas
		DValue dval = doScan(fix("list<string> name: [ 'a', 'b', 'c' ]"));
		checkDValList(dval, "list<string>", "name", 3, "a");
		assertEquals("b", dval.tmplist.get(1));
	}

	@Test
	public void testLineScanner2() {
		checkScanFail("int size 5");
		checkScanFail("int size ");
	}

	//THIS ONE!!
	@Test
	public void testLineScanner3() {
		DValue dval = doScan("Position pos: {");
		checkDVal(dval, "Position", "pos", null);
		assertTrue(dval.valueList != null);
		continueScan(dval, "x: 45 }", false);
		checkDVal(dval, "Position", "pos", null);
		assertEquals(1, dval.valueList.size());
		checkDVal(dval.valueList.get(0), null, "x", "45");
	}

	@Test
	public void testLineScanner4() {
		DValue dval = doScan("Position pos: {");
		checkDVal(dval, "Position", "pos", null);
		assertTrue(dval.valueList != null);
		continueScan(dval, "x: 45 ", true);
		checkDVal(dval, "Position", "pos", null);
		assertEquals(1, dval.valueList.size());
		checkDVal(dval.valueList.get(0), null, "x", "45");

		continueScan(dval, "y: 14 }", false);
		checkDVal(dval, "Position", "pos", null);
		assertEquals(2, dval.valueList.size());
		checkDVal(dval.valueList.get(1), null, "y", "14");

	}

	@Test
	public void testLineScanner3Missing() {
		DValue dval = doScan("Position pos: {");
		checkDVal(dval, "Position", "pos", null);
		assertTrue(dval.valueList != null);
		continueScan(dval, " col: 45 ", true);
		checkDVal(dval, "Position", "pos", null);
		assertEquals(1, dval.valueList.size());
	}

	//--helpers
	private void checkScanFail(String input) {
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		LineScanner scanner = new LineScanner(null, errorTracker);
		boolean b = scanner.scan(input);
		assertEquals(false, b);
		DValue dval = scanner.getDValue();
		assertEquals(null, dval);
	}

	private void checkDVal(DValue dval, String type, String name, String val) {
		assertEquals(type, dval.type);
		assertEquals(name, dval.name);
		assertEquals(val, dval.rawValue);
	}
	private void checkDValList(DValue dval, String type, String name, int listlen, String val0) {
		assertEquals(type, dval.type);
		assertEquals(name, dval.name);
		assertEquals(listlen, dval.tmplist.size());
		if (listlen > 0) {
			assertEquals(val0, dval.tmplist.get(0));
		}
	}

	private DValue doScan(String input) {
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		LineScanner scanner = new LineScanner(null, errorTracker);
		boolean b = scanner.scan(input);
		assertEquals(true, b);
		DValue dval = scanner.getDValue();
		return dval;
	}

	private void continueScan(DValue dval, String input, boolean expectedContFlag) {
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		LineScanner scanner = new LineScanner(null, dval, errorTracker);
		boolean b = scanner.scan(input);
		assertEquals(true, b);
		assertEquals(expectedContFlag, scanner.isContinueFlag());
	}


	///////////////////////////////////
	public static enum FSState {
		WANT_PACKAGE,
		WANT_PACKAGENAME,
		INSIDE,
		END,
		ERROR
	}


	public static class FileScanner {
		private ParseErrorTracker errorTracker = new ParseErrorTracker();
		public List<DValue> valueL = new ArrayList<>();
		private String currentPackage;
		private int lineNum;
		private DValue continuingDVal;

		public FileScanner(ParseErrorTracker errorTracker2) {
			this.errorTracker = errorTracker2;
		}

		public boolean scan(List<String> fileL) {
			FSState state = FSState.WANT_PACKAGE;

			lineNum = 0;
			for(String line : fileL) {
				log(String.format("line%d: %s",  lineNum++, line));
				
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				
				if (line.startsWith("//")) {
					continue;
				}
				
				if (state == FSState.INSIDE) {
					state = handleInside(line);
					continue;
				}
				
				//handle package line
				state = doPackageLine(state, line);
			}


			return (state == FSState.END) && (errorTracker.areNoErrors());
		}
		
		private FSState doPackageLine(FSState state, String line) {
			Scanner scan = new Scanner(line);

			while(scan.hasNext()) {
				String tok = scan.next();
				log(tok);
				tok = tok.trim();
				switch(state) {
				case WANT_PACKAGE:
					state = handlePackage(tok);
					break;
				case WANT_PACKAGENAME:
					state = handlePackageName(tok);
					break;
				case END:
					state = handleEnd(tok);
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

		private FSState handleEnd(String tok) {
			if (tok.startsWith("package")) {
				this.currentPackage = null;
				this.continuingDVal = null;
				return FSState.WANT_PACKAGENAME;
			}
			return FSState.END;
		}

		private FSState handlePackage(String tok) {
			if (tok.startsWith("package")) {
				return FSState.WANT_PACKAGENAME;
			}
			return FSState.ERROR;
		}
		private FSState handlePackageName(String tok) {
			this.currentPackage = tok;
			log("pack: " + currentPackage);
			return FSState.INSIDE;
		}
		private FSState handleInside(String tok) {
			if (tok.equals("end")) {
				return FSState.END;
			} else if (tok.startsWith("//")) {
				return FSState.INSIDE;
			}

			LineScanner scanner = new LineScanner(currentPackage, errorTracker);
			if (this.continuingDVal != null) {
				scanner = new LineScanner(currentPackage, continuingDVal, errorTracker);
			}
			boolean b = scanner.scan(tok);
			if (! b) {
				this.errorTracker.addError(String.format("line %d failed", lineNum));
				return FSState.ERROR;
			} else if (! scanner.isContinueFlag()) {
				this.valueL.add(scanner.getDValue());
			} else {
				this.continuingDVal = scanner.getDValue();
			}
			
			return FSState.INSIDE;
		}


		private void log(String s) {
			System.out.println(s);
		}

	}

	@Test
	public void testF0() {
		List<String> fileL = buildFile(0);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		FileScanner scanner = new FileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(false, b);
	}

	@Test
	public void testF1() {
		List<String> fileL = buildFile(1);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		FileScanner scanner = new FileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(0, scanner.valueL);
	}
	@Test
	public void testF2() {
		List<String> fileL = buildFile(2);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		FileScanner scanner = new FileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.valueL);
		checkDVal(scanner.valueL.get(0), "int", "size", "45");
	}
	
	@Test
	public void testF3() {
		List<String> fileL = buildFile(3);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		FileScanner scanner = new FileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.valueL);
		checkDVal(scanner.valueL.get(0), "int", "size", null);
		checkDVal(scanner.valueL.get(0).valueList.get(0), null, "wid", "45");
		assertEquals(1, scanner.valueL.get(0).valueList.size());
	}
	@Test
	public void testF4() {
		List<String> fileL = buildFile(4);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		FileScanner scanner = new FileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(2, scanner.valueL);
		checkDVal(scanner.valueL.get(0), "int", "size", "45");
		checkDVal(scanner.valueL.get(1), "int", "col", "145");
		checkPackage(scanner.valueL.get(0), "a.b.c");
		checkPackage(scanner.valueL.get(1), "a.b.c");
	}
	@Test
	public void testF5() {
		List<String> fileL = buildFile(5);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		FileScanner scanner = new FileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.valueL);
		checkDVal(scanner.valueL.get(0), "int", "size", null);
		checkDVal(scanner.valueL.get(0).valueList.get(0), null, "height", "66");
		checkSize(2, scanner.valueL.get(0).valueList);
	}
	@Test
	public void testF6() {
		List<String> fileL = buildFile(6);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		FileScanner scanner = new FileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(2, scanner.valueL);
		checkDVal(scanner.valueL.get(0), "int", "size", "45");
		checkPackage(scanner.valueL.get(0), "a.b.c");
		checkDVal(scanner.valueL.get(1), "int", "wid", "33");
		checkPackage(scanner.valueL.get(1), "d.e.f");
	}
	@Test
	public void testF7() {
		List<String> fileL = buildFile(7);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		FileScanner scanner = new FileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.valueL);
		checkDVal(scanner.valueL.get(0), "int", "size", "45");
		checkPackage(scanner.valueL.get(0), "a.b.c");
	}
	
	
	
	
	
	private void checkSize(int expectedSize, List<DValue> list) {
		assertEquals(expectedSize, list.size());
	}
	private void checkPackage(DValue dValue, String string) {
		assertEquals(string, dValue.packageName);
	}


	private List<String> buildFile(int scenario) {
		List<String> L = new ArrayList<>();
		switch(scenario) {
		case 0:
			L.add("");
			break;
		case 1:
			L.add("");
			L.add("package a.b.c");
			L.add(" ");
			L.add("end");
			L.add("");
			break;
		case 2:
			L.add("");
			L.add("package a.b.c");
			L.add(" int size: 45");
			L.add("end");
			L.add("");
			break;
		case 3:
			L.add("");
			L.add("package a.b.c");
			L.add(" int size: {");
			L.add("  wid: 45 }");
//			L.add(" }");
			L.add("end");
			L.add("");
			break;
		case 4:
			L.add("");
			L.add("package a.b.c");
			L.add(" int size: 45");
			L.add(" int col: 145");
			L.add("end");
			L.add("");
			break;
		case 5:
			L.add("");
			L.add("package a.b.c");
			L.add(" int size: {");
			L.add("   height: 66 ");
			L.add("   wid: 45 }");
//			L.add(" }");
			L.add("end");
			L.add("");
			break;
		case 6:
			L.add("");
			L.add("package a.b.c");
			L.add(" int size: 45");
			L.add("end");
			L.add("package d.e.f");
			L.add(" int wid: 33");
			L.add("end");
			L.add("");
			break;
		case 7:
			L.add("//a comment");
			L.add("package a.b.c //another one");
			L.add(" int size: 45 //third one");
			L.add("end");
			L.add(""); 
			break;
		}
		return L;
	}
	protected static String fix(String s)
	{
		s = s.replace('\'', '"');
		return s;
	}

}
