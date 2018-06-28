package com.xcompwiz.lookingglass.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;

public class MathUtils {

	public static Vec3d readCoordinates(ByteBuf data) {
		Vec3d coords = new Vec3d(data.readDouble(), data.readDouble(), data.readDouble());
		return coords;
	}

}
