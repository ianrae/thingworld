package org.thingworld.cmd;

public class BaseCommand implements ICommand
{
	public long entityId;

	@Override
	public long getEntityId() 
	{
		return entityId; //x
	}
}