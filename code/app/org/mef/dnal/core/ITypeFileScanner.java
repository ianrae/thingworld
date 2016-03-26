package org.mef.dnal.core;

import java.util.List;

public interface ITypeFileScanner {
	List<DType> getDTypes();
	boolean scan(List<String> fileL);
}