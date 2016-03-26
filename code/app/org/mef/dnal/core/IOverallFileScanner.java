package org.mef.dnal.core;

import java.util.List;

public interface IOverallFileScanner {
	boolean load(String path);
	boolean isValid();
	void dumpErrors();
	boolean scan(List<String> fileL);
	IDNALLoader getDloader();
}