package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.mef.dnal.parser.ParseErrorTracker;
import testhelper.BaseTest;


public class TypeParserTests extends BaseTest {
	
	public static class DTypeEntry {
		public String packageName;
		public String name;
		public String type;
	}	
	
	public enum LTState {
		WANT_TYPE,
		WANT_NAME,
		END,
		NO_MORE,
		ERROR
	}
	public static class TypeLineScanner {
		private DTypeEntry currentDTypeEntry;
		private DTypeEntry finalDvalue;
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

		public DTypeEntry getDTypeEntry() {
			if (errorTracker.hasErrors()) {
				return null;
			}
			return finalDvalue;
		}

		private LTState handleType(String tok) {
			currentDTypeEntry = new DTypeEntry();
			currentDTypeEntry.type = tok;
			currentDTypeEntry.packageName = this.packageName;
			return LTState.WANT_NAME;
		}
		private LTState handleName(String tok) {
			if (tok.endsWith(",") || tok.endsWith(";")) {
				tok = tok.substring(0, tok.length() - 1);
			}
			currentDTypeEntry.name = tok;
			finalDvalue = currentDTypeEntry;
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
		DTypeEntry dtype = doScan("int size");
		checkDTypeEntry(dtype, "int", "size");

		//, and ; allowed but ignored
		dtype = doScan("int size,");
		checkDTypeEntry(dtype, "int", "size");

		dtype = doScan("int size;");
		checkDTypeEntry(dtype, "int", "size");
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
		DTypeEntry dtype = scanner.getDTypeEntry();
		assertEquals(null, dtype);
	}

	private void checkDTypeEntry(DTypeEntry dtype, String type, String name) {
		assertEquals(type, dtype.type);
		assertEquals(name, dtype.name);
	}

	private DTypeEntry doScan(String input) {
		TypeLineScanner scanner = new TypeLineScanner(null);
		boolean b = scanner.scan(input);
		assertEquals(true, b);
		DTypeEntry dtype = scanner.getDTypeEntry();
		return dtype;
	}

	
	///////////////////////////////////
	public static class DType{
		public String packageName;
		public String name;
		public String baseType;
		public List<DTypeEntry> entries = new ArrayList<>();
	}	
	
	public static enum FTState {
		WANT_TYPE,
		WANT_TYPENAME,
		WANT_EXTENDS,
		WANT_BASETYPE,
		INSIDE,
		INSIDE_ENUM,
		END,
		ERROR
	}

	public static class TypeFileScanner {
		private ParseErrorTracker errorTracker;
		public List<DType> typeL = new ArrayList<>();
//		private String currentType;
		private int lineNum;
		private DType currentDType;

		public TypeFileScanner(ParseErrorTracker errorTracker) {
			this.errorTracker = errorTracker;
		}

		public boolean scan(List<String> fileL) {
			FTState state = FTState.WANT_TYPE;

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
				
				if (state == FTState.INSIDE) {
					state = handleInside(line);
					if (state == FTState.END) {
						state = handleEnd(line);
					}					
					continue;
				} else if (state == FTState.INSIDE_ENUM) {
					state = handleInsideEnum(line);
					if (state == FTState.END) {
						state = handleEnd(line);
					}					
					continue;
				}
				
				//handle package line
				state = doTypeLine(state, line);
			}

			return (state == FTState.END) && (errorTracker.areNoErrors());
		}
		
