package org.mef.dnal.parser;

public enum LSState {
	WANT_TYPE,
	WANT_NAME,
	WANT_VAL,
	END,
	NO_MORE,
	PARTIAL,
	ERROR
}