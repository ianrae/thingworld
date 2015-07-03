package org.thingworld.entity;

import org.thingworld.config.Config;
import org.thingworld.config.IConfig;

import com.rits.cloning.Cloner;

//clone automatically using Cloner

public class BaseEntity extends Entity
{
	@Override
	public Entity clone()
	{
		//Cloner has strange error when play does automatic recompile in Dev mode
		//java.lang.IllegalArgumentException: Can not set java.lang.String field tw.entities.Defect.s to tw.entities.Defect
		//avoid this by setting CLONE_ENTITY_SKIP to 1 but is dangerous!
		int skip = Config.getIntValue(IConfig.ConfigItem.CLONE_ENTITY_WHEN_HYDRATE);
		if (skip != 0)
		{
			return this;
		}
		
		
		Cloner cloner=new Cloner();
//		cloner.setDumpClonedClasses(true);
		Entity clone=cloner.deepClone(this);
		return clone;
	}
	
}