		private FTState doTypeLine(FTState state, String line) {
			Scanner scan = new Scanner(line);

			while(scan.hasNext()) {
				String tok = scan.next();
				log(tok);
				tok = tok.trim();
				switch(state) {
				case WANT_TYPE:
					state = handleType(tok);
					break;
				case WANT_TYPENAME:
					state = handleTypeName(tok);
					break;
				case WANT_EXTENDS:
					state = handleExtends(tok);
					break;
				case WANT_BASETYPE:
					state = handleBaseType(tok);
					break;
				case END:
					state = handleInnerEnd(tok);
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

		private FTState handleEnd(String tok) {
			this.typeL.add(currentDType);
			this.currentDType = null; 
			return FTState.END;
		}
		private FTState handleInnerEnd(String tok) {
			if (tok.startsWith("type")) {
				this.currentDType = new DType();
				return FTState.WANT_TYPENAME;
			}
			return FTState.END;
		}

		private FTState handleType(String tok) {
			if (tok.startsWith("type")) {
				this.currentDType = new DType();
				return FTState.WANT_TYPENAME;
			}
			return FTState.ERROR;
		}
		private FTState handleTypeName(String tok) {
			this.currentDType.name = tok;
			return FTState.WANT_EXTENDS;
		}
		private FTState handleExtends(String tok) {
			if (tok.equals("extends")) {
				return FTState.WANT_BASETYPE;
			}
			return FTState.ERROR;
		}
		private FTState handleBaseType(String tok) {
			this.currentDType.baseType = tok;
			if (tok.equals("enum")) {
				return FTState.INSIDE_ENUM;
			}
			return FTState.INSIDE;
		}
		
		private FTState handleInside(String tok) {
			if (tok.equals("end")) {
				return FTState.END;
			} else if (tok.startsWith("//")) {
				return FTState.INSIDE;
			}

			TypeLineScanner scanner = new TypeLineScanner(null); //handle package later
			boolean b = scanner.scan(tok);
			if (! b) {
				this.errorTracker.addError(String.format("line %d failed", lineNum));
				return FTState.ERROR;
			} else {
				currentDType.entries.add(scanner.getDTypeEntry());
			}
			
			return FTState.INSIDE;
		}
		private FTState handleInsideEnum(String tok) {
			if (tok.equals("end")) {
				return FTState.END;
			} else if (tok.startsWith("//")) {
				return FTState.INSIDE_ENUM;
			}
			
			DTypeEntry entry = new DTypeEntry();
			entry.name = tok;
			entry.packageName = null;
			entry.type = "enumval";

			currentDType.entries.add(entry);
			
			return FTState.INSIDE_ENUM;
		}

		private void log(String s) {
			System.out.println(s);
		}
	}

	@Test
	public void testF0() {
		List<String> fileL = buildFile(0);
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		TypeFileScanner scanner = new TypeFileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(false, b);
	}

	@Test
	public void testF1() {
		List<String> fileL = buildFile(1);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		TypeFileScanner scanner = new TypeFileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.typeL);
		checkEntrySize(0, scanner.typeL.get(0).entries);
		checkDType(scanner.typeL.get(0), "int", "Timeout");
	}
	@Test
	public void testF2() {
		List<String> fileL = buildFile(2);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		TypeFileScanner scanner = new TypeFileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.typeL);
		checkDTypeEntry(scanner.typeL.get(0).entries.get(0), "int", "size");
		checkDType(scanner.typeL.get(0), "int", "Timeout");
	}
	
	@Test
	public void testF4() {
		List<String> fileL = buildFile(4);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		TypeFileScanner scanner = new TypeFileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.typeL);
		checkEntrySize(2, scanner.typeL.get(0).entries);
		checkDTypeEntry(scanner.typeL.get(0).entries.get(0), "int", "size");
		checkDTypeEntry(scanner.typeL.get(0).entries.get(1), "int", "col");
		checkDType(scanner.typeL.get(0), "int", "Timeout");
	}
	@Test
	public void testF6() {
		List<String> fileL = buildFile(6);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		TypeFileScanner scanner = new TypeFileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(2, scanner.typeL);
		checkEntrySize(1, scanner.typeL.get(0).entries);
		checkDTypeEntry(scanner.typeL.get(0).entries.get(0), "int", "size");
		checkDType(scanner.typeL.get(0), "int", "Timeout");

		checkEntrySize(1, scanner.typeL.get(1).entries);
		checkDTypeEntry(scanner.typeL.get(1).entries.get(0), "int", "wid");
		checkDType(scanner.typeL.get(1), "int", "ZTimeout");
	}
	@Test
	public void testF7() {
		List<String> fileL = buildFile(7);

		ParseErrorTracker errorTracker = new ParseErrorTracker();
		TypeFileScanner scanner = new TypeFileScanner(errorTracker);
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.typeL);
		checkDTypeEntry(scanner.typeL.get(0).entries.get(0), "int", "size");
		checkDType(scanner.typeL.get(0), "int", "Timeout");
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
			L.add("");
			L.add("type Timeout extends int");
			L.add(" ");
			L.add("end");
			L.add("");
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
