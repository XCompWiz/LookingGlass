package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.api.event.ClientWorldInfoEvent;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketRequestWorldInfo extends PacketHandlerBase {
	@SideOnly(Side.CLIENT)
	public static FMLProxyPacket createPacket(int xPos, int yPos, int zPos, int dim) {
		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		PacketBuffer data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

		data.writeInt(dim);

		return buildPacket(data);
	}

	@Override
	public void handle(ByteBuf data, EntityPlayer player) {
		if (ModConfigs.disabled) return;
		int dim = data.readInt();

		if (!DimensionManager.isDimensionRegistered(dim)) return;
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ClientWorldInfoEvent(dim, (EntityPlayerMP) player));
		LookingGlassPacketManager.bus.sendTo(PacketWorldInfo.createPacket(player.getServer(), dim), (EntityPlayerMP) player);
	}
}
