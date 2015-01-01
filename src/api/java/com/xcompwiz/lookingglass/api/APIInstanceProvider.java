package com.xcompwiz.lookingglass.api;

import java.util.Map;
import java.util.Set;

/**
 * The purpose of this interface is simply to provide instances of the API interfaces. Request an instance via IMC (see below). This interface supports
 * providing multiple versions of an API, so you can request ex. 'symbol-1' and always get the same interface, enabling the API to move forward without breaking
 * compatibility. The provider instance provided to you, as well as any API interface instances created by it, will belong to the mod which requested the
 * provider.
 * @author xcompwiz
 */
public interface APIInstanceProvider {

	/**
	 * Returns a constructed version of the requested API. If the API requested doesn't exist then an exception will be thrown. Be wary when attempting to cast
	 * the returned instance, as, if you try using an interface not included in the class path, you may get missing definition crashes. It is wiser to, after
	 * verifying success, pass the object to another class dedicated to handling the casting.
	 * @param api The name of the API and version desired, formatted as ex. "symbol-1".
	 * @return The requested API instance as an Object. If you get an object, it is guaranteed to be of the requested API and version.
	 */
	public Object getAPIInstance(String api) throws APIUndefined, APIVersionUndefined, APIVersionRemoved;

	/**
	 * Returns a collection of all available APIs by name and what versions are supported in this environment.
	 * @return A map of API names and their available versions
	 */
	public Map<String, Set<Integer>> getAvailableAPIs();

//@formatter:off
	/*	Example Usage
	In order to get an instance of this class, send an IMC message to LookingGlass with the key "API" and a string value which is a static method (with classpath)
	which takes an instance of APIInstanceProvider as it's only param.
	Example: FMLInterModComms.sendMessage("LookingGlass", "API", "com.xcompwiz.newmod.integration.lookingglass.register");

 	public static void register(APIInstanceProvider provider) {
		try {
			Object apiinst = provider.getAPIInstance("awesomeAPI-3");
			OtherClass.apiGet(apiinst); //At this point, we've got an object of the right interface.
		} catch (APIUndefined e) {
			// The API we requested doesn't exist.  Give up with a nice log message.
		} catch (APIVersionUndefined e) {
			// The API we requested exists, but the version we wanted is missing in the local environment. We can try falling back to an older version.
		} catch (APIVersionRemoved e) {
			// The API we requested exists, but the version we wanted has been removed and is no longer supported. Better update.
		}
	}
	*/
//formatter:on
}
