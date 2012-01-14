package org.ic.mule.tools.processor.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.api.transport.DispatchException;
import org.mule.tck.junit4.FunctionalTestCase;

public class TestFlowController extends FunctionalTestCase {

	final static String SERVICE = "ServiceUnderTest";
	final static String VM_SERVICE = "vm://ServiceUnderTest";
	final static String VM_SERVICE_RESULT = "vm://ServiceUnderTest.result";
	final static String FLOW = "FlowUnderTest";
	final static String VM_FLOW = "vm://FlowUnderTest";
	final static String VM_FLOW_RESULT = "vm://FlowUnderTest.result";
	final static String VM_CONTROL = "vm://FlowControl";
	final static String VM_SELF_STOPPING = "vm://SelfStoppingFlow";
	final static Long TIMEOUT = 1000L;

	public TestFlowController() {
		setDisposeContextPerClass(false);
	}

	@Test
	public void testControlingService() throws Exception {
		// TestNormalFlow
		doRoundTrip(VM_SERVICE,VM_SERVICE_RESULT);
		
		// Stop & Start Service
		doControlTest(SERVICE, FlowController.STOP, FlowController.STOP,
				FlowController.START, FlowController.START, VM_SERVICE,
				VM_SERVICE_RESULT);
		doControlTest(SERVICE, FlowController.PAUSE, FlowController.PAUSE,
				FlowController.RESUME, FlowController.RESUME, VM_SERVICE,
				VM_SERVICE_RESULT);
	}
	
	@Test
	public void testControlingFlow() throws Exception {
		// TestNormalFlow
		doRoundTrip(VM_FLOW,VM_FLOW_RESULT);
		
		// Stop & Start Service
		doControlTest(FLOW, FlowController.STOP, FlowController.STOP,
				FlowController.START, FlowController.START, VM_FLOW,
				VM_FLOW_RESULT);
		doControlTest(FLOW, FlowController.PAUSE, FlowController.STOP,
				FlowController.RESUME, FlowController.START, VM_FLOW,
				VM_FLOW_RESULT);
	}
	
	@Test
	public void testControlingServiceBadAction() throws MuleException{
		MuleClient client = muleContext.getClient();
		String testMessage = "Test Message";
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(FlowController.FLOW_NAME, SERVICE);
		props.put(FlowController.ACTION, "BadAction");
		MuleMessage message = client.send(VM_CONTROL, testMessage, props);
		assertNotNull(message);
		assertNotNull(message.getExceptionPayload());
		assertTrue(message.getExceptionPayload().getException() instanceof UnsupportedOperationException);
		assertEquals("Unknown action BadAction. Action should be STOP, START, PAUSE or RESUME",message.getExceptionPayload().getMessage());
	}
	
	@Test
	public void testControlingFlowBadAction() throws MuleException{
		MuleClient client = muleContext.getClient();
		String testMessage = "Test Message";
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(FlowController.FLOW_NAME, FLOW);
		props.put(FlowController.ACTION, "BadAction");
		MuleMessage message = client.send(VM_CONTROL, testMessage, props);
		assertNotNull(message);
		assertNotNull(message.getExceptionPayload());
		assertTrue(message.getExceptionPayload().getException() instanceof UnsupportedOperationException);
		assertEquals("Unknown action BadAction. Action should be STOP, START, PAUSE or RESUME",message.getExceptionPayload().getMessage());
	}
	
	@Test
	public void testControlingBadFlowName() throws MuleException{
		MuleClient client = muleContext.getClient();
		String testMessage = "Test Message";
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(FlowController.FLOW_NAME, "BadName");
		props.put(FlowController.ACTION, FlowController.STOP);
		MuleMessage message = client.send(VM_CONTROL, testMessage, props);
		assertNotNull(message);
		assertNotNull(message.getExceptionPayload());
		assertTrue(message.getExceptionPayload().getException() instanceof MuleException);
		assertEquals("No flow found with name BadName",message.getExceptionPayload().getMessage());
	}
	
	@Test
	public void testSelfStoppingFlow() throws Exception{
		MuleClient client = muleContext.getClient();
		String testMessage = "Test Message";
		MuleMessage message = client.send(VM_SELF_STOPPING, testMessage, null);
		System.out.println(message);
		checkActionTaken(message,FlowController.STOP);
		try
		{
			message=client.send(VM_SELF_STOPPING, testMessage, null);
			fail("Exception should have been thrown!");
		}catch(DispatchException e)
		{
			assertTrue(e.getMessage().startsWith("Failed to route event via endpoint: DefaultOutboundEndpoint{endpointUri=vm://SelfStoppingFlow, connector=VMConnector"));
		}
	}
	
	private void checkActionTaken(MuleMessage message,String expectedAction)
	{
		assertNotNull(message);
		assertTrue(message.getInboundPropertyNames().contains(
				FlowController.ACTION_TAKEN));
		assertEquals(expectedAction,
				message.getInboundProperty(FlowController.ACTION_TAKEN));
	}
	
	private void doRoundTrip(String requestURI, String responseURI) throws Exception
	{
		MuleClient client = muleContext.getClient();
		String testMessage = "Test Message";
		client.dispatch(requestURI, testMessage, null);
		MuleMessage message = client.request(responseURI, TIMEOUT);
		assertNotNull(message);
		assertEquals(testMessage, message.getPayloadAsString());		
	}

	private void doControlTest(String flowName, String action1,
			String expectedaction1, String action2, String expectedaction2,
			String requestURI, String responseURI) throws Exception {
		MuleClient client = muleContext.getClient();
		String testMessage = "Test Message";
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(FlowController.FLOW_NAME, flowName);
		props.put(FlowController.ACTION, action1);
		MuleMessage message = client.send(VM_CONTROL, testMessage, props);
		assertNotNull(message);
		assertTrue(message.getInboundPropertyNames().contains(
				FlowController.ACTION_TAKEN));
		assertEquals(expectedaction1,
				message.getInboundProperty(FlowController.ACTION_TAKEN));
		client.dispatch(requestURI, testMessage, null);
		message = client.request(responseURI, TIMEOUT);
		assertNull(message);
		props.put(FlowController.ACTION, action2);
		message = client.send(VM_CONTROL, testMessage, props);
		assertNotNull(message);
		assertTrue(message.getInboundPropertyNames().contains(
				FlowController.ACTION_TAKEN));
		assertEquals(expectedaction2,
				message.getInboundProperty(FlowController.ACTION_TAKEN));
		message = client.request(responseURI, TIMEOUT * 2);
		assertNotNull(message);
		assertEquals(testMessage, message.getPayloadAsString());
	}

	@Override
	protected String getConfigResources() {
		return "processor/control/flow-controller.xml";
	}

}
