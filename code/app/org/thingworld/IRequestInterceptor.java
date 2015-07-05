package org.thingworld;


public interface IRequestInterceptor
{
	void process(Request request, Reply reply, InterceptorContext itx);
}