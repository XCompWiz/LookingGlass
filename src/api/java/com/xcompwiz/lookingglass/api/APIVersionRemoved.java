package com.xcompwiz.lookingglass.api;

/**
 * Thrown in the case of an API version having been removed entirely and no longer supported. This can be interpreted as the request being for below the minimum
 * supported version for the API.
 * @author xcompwiz
 */
public class APIVersionRemoved extends Exception {

	/**
	 * Generated Serial UID
	 */
	private static final long	serialVersionUID	= -7702376017254522430L;

	public APIVersionRemoved(String string) {
		super(string);
	}
}
