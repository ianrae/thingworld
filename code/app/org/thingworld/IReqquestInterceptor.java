package org.thingworld;


public interface IReqquestInterceptor
{
	void process(Request request, Reply reply, InterceptorContext itx);
}