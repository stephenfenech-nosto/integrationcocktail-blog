package org.ic.mule.tools.processor.control;

import org.ic.mule.tools.IntegrationCocktailConstants;
import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowController implements MessageProcessor{

	public static final String START = "START";
	public static final String STOP = "STOP";
	public static final String PAUSE = "PAUSE";
	public static final String RESUME = "RESUME";

	public static final String PREFIX = IntegrationCocktailConstants.PREFIX
			+ ".RouteController";
	public static final String FLOW_NAME = PREFIX + ".FlowName";
	public static final String ACTION = PREFIX + ".Action";
	public static final String ACTION_TAKEN = PREFIX + ".ActionTaken";

	private String action = PAUSE;
	private String flowName = null;
	
	Logger logger = LoggerFactory.getLogger(FlowController.class);

	
	@Override
	public MuleEvent process(MuleEvent event) throws MuleException {
		MuleContext muleContext=event.getMuleContext();
		MuleMessage message = event.getMessage();
		
		// Check if action and flow name are set as header
		String flowName=message.getInboundProperty(FLOW_NAME,getFlowName());		
		String action=message.getInboundProperty(ACTION,getAction());
		
		Lifecycle lifecycle=muleContext.getRegistry().lookupObject(flowName);
		if(lifecycle == null)
		{
			throw new DefaultMuleException("No flow found with name "+flowName);
		}
		
		String actionTaken=null;
		logger.debug("Excecuting " + action + " on " + flowName);
		if(lifecycle instanceof Service)
		{
			Service service=(Service) lifecycle;
			actionTaken=performAction(service,action);
		}else
		{
			actionTaken=performAction(lifecycle,action);
		}
		
		message.setOutboundProperty(ACTION_TAKEN, actionTaken);
		return event;
	}
	
	private String performAction(Lifecycle lifecycle, String action) throws MuleException {
		if(action.equals(START))
		{
			lifecycle.start();
			return action;
		}else if (action.equals(STOP))
		{
			lifecycle.stop();
			return action;
		}else if (action.equals(PAUSE))
		{
			logger.warn("Stopping instead of Pausing since don't know how to pause "+lifecycle.getClass());
			lifecycle.stop();
			return STOP;
		}else if (action.equals(RESUME))
		{
			logger.warn("Starting instead of Resuming since don't know how to pause "+lifecycle.getClass());
			lifecycle.start();
			return START;
		}else
		{
			throw new UnsupportedOperationException("Unknown action " + action
					+ ". Action should be STOP, START, PAUSE or RESUME");
		}
	}

	private String performAction(Service service, String action) throws MuleException
	{
		if(action.equals(START))
		{
			service.start();
			return action;
		}else if (action.equals(STOP))
		{
			service.stop();
			return action;
		}else if (action.equals(PAUSE))
		{
			service.pause();
			return action;
		}else if (action.equals(RESUME))
		{
			service.resume();
			return action;
		}else
		{
			throw new UnsupportedOperationException("Unknown action " + action
					+ ". Action should be STOP, START, PAUSE or RESUME");
		}
	}


	public String getAction() {
		return action;
	}


	public void setAction(String action) {
		this.action = action;
	}


	public String getFlowName() {
		return flowName;
	}


	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}
	
	
}
