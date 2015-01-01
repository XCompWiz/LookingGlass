package com.xcompwiz.lookingglass.apiimpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This is a bit of a magic class. We use it to build API instances (API wrappers). It requires that the target class have a constructor which takes a string.
 * It passes the API instance "owner" to the constructor when building the wrapper instance.  Basically a cheap and easy class factory.
 */
public class WrapperBuilder {

	private final Constructor	itemCtor;

	public WrapperBuilder(Class clazz) {
		try {
			itemCtor = clazz.getConstructor(String.class);
		} catch (Exception e) {
			throw new RuntimeException("LookingGlass has derped.", e);
		}
	}

	/**
	 * Called by the APIProviderImpl to construct the API wrapper passed to it on its construction.
	 * @param owner
	 * @return
	 */
	public Object newInstance(String owner) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		return itemCtor.newInstance(owner);
	}

}
