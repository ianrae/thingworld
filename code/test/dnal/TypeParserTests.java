package dnal;

import static org.junit.Assert.*;

import java.util.Scanner;

import org.junit.Test;
import org.mef.dnal.parser.ParseErrorTracker;

import testhelper.BaseTest;


public class TypeParserTests extends BaseTest {
	
	public static class DType {
		public String packageName;
		public String baseType;
		public String name;
	}	
	
	public enum LTState {
		WANT_TYPE,
		WANT_NAME,
		END,
		NO_MORE,
		PARTIAL,
		ERROR
	}
	public static class TypeLineScanner {
		private DType currentDType;
		private DType finalDvalue;
		private ParseErrorTracker errorTracker = new ParseErrorTracker();
		private String packageName;

		public TypeLineScanner(String packageName) {
			this.packageName = packageName;
		}

		public boolean scan(String line) {
			LTState state = LTState.WANT_TYPE;
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
				case END:
					state = handleEnd(tok);
					break;
				default:
					errorTracker.addError("unexpected token:" + tok + " state:" + state);
					break;
				}
				
				if (state == LTState.NO_MORE) {
					break; //we're done
				}
			}
			scan.close();

			return (state == LTState.END || state == LTState.NO_MORE) && (errorTracker.areNoErrors());
		}

		public DType getDType() {
			if (errorTracker.hasErrors()) {
				return null;
			}
			return finalDvalue;
		}

		private LTState handleType(String tok) {
			currentDType = new DType();
			currentDType.baseType = tok;
			currentDType.packageName = this.packageName;
			return LTState.WANT_NAME;
		}
		private LTState handleName(String tok) {
			if (tok.endsWith(",") || tok.endsWith(";")) {
				tok = tok.substring(0, tok.length() - 1);
			}
			currentDType.name = tok;
			finalDvalue = currentDType;
			return LTState.END;
		}
		private LTState handleEnd(String tok) {
			if (tok.startsWith("//")) {
				return LTState.NO_MORE;
			}
			this.errorTracker.addError("unknown token: " + tok);
			return LTState.ERROR;
		}


		private void log(String s) {
			System.out.println(s);
		}
	}

	@Test
	public void testTypeLineScanner() {
		DType dtype = doScan("int size");
		checkDType(dtype, "int", "size");

		//, and ; allowed but ignored
		dtype = doScan("int size,");
		checkDType(dtype, "int", "size");

		dtype = doScan("int size;");
		checkDType(dtype, "int", "size");
	}

	@Test
	public void testTypeLineScanner2() {
		checkScanFail("int ");
		checkScanFail("int size sdldssdfs");
	}


	//--helpers
	private void checkScanFail(String input) {
		TypeLineScanner scanner = new TypeLineScanner(null);
		boolean b = scanner.scan(input);
		assertEquals(false, b);
		DType dtype = scanner.getDType();
		assertEquals(null, dtype);
	}

	private void checkDType(DType dtype, String type, String name) {
		assertEquals(type, dtype.baseType);
		assertEquals(name, dtype.name);
	}

	private DType doScan(String input) {
		TypeLineScanner scanner = new TypeLineScanner(null);
		boolean b = scanner.scan(input);
		assertEquals(true, b);
		DType dtype = scanner.getDType();
		return dtype;
	}

}
