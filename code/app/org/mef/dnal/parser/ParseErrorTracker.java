package org.mef.dnal.parser;

import java.util.ArrayList;
import java.util.List;

public class ParseErrorTracker {
	private List<String> list = new ArrayList<>();

	public void addError(String err) {
		System.out.println("ERR: " + err);
		list.add(err);
	}

	public boolean areNoErrors() {
		return list.size() == 0;
	}

	public boolean hasErrors() {
		return list.size() > 0;
	}
	
	public void dumpErrors() {
		for(String err: list) {
			System.out.println(err);
		}
		System.out.println(String.format("%d errros", list.size()));
	}
}