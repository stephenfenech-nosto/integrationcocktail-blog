package org.ic.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.ic.domain.Cocktail;
import org.ic.service.CocktailService;
import org.springframework.security.access.prepost.PreAuthorize;

public class CocktailServiceImpl implements CocktailService{

	List<Cocktail> cocktails=new ArrayList<Cocktail>();
	
	@Override
	@PreAuthorize(value="isAuthenticated()")
	public Integer saveCocktail(Cocktail cocktail) {
		if(cocktail.getId()==null)
		{
			cocktails.add(cocktail);
			cocktail.setId(cocktails.indexOf(cocktail));
			return cocktail.getId();
		}else
		{
			cocktails.set(cocktail.getId(), cocktail);
			return cocktail.getId();
		}
	}

	@Override
	@PreAuthorize(value="isAuthenticated()")
	public Cocktail getCocktail(Integer id) {
		return cocktails.get(id);
	}
	
	@Override
	//Oops forgot to sort lock this method!
	public Cocktail deleteCocktail(Integer id) {
		return cocktails.remove(id.intValue());
	}

}
