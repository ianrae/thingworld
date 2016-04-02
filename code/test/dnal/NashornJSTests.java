package dnal;

import static org.junit.Assert.*;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

import testhelper.BaseTest;

public class NashornJSTests extends BaseTest {

	@Test
	public void test0() throws Exception {
		ScriptEngineManager engineManager = 
				new ScriptEngineManager();
		ScriptEngine engine = 
				engineManager.getEngineByName("nashorn");
		engine.eval("function sum(a, b) { return a + b; }");
		
		Object obj = engine.eval("sum(1, 2);");
		log(obj.toString());
		String s = obj.toString();
		assertEquals("3", s);
	}

	@Test
	public void test1() throws Exception {
		ScriptEngineManager engineManager = 
				new ScriptEngineManager();
		ScriptEngine engine = 
				engineManager.getEngineByName("nashorn");
		
		 ScriptContext context = engine.getContext();
        context.setAttribute("x", 15, ScriptContext.ENGINE_SCOPE);
		
		Object obj = engine.eval("x > 0;");
		log(obj.toString());
		String s = obj.toString();
		assertEquals("true", s);
	}
}
