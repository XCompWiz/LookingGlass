package com.xcompwiz.lookingglass.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.Vec3;

public class MathUtils {

	public static Vec3 readCoordinates(ByteBuf data) {
		Vec3 coords = Vec3.createVectorHelper(data.readDouble(), data.readDouble(), data.readDouble());
		return coords;
	}

}
