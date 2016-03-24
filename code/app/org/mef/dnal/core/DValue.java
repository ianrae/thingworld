package org.mef.dnal.core;

import java.util.List;


public class DValue {
	public String packageName;
	public String type;
	public String name;
	public String rawValue;
	public Object finalValue;
	public List<DValue> valueList; //either value or this
}