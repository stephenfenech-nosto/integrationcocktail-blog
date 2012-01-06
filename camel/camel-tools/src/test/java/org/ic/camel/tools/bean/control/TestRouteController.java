package org.ic.camel.tools.bean.control;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultRoute;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.junit4.TestSupport;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRouteController extends CamelTestSupport {

	private static final Long SLEEP_SHORT = 500L;
	private static final Long SLEEP_LONG = 1500L;

	private static final Logger logger = LoggerFactory
			.getLogger(TestSupport.class);

	@EndpointInject(uri = "mock:fileResult")
	protected MockEndpoint fileResultEndpoint;

	@Produce(uri = "direct:start")
	protected ProducerTemplate template;

	@Test
	public void testNormalFlow() throws Exception {
		String expectedBody = "Test Data";
		File testFile = new File("target/inbox/test.txt");
		FileUtils.writeStringToFile(testFile, expectedBody);

		fileResultEndpoint.expectedBodiesReceived(expectedBody);
		fileResultEndpoint.assertIsSatisfied(1000);
		sleepShort();
		assertFalse(testFile.exists());
	}

	@Test
	public void testStoppingFlow() throws Exception {
		DefaultRoute fileRoute=(DefaultRoute) context.getRoute("FileRoute");
		
		// Send simple Test
		File testFile = new File("target/inbox/test1.txt");
		String expectedBody = "Test Data";
		FileUtils.writeStringToFile(testFile, expectedBody);
		assertReceived("test1.txt", expectedBody);

		// Send Stop Command
		Map<String, Object> headers = new HashMap<String, Object>();
		template.sendBodyAndHeaders(expectedBody, headers);

		sendTestMessageWhileRouteIsNotConsuming("test2.txt", expectedBody);

		// Send Start Command
		fileResultEndpoint.reset();
		headers.put(RouteController.ROUTE_ID, "FileRoute");
		headers.put(RouteController.ACTION_ID, RouteController.START);
		template.sendBodyAndHeaders(expectedBody, headers);
		assertReceived("test2.txt", expectedBody);

		// Send Pause Command
		headers.put(RouteController.ACTION_ID, RouteController.SUSPEND);
		template.sendBodyAndHeaders(expectedBody, headers);
		
		sendTestMessageWhileRouteIsNotConsuming("test3.txt", expectedBody);
		
		// Send Resume Command
		fileResultEndpoint.reset();
		headers.put(RouteController.ACTION_ID, RouteController.RESUME);
		template.sendBodyAndHeaders(expectedBody, headers);
		assertReceived("test3.txt", expectedBody);
	}

	@Test(expected=org.apache.camel.CamelExecutionException.class)
	public void testSendingOfBadAction() throws Exception {
		Map<String, Object> headers = new HashMap<String, Object>();
		// Send Start Command
		fileResultEndpoint.reset();
		headers.put(RouteController.ROUTE_ID, "FileRoute");
		headers.put(RouteController.ACTION_ID, "BadAction");
		template.sendBodyAndHeaders("Body", headers);
	}

	private void sendTestMessageWhileRouteIsNotConsuming(String filename,
			String expectedBody) throws IOException, InterruptedException {
		// Send test that should not go through
		fileResultEndpoint.reset();
		File testFile = new File("target/inbox/" + filename);
		FileUtils.writeStringToFile(testFile, expectedBody);
		fileResultEndpoint.expectedMessageCount(0);
		fileResultEndpoint.assertIsSatisfied(1000);
		assertTrue(testFile.exists());
	}

	private void assertReceived(String filename, String expectedBody)
			throws InterruptedException {
		fileResultEndpoint.expectedBodiesReceived(expectedBody);
		fileResultEndpoint.assertIsSatisfied(2000);
		sleepShort();
		assertFalse(new File("target/inbox/" + filename).exists());
	}

	@Override
	protected RouteBuilder[] createRouteBuilders() throws Exception {
		return new RouteBuilder[] { new RouteBuilder() {
			public void configure() {
				RouteController routeController = new RouteController();
				routeController.setRouteId("FileRoute");
				routeController.setAction(RouteController.STOP);
				from("direct:start").routeId("ControllerRoute")
						.bean(routeController).to("mock:controllerResult");
			}
		}, new RouteBuilder() {
			public void configure() throws Exception {
				from("file:./target/inbox").routeId("FileRoute").to(
						"mock:fileResult");
			}
		}, new RouteBuilder() {
			public void configure() throws Exception {
				RouteController routeController = new RouteController();
				routeController.setAction(RouteController.STOP);
				from("file:./target/inbox2").routeId("StoppingSelfRoute")
						.bean(routeController).to("mock:fileResult");
			}
		} };
	}

	private void sleepShort() {
		try {
			Thread.sleep(SLEEP_SHORT);
		} catch (InterruptedException e) {
			logger.warn("Sleep Short Interrupted!");
		}
	}
	
	private void sleepLong() {
		try {
			Thread.sleep(SLEEP_LONG);
		} catch (InterruptedException e) {
			logger.warn("Sleep Long Interrupted!");
		}
	}
}
