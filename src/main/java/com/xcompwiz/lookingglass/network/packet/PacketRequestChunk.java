package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.network.ServerPacketDispatcher;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class PacketRequestChunk extends PacketHandlerBase {
	public static FMLProxyPacket createPacket(int xPos, int yPos, int zPos, int dim) {
		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		PacketBuffer data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

		data.writeInt(dim);
		data.writeInt(xPos);
		data.writeInt(yPos);
		data.writeInt(zPos);

		return buildPacket(data);
	}

	@Override
	public void handle(ByteBuf data, EntityPlayer player) {
		if (ModConfigs.disabled) return;
		int dim = data.readInt();
		int xPos = data.readInt();
		int yPos = data.readInt();
		int zPos = data.readInt();

		if (!DimensionManager.isDimensionRegistered(dim)) return;
		WorldServer world = player.getServer().getWorld(dim);
		if (world == null) return;
		Chunk chunk = world.getChunkFromChunkCoords(xPos, zPos);
		if (!chunk.isLoaded()) chunk = world.getChunkProvider().loadChunk(xPos, zPos);
		ServerPacketDispatcher.getInstance().addPacket(player, PacketChunkInfo.createPacket(chunk, true, yPos, dim));
	}
}
