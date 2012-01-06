package org.ic.camel.tools.bean.control;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.ic.camel.tools.IntegrationCocktailConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteController {
	public static final String START = "START";
	public static final String STOP = "STOP";
	public static final String SUSPEND = "SUSPEND";
	public static final String RESUME = "RESUME";

	public static final String PREFIX = IntegrationCocktailConstants.PREFIX
			+ ".RouteController";
	public static final String ROUTE_ID = PREFIX + ".RouteId";
	public static final String ACTION_ID = PREFIX + ".ActionId";

	private String action = null;
	private String routeId = null;

	Logger logger = LoggerFactory.getLogger(RouteController.class);

	public void performAction(Exchange exchange) throws Exception {
		CamelContext context = exchange.getContext();

		// Check if action and method are set as headers
		String routeId = (String) exchange.getIn().getHeader(ROUTE_ID,
				getRouteId());
		String action = (String) exchange.getIn().getHeader(ACTION_ID,
				getAction());

		performAction(routeId, action, context);
	}

	private void performAction(String routeId, String action,
			CamelContext context) throws Exception {
		logger.debug("Excecuting " + action + " on " + routeId);
		if (action.equals(STOP)) {
			context.stopRoute(routeId);
		} else if (action.equals(START)) {
			context.startRoute(routeId);
		} else if (action.equals(SUSPEND)) {
			context.suspendRoute(routeId);
		} else if (action.equals(RESUME)) {
			context.resumeRoute(routeId);
		} else {
			throw new UnsupportedOperationException("Unknown action " + action
					+ ". Action should be STOP, START, SUSPEND or RESUME");
		}
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

}
