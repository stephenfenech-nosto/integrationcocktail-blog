package org.ic.service;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
public class CocktailServiceTest extends AbstractSecurityTest {

	@Autowired
	CocktailService service;

	@Override
	public Class<?>[] getInterfaceClasses() {
		return new Class<?>[]{CocktailService.class};
	}

	@Override
	public Object getService() {
		return service;
	}

}
