package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.DimensionManager;

import com.xcompwiz.lookingglass.api.event.ClientWorldInfoEvent;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketRequestWorldInfo extends PacketHandlerBase {
	@SideOnly(Side.CLIENT)
	public static FMLProxyPacket createPacket(int xPos, int yPos, int zPos, int dim) {
		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		ByteBuf data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

		data.writeInt(dim);

		return buildPacket(data);
	}

	@Override
	public void handle(ByteBuf data, EntityPlayer player) {
		if (ModConfigs.disabled) return;
		int dim = data.readInt();

		if (!DimensionManager.isDimensionRegistered(dim)) return;
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ClientWorldInfoEvent(dim, (EntityPlayerMP) player));
		LookingGlassPacketManager.bus.sendTo(PacketWorldInfo.createPacket(dim), (EntityPlayerMP) player);
	}
}
