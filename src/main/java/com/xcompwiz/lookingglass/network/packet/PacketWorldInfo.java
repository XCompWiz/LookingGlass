package com.xcompwiz.lookingglass.network.packet;

import java.util.Collection;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.log.LoggerUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

/**
 * Based on code from Ken Butler/shadowking97
 */
public class PacketWorldInfo extends PacketHandlerBase {

	public static FMLProxyPacket createPacket(MinecraftServer server, int dimension) {
		WorldServer world = server.getWorld(dimension);
		if (world == null) {
			LoggerUtils.warn("Server-side world for dimension %i is null!", dimension);
			return null;
		}
		BlockPos cc = world.provider.getSpawnPoint();
		int posX = cc.getX();
		int posY = cc.getY();
		int posZ = cc.getZ();
		int skylightSubtracted = world.getSkylightSubtracted();
		float thunderingStrength = world.thunderingStrength;
		float rainingStrength = world.rainingStrength;
		long worldTime = world.provider.getWorldTime();

		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		PacketBuffer data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

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
		if (proxyworld.provider.getDimension() != dimension) return;

		BlockPos cc = new BlockPos(posX, posY, posZ);
		Collection<WorldView> views = ProxyWorldManager.getWorldViews(dimension);
		for (WorldView view : views) {
			view.updateWorldSpawn(cc);
		}
		proxyworld.setSpawnPoint(cc);
		proxyworld.setSkylightSubtracted(skylightSubtracted);
		proxyworld.thunderingStrength = thunderingStrength;
		proxyworld.setRainStrength(rainingStrength);
		proxyworld.setWorldTime(worldTime);
	}
}
