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
	
	public static class EvalResult {
		public boolean success;
		public boolean result;
		public String errMsg;
		
		public void succeeded(boolean result) {
			this.success = true;
			this.result = result;
			this.errMsg = null;
		}
		public void failed(String errMsg) {
			this.success = false;
			this.result = false;
			this.errMsg = errMsg;
		}
	}

	public static class ExpressionEval {

		public EvalResult evalInt(int value, String expr) {

			ScriptEngineManager engineManager = new ScriptEngineManager();
			ScriptEngine engine = engineManager.getEngineByName("nashorn");

			ScriptContext context = engine.getContext();
			context.setAttribute("value", value, ScriptContext.ENGINE_SCOPE);

			if (! expr.endsWith(";")) {
				expr += ";";
			}
			
			EvalResult result = new EvalResult();
			Object obj = null;
			String errMsg = null;
			try {
				obj = engine.eval(expr);
			} catch (ScriptException e) {
				errMsg = e.getMessage();
			}
			if (obj instanceof Boolean) {
				result.succeeded((Boolean)obj);
			} else {
				result.failed(errMsg);
			}
			
			return result;
		}
	}

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

	@Test
	public void test2() throws Exception {

		ExpressionEval eval = new ExpressionEval();

		checkEval(true, eval, 45, "value > 0");
		checkEval(true, eval, 45, "value > 44");
		checkEval(false, eval , 45, "value > 100");

		checkEvalFail(false, eval , 45, "value >>>> 100");
		
	}
	
	private void checkEval(boolean expected, ExpressionEval eval, int val, String expr) {
		EvalResult result = eval.evalInt(val, expr);
		assertEquals(expected, result.result);
	}
	private void checkEvalFail(boolean expected, ExpressionEval eval, int val, String expr) {
		EvalResult result = eval.evalInt(val, expr);
		log(result.errMsg);
		assertEquals(false, result.success);
	}
}
