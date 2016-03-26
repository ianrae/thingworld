package org.mef.dnal.parser;

public enum LSState {
	WANT_TYPE,
	WANT_NAME,
	WANT_VAL,
	LIST,
	END,
	NO_MORE,
	PARTIAL,
	ERROR
}