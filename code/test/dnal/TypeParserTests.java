package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.mef.dnal.parser.LSState;
import org.mef.dnal.parser.ParseErrorTracker;

import testhelper.BaseTest;


public class TypeParserTests extends BaseTest {
	
	public static class DType {
		public String packageName;
		public String baseType;
		public String name;
	}	

	public static class TypeLineScanner {
		private DType currentDType;
		private DType finalDvalue;
		private ParseErrorTracker errorTracker = new ParseErrorTracker();
		private boolean continueFlag;
		private String packageName;

		public TypeLineScanner(String packageName) {
			this.packageName = packageName;
		}
		public TypeLineScanner(String packageName, DType dtype) {
			this.packageName = packageName;
			this.finalDvalue = dtype;
			continueFlag = true;
		}

		public boolean scan(String line) {
			LSState state = LSState.WANT_TYPE;
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
					state = handleVal(tok);
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

		public DType getDType() {
			if (errorTracker.hasErrors()) {
				return null;
			}
			return finalDvalue;
		}

		private LSState handleType(String tok) {
			currentDType = new DType();
//			currentDType.type = tok;
			currentDType.packageName = this.packageName;
			return LSState.WANT_NAME;
		}
		private LSState handleName(String tok) {
			if (! tok.endsWith(":")) {
				errorTracker.addError("missing ':'");
				return LSState.ERROR;
			}
			currentDType.name = tok.substring(0, tok.length() - 1);
			return LSState.WANT_VAL;
		}
		private LSState handleVal(String tok) {
			if (tok.equals("{")) {
//				if (currentDType.valueList == null) {
//					currentDType.valueList = new ArrayList<>();
//				}
				finalDvalue = currentDType;
				continueFlag = true;
				return LSState.PARTIAL;
			}

			if (tok.endsWith(",") || tok.endsWith(";")) {
				tok = tok.substring(0, tok.length() - 1);
			}
//			currentDType.rawValue = tok;

			if (continueFlag) {
//				finalDvalue.valueList.add(currentDType);
			} else {
				finalDvalue = currentDType;
			}
			return LSState.END;
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
	public void testTypeLineScanner() {
		DType dtype = doScan("int size");
		checkDVal(dtype, "int", "size", "5");
//
//		//, and ; allowed but ignored
//		dtype = doScan("int size: 5,");
//		checkDVal(dtype, "int", "size", "5");
//
//		dtype = doScan("int size: 5;");
//		checkDVal(dtype, "int", "size", "5");
	}

//	@Test
//	public void testTypeLineScanner2() {
//		checkScanFail("int size 5");
//		checkScanFail("int size ");
//	}
//
//	@Test
//	public void testTypeLineScanner3() {
//		DType dtype = doScan("int size: {");
//		checkDVal(dtype, "int", "size", null);
//		assertTrue(dtype.valueList != null);
//		continueScan(dtype, "int col: 45 }", false);
//		checkDVal(dtype, "int", "size", null);
//		assertEquals(1, dtype.valueList.size());
//		checkDVal(dtype.valueList.get(0), "int", "col", "45");
//	}
//
//	@Test
//	public void testTypeLineScanner4() {
//		DType dtype = doScan("int size: {");
//		checkDVal(dtype, "int", "size", null);
//		assertTrue(dtype.valueList != null);
//		continueScan(dtype, "int col: 45 ", true);
//		checkDVal(dtype, "int", "size", null);
//		assertEquals(1, dtype.valueList.size());
//		checkDVal(dtype.valueList.get(0), "int", "col", "45");
//
//		continueScan(dtype, "int wid: 14 }", false);
//		checkDVal(dtype, "int", "size", null);
//		assertEquals(2, dtype.valueList.size());
//		checkDVal(dtype.valueList.get(1), "int", "wid", "14");
//
//	}
//
//	@Test
//	public void testTypeLineScanner3Missing() {
//		DType dtype = doScan("int size: {");
//		checkDVal(dtype, "int", "size", null);
//		assertTrue(dtype.valueList != null);
//		continueScan(dtype, "int col: 45 ", true);
//		checkDVal(dtype, "int", "size", null);
//		assertEquals(1, dtype.valueList.size());
//	}

	//--helpers
	private void checkScanFail(String input) {
		TypeLineScanner scanner = new TypeLineScanner(null);
		boolean b = scanner.scan(input);
		assertEquals(false, b);
		DType dtype = scanner.getDType();
		assertEquals(null, dtype);
	}

	private void checkDVal(DType dtype, String type, String name, String val) {
//		assertEquals(type, dtype.type);
//		assertEquals(name, dtype.name);
//		assertEquals(val, dtype.rawValue);
	}

	private DType doScan(String input) {
		TypeLineScanner scanner = new TypeLineScanner(null);
		boolean b = scanner.scan(input);
		assertEquals(true, b);
		DType dtype = scanner.getDType();
		return dtype;
	}

	private void continueScan(DType dtype, String input, boolean expectedContFlag) {
		TypeLineScanner scanner = new TypeLineScanner(null, dtype);
		boolean b = scanner.scan(input);
		assertEquals(true, b);
		assertEquals(expectedContFlag, scanner.isContinueFlag());
	}

}
