package org.ic.service;

import org.ic.domain.Cocktail;
import org.springframework.security.access.prepost.PreAuthorize;

public interface CocktailService {
	public Integer saveCocktail(Cocktail cocktail);
	public Cocktail getCocktail(Integer id);
	public Cocktail deleteCocktail(Integer id);
}
