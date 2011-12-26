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
	/**
	 * This is the test method which I will be extending and which 
	 * will check that all the methods in the interfaces are secured
	 */
	public void testAllMethodsInInterface() throws Throwable {

		// Get all the methods from the interface classes
		ArrayList<Method> methods = new ArrayList<Method>();
		for (Class<?> clazz : getInterfaceClasses()) {
			methods.addAll(Arrays.asList(clazz.getMethods()));
		}

		// Define an list which will contain the method names which are not
		// secured
		ArrayList<String> errorMethodNames = new ArrayList<String>();

		// Loop and invoke all the methods
		for (Method method : methods) {
			// If the method is not one of the ignored methods, invoke
			if (!isMethodIgnored(method.getName())) {
				// Get parameters and create a new ObjectArray which will
				// contain the parameters we are passing
				Class<?>[] parameters = method.getParameterTypes();
				Object[] params = new Object[parameters.length];
				// Loop through the parameters in order to initialise certain
				// types, especially non object types like long and int as you
				// see below. Please note that this is NOT an exhaustive list
				for (int i = 0; i < parameters.length; i++) {
					if (parameters[i].equals(long.class)) {
						params[i] = 0L;
					} else if (parameters[i].equals(int.class)) {
						params[i] = 0;
					}
				}
				// try invoking the method. If the method is successfull, it
				// means that it is NOT secured, so add the method to the
				// errorMethodNames.
				try {
					System.out.println("Invoking :" + method.toGenericString());
					method.invoke(getService(), params);
					errorMethodNames.add(method.toGenericString());
				} catch (InvocationTargetException e) {
					// the more common scenario is that an exception is thrown.
					// If it is an AuthenticationCredentialsNotFoundException,
					// that is exactly what we are looking for, so that means
					// that the method is secured and we are getting the
					// exception from the security aspect. If we get any other
					// exception, it means that the method is not secure. In my
					// case I am re-throwing an exception, however you could
					// also add the method name to the errorMethodNames. It is
					// normal that if a method that is not secured, an exception
					// is thrown since the parameters that we are passing are
					// mostly null or invalid.
					if (!(e.getTargetException() instanceof AuthenticationCredentialsNotFoundException)) {
						throw new Exception("The method " + method.getName()
								+ " is not secured and a "
								+ e.getTargetException().getClass().getName()
								+ " was thrown!", e.getTargetException());
					}
				}
			}
		}
		// Logging the error method names for convenince.
		System.out.println("The following public methods are NOT adviced: ");
		for (String s : errorMethodNames) {
			System.out.println("   " + s);
		}
		// If there are errorMethodNames, then fail.
		assertEquals(
				"The following public methods are NOT adviced: "
						+ Arrays.toString(errorMethodNames.toArray()), 0,
				errorMethodNames.size());
	}

	/**
	 * Utility method to check if the method name is contained
	 */
	public boolean isMethodIgnored(String methodName) {
		for (String s : getIgnoredMethods()) {
			if (s.equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method to be overloaded if you want certain methods to be ignored
	 */
	public String[] getIgnoredMethods() {
		return new String[0];
	}

	/**
	 * Abstract method which returns the interfaces that we want to test since a
	 * service might implement more than one
	 */
	public abstract Class<?>[] getInterfaceClasses();

	/**
	 * Abstract method which returns the service object on which we will invoke
	 * the methods.
	 */
	public abstract Object getService();

}
