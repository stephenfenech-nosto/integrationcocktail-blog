package org.ic.service;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

public abstract class AbstractSecurityTest {
	@Test
	public void testAllMethodsInInterface() throws Throwable {
		ArrayList<Method> methods = new ArrayList<Method>();
		for(Class<?> clazz:getInterfaceClasses())
		{
			methods.addAll(Arrays.asList(clazz.getMethods()));
		}		

		ArrayList<String> errorMethodNames = new ArrayList<String>();

		for (Method method : methods) {
			if (!isMethodIgnored(method.getName())) {

				Class<?>[] parameters = method.getParameterTypes();
				Object[] params = new Object[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					if (parameters[i].equals(long.class)) {
						params[i] = 0L;
					} else if (parameters[i].equals(int.class)) {
						params[i] = 0;
					} else if (parameters[i].equals(java.lang.String.class)) {
						params[i] = "0";
					}
				}
				try {
					System.out.println("Invoking :" + method.toGenericString());
					method.invoke(getService(), params);
					errorMethodNames.add(method.toGenericString());
				} catch (InvocationTargetException e) {
					if (!(e.getTargetException() instanceof AuthenticationCredentialsNotFoundException)) {
						throw new Exception("The method "+method.getName()+" is not secured and a "+e.getTargetException().getClass().getName()+" was thrown!",e.getTargetException());
					}
				}
			}
		}
		System.out.println("The following public methods are NOT adviced: ");
		for(String s:errorMethodNames)
		{
			System.out.println("   "+s);
		}
		assertEquals("The following public methods are NOT adviced: " + Arrays.toString(errorMethodNames.toArray()), 0, errorMethodNames.size());
	}

	public boolean isMethodIgnored(String methodName) {
		for (String s : getIgnoredMethods()) {
			if (s.equals(methodName)) {
				return true;
			}
		}
		return false;
	}
	
	public String[] getIgnoredMethods(){
		return new String[0];
	}
	public abstract Class<?>[] getInterfaceClasses();
	public abstract Object getService();
	
}
