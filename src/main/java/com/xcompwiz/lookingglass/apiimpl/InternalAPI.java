package com.xcompwiz.lookingglass.apiimpl;

import java.util.HashMap;

import com.xcompwiz.lookingglass.api.APIInstanceProvider;

/**
 * This class simply manages the construction and tracking of the API provider instances. The Mystcraft version was enormously more complex and did lots more,
 * but I simplified it here. The class is intentionally not named APIInstanceProviderProvider....
 */
public class InternalAPI {

	private static HashMap<String, APIInstanceProvider>	instances	= new HashMap<String, APIInstanceProvider>();

	public synchronized static APIInstanceProvider getAPIProviderInstance(String modname) {
		APIInstanceProvider instance = instances.get(modname);
		if (instance == null) {
			instance = new APIProviderImpl(modname);
			instances.put(modname, instance);
		}
		return instance;
	}

}
