package org.ic.service;

import static org.junit.Assert.assertEquals;

import org.ic.domain.Cocktail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
public class ServiceTest {
	
	@Autowired
	CocktailService service;
	
	@Test
	public void testCocktailService()
	{
		logIn();
		
		Cocktail cocktail=new Cocktail();
		cocktail.setName("White Russian");
		cocktail.setDescription("The cocktail answer for low calcium.");
		cocktail.setInstructions("Vodka, Kahlua and milk! Done.");
		
		assertEquals(0,service.saveCocktail(cocktail).intValue());
		assertEquals(cocktail,service.getCocktail(0));
	}
	
	private Authentication logIn()
	{
		SecurityContext securityContext=SecurityContextHolder.getContext();
		Authentication authentication=new UsernamePasswordAuthenticationToken("admin","admin");
		securityContext.setAuthentication(authentication);
		return authentication;
	}

}
