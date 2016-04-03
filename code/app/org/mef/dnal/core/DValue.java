package org.mef.dnal.core;

import java.util.List;


public class DValue {
	public String packageName;
	public String type;
	public String name;
	public String rawValue;
	public Object finalValue;
//	public List<String> tmplist; //when value is a list of things
	public List<DValue> valueList; //either value or this. for struct and list
}