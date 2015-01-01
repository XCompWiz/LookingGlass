package com.xcompwiz.lookingglass.apiimpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.xcompwiz.lookingglass.api.APIInstanceProvider;
import com.xcompwiz.lookingglass.api.APIUndefined;
import com.xcompwiz.lookingglass.api.APIVersionRemoved;
import com.xcompwiz.lookingglass.api.APIVersionUndefined;
import com.xcompwiz.lookingglass.log.LoggerUtils;

/**
 * The implementation of the API provider interface. Instances of this class are given to mods requesting an API provider and bound to that mod's name. The
 * class also functions as the registration manager for what APIs we have available.
 */
public class APIProviderImpl implements APIInstanceProvider {
	private String	modname;

	public APIProviderImpl(String modname) {
		this.modname = modname;
	}

	public String getOwnerMod() {
		return modname;
	}

	private HashMap<String, Object>	instances	= new HashMap<String, Object>();

	// See parent/javadoc for doc
	@Override
	public Object getAPIInstance(String api) throws APIUndefined, APIVersionUndefined, APIVersionRemoved {
		Object ret = instances.get(api);
		// First, we check if the API has already been constructed up for this provider
		if (ret != null) return ret;
		// Get the id and version from the passed in arg
		String[] splitName = api.split("-");
		// If we can't get a name and version then we throw APIUndefined.
		if (splitName.length != 2) throw new APIUndefined(api);
		String apiname = splitName[0];
		int version = Integer.parseInt(splitName[1]);
		// Ask the magical constructor to provide us an instance of the API for the specified version.
		ret = constructAPIWrapper(modname, apiname, version);
		instances.put(api, ret);
		return ret;
	}

	private static Map<String, Map<Integer, WrapperBuilder>>	apiCtors;
	private static Map<String, Set<Integer>>					apiVersions;
	private static Map<String, Set<Integer>>					apiVersions_immutable_sets;
	private static Map<String, Set<Integer>>					apiVersions_immutable;

	/**
	 * This init function sets up the wrapper constructors for the different APIs and versions of APIs we support.
	 */
	public static void init() {
		// Skip if already initialized
		if (apiCtors != null) return;
		apiCtors = new HashMap<String, Map<Integer, WrapperBuilder>>();
		apiVersions = new HashMap<String, Set<Integer>>();
		// Immutable views to the internal stuff to allow for the getAvailableAPIs functionality without breaking containment
		apiVersions_immutable_sets = new HashMap<String, Set<Integer>>();
		apiVersions_immutable = Collections.unmodifiableMap(apiVersions_immutable_sets);

		// Register the APIs we support
		registerAPI("view", 1, new WrapperBuilder(LookingGlassAPIWrapper.class));
		registerAPI("view", 2, new WrapperBuilder(LookingGlassAPI2Wrapper.class));
		// Note that removed API versions should be registered as null.
	}

	private static void registerAPI(String apiname, int version, WrapperBuilder builder) {
		getVersions(apiname).add(version);
		getCtors(apiname).put(version, builder);
	}

	private static Map<Integer, WrapperBuilder> getCtors(String apiname) {
		Map<Integer, WrapperBuilder> ctors = apiCtors.get(apiname);
		if (ctors == null) {
			ctors = new HashMap<Integer, WrapperBuilder>();
			apiCtors.put(apiname, ctors);
		}
		return ctors;
	}

	private static Set<Integer> getVersions(String apiname) {
		Set<Integer> versions = apiVersions.get(apiname);
		if (versions == null) {
			versions = new HashSet<Integer>();
			apiVersions.put(apiname, versions);
			apiVersions_immutable_sets.put(apiname, Collections.unmodifiableSet(versions));
		}
		return versions;
	}

	/**
	 * ** This function is voodoo.** <br/> This is the function which actually calls the builders to produce the wrappers which implement the version of the requested API.
	 * @param owner The name of the mod wanting the API
	 * @param apiname The name of the API wanted
	 * @param version The version of the API wanted
	 * @return An object which is an instance of the interface matching the API version
	 * @throws APIUndefined The API requested doesn't exist
	 * @throws APIVersionUndefined The API requested exists, but the version requested is missing in the local environment
	 * @throws APIVersionRemoved The API requested exists, but the version requested has been removed and is no longer supported
	 */
	private static Object constructAPIWrapper(String owner, String apiname, int version) throws APIUndefined, APIVersionUndefined, APIVersionRemoved {
		// First, check to make sure we initialized before
		if (apiCtors == null) throw new RuntimeException("Something is broken. The LookingGlass API Provider hasn't constructed properly.");
		// Get the builders for the API we want
		Map<Integer, WrapperBuilder> ctors = apiCtors.get(apiname);
		// If there are no builders, then the API doesn't exist
		if (ctors == null) throw new APIUndefined(apiname);
		// If the builders collection doesn't have an entry for the version we wanted, it never existed
		if (!ctors.containsKey(version)) throw new APIVersionUndefined(apiname + "-" + version);
		// Get the builder entry
		WrapperBuilder ctor = ctors.get(version);
		// If the builder is null, then the API has been removed.
		if (ctor == null) throw new APIVersionRemoved(apiname + "-" + version);
		// Now, the magic itself. Use the builder to produce an instance of the API
		try {
			return ctor.newInstance(owner); // Poof!
		} catch (Exception e) {
			// If there are any problems then we need to report them. Theoretically there shouldn't be, but one never knows.
			LoggerUtils.error("Caught an exception while building an API wrapper. Go kick XCompWiz.");
			throw new RuntimeException("Caught an exception while building an API wrapper. Go kick XCompWiz.", e);
		}
	}

	@Override
	public Map<String, Set<Integer>> getAvailableAPIs() {
		return apiVersions_immutable;
	}
}
