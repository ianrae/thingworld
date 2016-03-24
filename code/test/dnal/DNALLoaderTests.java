package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DNALLoaderTests {

	@Test
	public void test() {
		fail("Not yet implemented");
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
			L.add(" int wid: 45 }");
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
			L.add(" int height: 66 ");
			L.add(" int wid: 45 }");
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
	

}
