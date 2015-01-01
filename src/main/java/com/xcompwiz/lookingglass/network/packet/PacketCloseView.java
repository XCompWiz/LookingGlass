package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketCloseView extends PacketHandlerBase {
	@SideOnly(Side.CLIENT)
	public static FMLProxyPacket createPacket(WorldView worldview) {
		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		ByteBuf data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

		return buildPacket(data);
	}

	@Override
	public void handle(ByteBuf data, EntityPlayer player) {
		if (ModConfigs.disabled) return;

		//TODO: make closing viewpoint aware.  See PacketCreateView
	}
}
