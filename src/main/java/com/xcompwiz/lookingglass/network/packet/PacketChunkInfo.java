package com.xcompwiz.lookingglass.network.packet;

import java.io.IOException;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.log.LoggerUtils;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

// With thanks to Runemoro for cleaning this up
public class PacketChunkInfo extends PacketHandlerBase {

	public static FMLProxyPacket createPacket(Chunk chunk, int subid, int dim) {
		try {
			PacketBuffer data = PacketHandlerBase.createDataBuffer(PacketChunkInfo.class);
			data.writeInt(dim);
			SPacketChunkData packet = new SPacketChunkData(chunk, 0xFFFF); // TODO don't resend whole chunk
			packet.writePacketData(data);
			return buildPacket(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void handle(PacketBuffer in, EntityPlayer player) {
		try {
			int dim = in.readInt();
			SPacketChunkData packet = new SPacketChunkData();
			packet.readPacketData(in);
			handleChunkData(dim, packet);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void handleChunkData(int dim, SPacketChunkData packet) {
		WorldClient proxyworld = ProxyWorldManager.getProxyworld(dim);
		if (proxyworld == null) return;
		if (proxyworld.provider.getDimension() != dim) return;

		// Reference: NetHandlePlayClient public void handleChunkData(SPacketChunkData packetIn)

		//TODO: Test to see if this first part is even necessary
		Chunk chunk = proxyworld.getChunkFromChunkCoords(packet.getChunkX(), packet.getChunkZ());
		if (chunk.isLoaded()) {
			LoggerUtils.debug("Skipping loaded chunk at " + packet.getChunkX() + " " + packet.getChunkZ());
		} else {
			LoggerUtils.debug("Setting chunk info for " + packet.getChunkX() + " " + packet.getChunkZ());

			if (packet.isFullChunk()) {
				proxyworld.doPreChunk(packet.getChunkX(), packet.getChunkZ(), true);
			}

			proxyworld.invalidateBlockReceiveRegion(packet.getChunkX() << 4, 0, packet.getChunkZ() << 4, (packet.getChunkX() << 4) + 15, 256, (packet.getChunkZ() << 4) + 15);
			chunk.read(packet.getReadBuffer(), packet.getExtractedSize(), packet.isFullChunk());
			proxyworld.markBlockRangeForRenderUpdate(packet.getChunkX() << 4, 0, packet.getChunkZ() << 4, (packet.getChunkX() << 4) + 15, 256, (packet.getChunkZ() << 4) + 15);

			if (!packet.isFullChunk() || !(proxyworld.provider instanceof WorldProviderSurface)) {
				chunk.resetRelightChecks();
			}

			for (NBTTagCompound nbttagcompound : packet.getTileEntityTags()) {
				BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
				TileEntity tileentity = proxyworld.getTileEntity(blockpos);

				if (tileentity != null) {
					tileentity.handleUpdateTag(nbttagcompound);
				}
			}
		}

		for (WorldView activeview : ProxyWorldManager.getWorldViews(proxyworld.provider.getDimension())) { // TODO: dim?
			activeview.onChunkReceived(packet.getChunkX(), packet.getChunkZ());
		}
	}
}
