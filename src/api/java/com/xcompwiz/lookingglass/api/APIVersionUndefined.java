package com.xcompwiz.lookingglass.api;

/**
 * Thrown in the case of an API version not being available (ex. requesting 'symbol-4096')
 * @author xcompwiz
 */
public class APIVersionUndefined extends Exception {

	/**
	 * Generated Serial UID
	 */
	private static final long	serialVersionUID	= -6195164554156957083L;

	public APIVersionUndefined(String string) {
		super(string);
	}
}
