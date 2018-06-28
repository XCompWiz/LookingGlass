package com.xcompwiz.lookingglass.proxyworld;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ModConfigs {
	private static final String	CATAGORY_SERVER	= "server";

	public static boolean		disabled		= false;

	public static int			dataRate		= 2048;

	public static byte			renderDistance	= 7;

	public static void loadConfigs(Configuration config) {
		Property off = config.get(CATAGORY_SERVER, "disabled", disabled);
		off.setComment("On the client this disables other world renders entirely, preventing world requests. On the server this disables sending world info to all clients.");
		disabled = off.getBoolean(disabled);

		Property d = config.get(CATAGORY_SERVER, "datarate", dataRate);
		d.setComment("The number of bytes to send per tick before the server cuts off sending. Only applies to other-world chunks. Default: " + dataRate);
		dataRate = d.getInt(dataRate);

		if (dataRate <= 0) disabled = true;

		if (config.hasChanged()) config.save();
	}

}
