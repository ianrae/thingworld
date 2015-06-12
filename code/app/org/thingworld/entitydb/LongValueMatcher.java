package org.thingworld.entitydb;

public class LongValueMatcher implements IValueMatcher
{

	@Override
	public boolean isMatch(Object value, Object valueToMatch, int matchType)
	{
		Long n1 = (Long)value;
		Long n2 = (Long) valueToMatch;
		
		return (n1.longValue() == n2.longValue());
	}

	@Override
	public int compare(Object value, Object valueToMatch, int matchType) 
	{
		Long n1 = (Long)value;
		Long n2 = (Long) valueToMatch;
		return n1.compareTo(n2);
	}
	
}
