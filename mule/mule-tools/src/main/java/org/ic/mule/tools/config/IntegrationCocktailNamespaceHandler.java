package org.ic.mule.tools.config;

import org.ic.mule.tools.processor.control.FlowController;
import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class IntegrationCocktailNamespaceHandler extends NamespaceHandlerSupport{

	@Override
	public void init() {
		registerBeanDefinitionParser("flow-controller",new ChildDefinitionParser("messageProcessor",
	            FlowController.class));
	}

}
