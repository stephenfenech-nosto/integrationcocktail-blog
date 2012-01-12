package org.ic.camel.tools.bean.exception;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RethrowException {
	
	Logger logger=LoggerFactory.getLogger(RethrowException.class);
	
	public void processExchange(Exchange exchange) throws Throwable
	{
		Throwable caughtException=(Throwable) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		if(caughtException!=null)
		{
			logger.debug("Rethrowing Exception "+caughtException.getClass());
			throw caughtException;
		}
	}
}
