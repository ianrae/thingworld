package org.thingworld.entitydb;

public class IntegerValueMatcher implements IValueMatcher
{

	@Override
	public boolean isMatch(Object value, Object valueToMatch, int matchType)
	{
		Integer n1 = (Integer)value;
		Integer n2 = (Integer) valueToMatch;
		
		return (n1.intValue() == n2.intValue());
	}

	@Override
	public int compare(Object value, Object valueToMatch, int matchType) 
	{
		Integer n1 = (Integer)value;
		Integer n2 = (Integer) valueToMatch;
		return n1.compareTo(n2);
	}

}
