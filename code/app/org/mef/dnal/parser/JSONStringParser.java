package org.mef.dnal.parser;

public class JSONStringParser {
		
//		http://stackoverflow.com/questions/19176024/how-to-escape-special-characters-in-building-a-json-string
		public String findJSONString(String s, int startPos) {
			int pos = s.indexOf('"', startPos);
			
			int endpos = 0;
			boolean inQuote = false;
			for(int i = pos + 1; i < s.length(); i++) {
				char ch = s.charAt(i);
				if (ch == '\\') {
					if (! inQuote) {
						inQuote = true;
					} else {
						inQuote = false;
					}
				}
				
				if (ch == '"') {
					if (! inQuote) {
						endpos = i;
						break;
					} else {
						inQuote = false;
					}
				} else if ("/bfnrtu".indexOf(ch) >= 0 && inQuote) {
					inQuote = false;
				}
			}
			
			return s.substring(pos + 1, endpos);
		}
	}