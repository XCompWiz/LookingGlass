package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;

import java.util.Collection;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.log.LoggerUtils;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;

/**
 * Based on code from Ken Butler/shadowking97
 */
public class PacketWorldInfo extends PacketHandlerBase {

	public static FMLProxyPacket createPacket(int dimension) {
		WorldServer world = MinecraftServer.getServer().worldServerForDimension(dimension);
		if (world == null) {
			LoggerUtils.warn("Server-side world for dimension %i is null!", dimension);
			return null;
		}
		ChunkCoordinates cc = world.provider.getSpawnPoint();
		int posX = cc.posX;
		int posY = cc.posY;
		int posZ = cc.posZ;
		int skylightSubtracted = world.skylightSubtracted;
		float thunderingStrength = world.thunderingStrength;
		float rainingStrength = world.rainingStrength;
		long worldTime = world.provider.getWorldTime();

		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		ByteBuf data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

		data.writeInt(dimension);
		data.writeInt(posX);
		data.writeInt(posY);
		data.writeInt(posZ);
		data.writeInt(skylightSubtracted);
		data.writeFloat(thunderingStrength);
		data.writeFloat(rainingStrength);
		data.writeLong(worldTime);

		return buildPacket(data);
	}

	@Override
	public void handle(ByteBuf in, EntityPlayer player) {
		int dimension = in.readInt();
		int posX = in.readInt();
		int posY = in.readInt();
		int posZ = in.readInt();
		int skylightSubtracted = in.readInt();
		float thunderingStrength = in.readFloat();
		float rainingStrength = in.readFloat();
		long worldTime = in.readLong();

		WorldClient proxyworld = ProxyWorldManager.getProxyworld(dimension);

		if (proxyworld == null) return;
		if (proxyworld.provider.dimensionId != dimension) return;

		ChunkCoordinates cc = new ChunkCoordinates();
		cc.set(posX, posY, posZ);
		Collection<WorldView> views = ProxyWorldManager.getWorldViews(dimension);
		for (WorldView view : views) {
			view.updateWorldSpawn(cc);
		}
		proxyworld.setSpawnLocation(posX, posY, posZ);
		proxyworld.skylightSubtracted = skylightSubtracted;
		proxyworld.thunderingStrength = thunderingStrength;
		proxyworld.setRainStrength(rainingStrength);
		proxyworld.setWorldTime(worldTime);
	}
}
