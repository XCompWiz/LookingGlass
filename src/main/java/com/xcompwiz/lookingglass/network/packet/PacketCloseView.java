package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketCloseView extends PacketHandlerBase {
	@SideOnly(Side.CLIENT)
	public static FMLProxyPacket createPacket(WorldView worldview) {
		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		PacketBuffer data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

		return buildPacket(data);
	}

	@Override
	public void handle(PacketBuffer data, EntityPlayer player) {
		if (ModConfigs.disabled) return;

		//TODO: make closing viewpoint aware.  See PacketCreateView
	}
}
