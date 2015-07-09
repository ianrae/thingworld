package testhelper;

import java.util.Map;

import org.mef.twixt.ValueContainer;
import org.mef.twixt.binder.MockTwixtBinder;
import org.mef.twixt.binder.TwixtBinder;
import org.thingworld.IFormBinder;

import play.data.Form;


public class LocalMockBinder<T extends ValueContainer> implements IFormBinder<T>
{
	MockTwixtBinder<T> inner;
	int failureDestination;
	
	public LocalMockBinder(Class<T> clazz, Map<String,String> anyData)
	{
		inner = new MockTwixtBinder<T>((Class<T>) clazz, anyData);
	}

	@Override
	public boolean bind() 
	{
		return inner.bind();
	}

	@Override
	public Object getValidationErrors() {
		return inner.getValidationErrors();
	}

	@Override
	public T get() {
		return inner.get();
	}

	@Override
	public Form<T> getForm() {
		return inner.getForm();
	}

	@Override
	public Form<T> fillForm(T input) 
	{
		return inner.fillForm(input);
	}

	@Override
	public int getFailDestination() {
		return this.failureDestination;
	}

	@Override
	public void setFailDestination(int failureDestination) {
		this.failureDestination = failureDestination;
	}

}
