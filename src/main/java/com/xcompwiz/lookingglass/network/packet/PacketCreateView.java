package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import com.xcompwiz.lookingglass.api.event.ClientWorldInfoEvent;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.proxyworld.ChunkFinder;
import com.xcompwiz.lookingglass.proxyworld.ChunkFinderManager;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketCreateView extends PacketHandlerBase {
	@SideOnly(Side.CLIENT)
	public static FMLProxyPacket createPacket(WorldView worldview) {
		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		ByteBuf data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

		int x = 0;
		int y = -1;
		int z = 0;
		if (worldview.coords != null) {
			x = worldview.coords.posX >> 4;
			y = worldview.coords.posY >> 4;
			z = worldview.coords.posZ >> 4;
		}

		data.writeInt(worldview.getWorldObj().provider.dimensionId);
		data.writeInt(x);
		data.writeInt(y);
		data.writeInt(z);
		data.writeByte(Math.min(ModConfigs.renderDistance, Minecraft.getMinecraft().gameSettings.renderDistanceChunks));

		return buildPacket(data);
	}

	@Override
	public void handle(ByteBuf data, EntityPlayer player) {
		if (ModConfigs.disabled) return;
		int dim = data.readInt();
		int xPos = data.readInt();
		int yPos = data.readInt();
		int zPos = data.readInt();
		byte renderDistance = data.readByte();

		if (!DimensionManager.isDimensionRegistered(dim)) return;
		WorldServer world = MinecraftServer.getServer().worldServerForDimension(dim);
		if (world == null) return;
		int x;
		int y;
		int z;
		if (yPos < 0) {
			ChunkCoordinates c = world.getSpawnPoint();
			x = c.posX >> 4;
			y = c.posY >> 4;
			z = c.posZ >> 4;
		} else {
			x = xPos;
			y = yPos;
			z = zPos;
		}
		if (renderDistance > ModConfigs.renderDistance) renderDistance = ModConfigs.renderDistance;
		ChunkFinderManager.instance.addFinder(new ChunkFinder(new ChunkCoordinates(x, y, z), dim, world.getChunkProvider(), player, renderDistance));
		//TODO: Add to tracking list.  Send time/data updates at intervals. Keep in mind to catch player disconnects when tracking clients.
		//Register ChunkFinder, and support change of finder location.
		//TODO: This is a repeat of the handling of PacketRequestWorldInfo
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ClientWorldInfoEvent(dim, (EntityPlayerMP) player));
		LookingGlassPacketManager.bus.sendTo(PacketWorldInfo.createPacket(dim), (EntityPlayerMP) player);
	}
}
