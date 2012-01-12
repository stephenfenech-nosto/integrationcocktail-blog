package org.ic.camel.tools.bean.exception;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class TestRethrowException extends CamelTestSupport {

	@Produce(uri = "direct:start")
	protected ProducerTemplate template;
	
	@Test(expected=Exception.class)
	public void testRethrowException() throws Exception
	{
		try{
			template.sendBody("Test");
		}catch(Exception e)
		{
			assertTrue(e instanceof CamelExecutionException);
			CamelExecutionException camelException=(CamelExecutionException)e;
			Exception myException=(Exception) camelException.getCause();
			assertEquals("MyException",myException.getMessage());
			throw e;
		}
	}
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
				from("direct:start").routeId("RethrowException").doTry().throwException(new Exception("MyException")).doCatch(Exception.class).bean(RethrowException.class);
			}
		};
	}

}
