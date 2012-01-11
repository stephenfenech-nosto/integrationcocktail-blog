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
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRouteController extends CamelTestSupport {

	private static final Long SLEEP_SHORT = 500L;
	private static final Long SLEEP_LONG = 2500L;

	private static final Logger logger = LoggerFactory
			.getLogger(TestSupport.class);

	@EndpointInject(uri = "mock:result")
	protected MockEndpoint resultEndpoint;

	@Produce(uri = "direct:start")
	protected ProducerTemplate template;

	@Test
	public void testNormalFlow() throws Exception {
		String expectedBody = "Test Data";
		File testFile = new File("target/inbox/test.txt");
		FileUtils.writeStringToFile(testFile, expectedBody);

		resultEndpoint.expectedBodiesReceived(expectedBody);
		resultEndpoint.assertIsSatisfied(1000);
		sleepShort();
		assertFalse(testFile.exists());
	}

	@Test
	public void testStoppingFlow() throws Exception {
		DefaultRoute fileRoute = (DefaultRoute) context.getRoute("FileRoute");

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
		resultEndpoint.reset();
		headers.put(RouteController.ROUTE_ID, "FileRoute");
		headers.put(RouteController.ACTION_ID, RouteController.START);
		template.sendBodyAndHeaders(expectedBody, headers);
		assertReceived("test2.txt", expectedBody);

		// Send Pause Command
		headers.put(RouteController.ACTION_ID, RouteController.SUSPEND);
		template.sendBodyAndHeaders(expectedBody, headers);

		sendTestMessageWhileRouteIsNotConsuming("test3.txt", expectedBody);

		// Send Resume Command
		resultEndpoint.reset();
		headers.put(RouteController.ACTION_ID, RouteController.RESUME);
		template.sendBodyAndHeaders(expectedBody, headers);
		assertReceived("test3.txt", expectedBody);
	}

	@Test(expected = org.apache.camel.CamelExecutionException.class)
	public void testSendingOfBadAction() throws Exception {
		Map<String, Object> headers = new HashMap<String, Object>();
		// Send Start Command
		resultEndpoint.reset();
		headers.put(RouteController.ROUTE_ID, "FileRoute");
		headers.put(RouteController.ACTION_ID, "BadAction");
		template.sendBodyAndHeaders("Body", headers);
	}

	@Test
	public void testSuspendingSelf() throws Exception {
		DefaultRoute route = (DefaultRoute) context
				.getRoute("SuspendingSelfRoute");
		assertTrue(route.isStarted());
		File testFile = new File("target/inbox2/test.txt");
		FileUtils.writeStringToFile(testFile, "Data");
		sleepLong();
		assertFalse(testFile.exists());

		testFile = new File("target/inbox2/test2.txt");
		FileUtils.writeStringToFile(testFile, "Data");
		sleepShort();
		assertTrue(testFile.exists());
	}

	@Test
	public void testStoppingSelf() throws Exception {
		DefaultRoute route = (DefaultRoute) context
				.getRoute("StoppingSelfRoute");
		assertTrue(route.isStarted());
		File testFile = new File("target/inbox3/test.txt");
		FileUtils.writeStringToFile(testFile, "Data");
		sleepLong();
		assertFalse(testFile.exists());

		testFile = new File("target/inbox3/test2.txt");
		FileUtils.writeStringToFile(testFile, "Data");
		sleepShort();
		assertTrue(testFile.exists());
	}

	private void sendTestMessageWhileRouteIsNotConsuming(String filename,
			String expectedBody) throws IOException, InterruptedException {
		// Send test that should not go through
		resultEndpoint.reset();
		File testFile = new File("target/inbox/" + filename);
		FileUtils.writeStringToFile(testFile, expectedBody);
		resultEndpoint.expectedMessageCount(0);
		resultEndpoint.assertIsSatisfied(1000);
		assertTrue(testFile.exists());
	}

	private void assertReceived(String filename, String expectedBody)
			throws InterruptedException {
		resultEndpoint.expectedBodiesReceived(expectedBody);
		resultEndpoint.assertIsSatisfied(2000);
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
						"mock:result");
			}
		}, new RouteBuilder() {
			public void configure() throws Exception {
				RouteController routeController = new RouteController();
				from("file:./target/inbox2").routeId("SuspendingSelfRoute")
						.bean(routeController);
			}
		}, new RouteBuilder() {
			public void configure() throws Exception {
				RouteController routeController = new RouteController();
				routeController.setAction(RouteController.STOP);
				from("file:./target/inbox3").routeId("StoppingSelfRoute").bean(
						routeController);
			}
		} };
	}

	@BeforeClass
	public static void clearDirectories() {
		FileUtils.deleteQuietly(new File("./target/inbox"));
		FileUtils.deleteQuietly(new File("./target/inbox2"));
		FileUtils.deleteQuietly(new File("./target/inbox3"));
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
