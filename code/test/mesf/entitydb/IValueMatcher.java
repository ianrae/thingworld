package mesf.entitydb;

public interface IValueMatcher 
{
	public final int EXACT = 1;
	public final int CASE_INSENSITIVE = 2;
	public final int LIKE = 3;
	public final int ILIKE = 4;
	
	public final int LT=5;
	public final int GT=6;
	public final int LE=7;
	public final int GE=8;
	public final int NEQ=9;
	
	boolean isMatch(Object value, Object valueToMatch, int matchType); 
	int compare(Object value, Object valueToMatch, int matchType); 
}
