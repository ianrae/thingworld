package org.mef.dnal.core;

import java.util.List;

public interface IDNALLoader {
	boolean load(String path);
	boolean isValid();
	boolean load(List<String> lines);
	List<DValue> getDataL();
}