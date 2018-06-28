package com.xcompwiz.lookingglass.network.packet;

import java.util.concurrent.Semaphore;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.log.LoggerUtils;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S21PacketChunkData.Extracted;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

/**
 * Based on code from Ken Butler/shadowking97
 */
public class PacketChunkInfo extends PacketHandlerBase {
	private static byte[]		inflatearray;
	private static byte[]		dataarray;
	private static Semaphore	deflateGate	= new Semaphore(1);

	private static int deflate(byte[] chunkData, byte[] compressedChunkData) {
		Deflater deflater = new Deflater(-1);
		if (compressedChunkData == null) return 0;
		int bytesize = 0;
		try {
			deflater.setInput(chunkData, 0, chunkData.length);
			deflater.finish();
			bytesize = deflater.deflate(compressedChunkData);
		} finally {
			deflater.end();
		}
		return bytesize;
	}

	public static FMLProxyPacket createPacket(Chunk chunk, boolean includeinit, int subid, int dim) {
		int xPos = chunk.x;
		int zPos = chunk.z;
		Extracted extracted = getMapChunkData(chunk, includeinit, subid);
		int yMSBPos = extracted.field_150281_c;
		int yPos = extracted.field_150280_b;
		byte[] chunkData = extracted.field_150282_a;

		deflateGate.acquireUninterruptibly();
		byte[] compressedChunkData = new byte[chunkData.length];
		int len = deflate(chunkData, compressedChunkData);
		deflateGate.release();

		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		ByteBuf data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

		data.writeInt(dim);
		data.writeInt(xPos);
		data.writeInt(zPos);
		data.writeBoolean(includeinit);
		data.writeShort((short) (yPos & 65535));
		data.writeShort((short) (yMSBPos & 65535));
		data.writeInt(len);
		data.writeInt(chunkData.length);
		data.ensureWritable(len);
		data.writeBytes(compressedChunkData, 0, len);

		return buildPacket(data);
	}

	@Override
	public void handle(ByteBuf in, EntityPlayer player) {
		int dim = in.readInt();
		int xPos = in.readInt();
		int zPos = in.readInt();
		boolean reqinit = in.readBoolean();
		short yPos = in.readShort();
		short yMSBPos = in.readShort();
		int compressedsize = in.readInt();
		int uncompressedsize = in.readInt();
		byte[] chunkData = inflateChunkData(in, compressedsize, uncompressedsize);

		if (chunkData == null) {
			LookingGlassPacketManager.bus.sendToServer(PacketRequestChunk.createPacket(xPos, yPos, zPos, dim));
			LoggerUtils.error("Chunk decompression failed: %d\t:\t%d\t\t%d : %d\n", yMSBPos, yPos, compressedsize, uncompressedsize);
			return;
		}
		handle(player, chunkData, dim, xPos, zPos, reqinit, yPos, yMSBPos);
	}

	public void handle(EntityPlayer player, byte[] chunkData, int dim, int xPos, int zPos, boolean reqinit, short yPos, short yMSBPos) {
		WorldClient proxyworld = ProxyWorldManager.getProxyworld(dim);
		if (proxyworld == null) return;
		if (proxyworld.provider.dimensionId != dim) return;

		//TODO: Test to see if this first part is even necessary
		Chunk chunk = proxyworld.getChunkProvider().provideChunk(xPos, zPos);
		if (reqinit && (chunk == null || chunk.isEmpty())) {
			if (yPos == 0) {
				proxyworld.doPreChunk(xPos, zPos, false);
				return;
			}
			proxyworld.doPreChunk(xPos, zPos, true);
		}
		// End possible removal section
		proxyworld.invalidateBlockReceiveRegion(xPos << 4, 0, zPos << 4, (xPos << 4) + 15, 256, (zPos << 4) + 15);
		chunk = proxyworld.getChunkFromChunkCoords(xPos, zPos);
		if (reqinit && (chunk == null || chunk.isEmpty())) {
			proxyworld.doPreChunk(xPos, zPos, true);
			chunk = proxyworld.getChunkFromChunkCoords(xPos, zPos);
		}
		if (chunk != null) {
			chunk.fillChunk(chunkData, yPos, yMSBPos, reqinit);
			receivedChunk(proxyworld, xPos, zPos);
			if (!reqinit || !(proxyworld.provider instanceof WorldProviderSurface)) {
				chunk.resetRelightChecks();
			}
		}
	}

	public void receivedChunk(WorldClient worldObj, int cx, int cz) {
		worldObj.markBlockRangeForRenderUpdate(cx << 4, 0, cz << 4, (cx << 4) + 15, 256, (cz << 4) + 15);
		Chunk c = worldObj.getChunkFromChunkCoords(cx, cz);
		if (c == null || c.isEmpty()) return;

		for (WorldView activeview : ProxyWorldManager.getWorldViews(worldObj.provider.dimensionId)) {
			activeview.onChunkReceived(cx, cz);
		}

		int x = (cx << 4);
		int z = (cz << 4);
		for (int y = 0; y < worldObj.getActualHeight(); y += 16) {
			if (c.isEmptyBetween(y, y)) continue;
			for (int x2 = 0; x2 < 16; ++x2) {
				for (int z2 = 0; z2 < 16; ++z2) {
					for (int y2 = 0; y2 < 16; ++y2) {
						int lx = x + x2;
						int ly = y + y2;
						int lz = z + z2;
						BlockPos pos = new BlockPos(lx, ly, lz);
						if (worldObj.getTileEntity(pos) != null) {
							LookingGlassPacketManager.bus.sendToServer(PacketRequestTE.createPacket(lx, ly, lz, worldObj.provider.getDimension()));
						}
					}
				}
			}
		}
	}

	private byte[] inflateChunkData(ByteBuf in, int compressedsize, int uncompressedsize) {
		if (inflatearray == null || inflatearray.length < compressedsize) {
			inflatearray = new byte[compressedsize];
		}
		in.readBytes(inflatearray, 0, compressedsize);
		byte[] chunkData = new byte[uncompressedsize];
		Inflater inflater = new Inflater();
		inflater.setInput(inflatearray, 0, compressedsize);

		try {
			inflater.inflate(chunkData);
		} catch (DataFormatException e) {
			return null;
		} finally {
			inflater.end();
		}
		return chunkData;
	}

	public static Extracted getMapChunkData(Chunk chunk, boolean includeinit, int subid) {
		int j = 0;
		ExtendedBlockStorage[] aextendedblockstorage = chunk.getBlockStorageArray();
		int k = 0;
		S21PacketChunkData.Extracted extracted = new S21PacketChunkData.Extracted();
		if (dataarray == null || dataarray.length < 196864) {
			dataarray = new byte[196864];
		}
		byte[] abyte = dataarray;

		if (includeinit) {
			chunk.sendUpdates = true;
		}

		int l;

		for (l = 0; l < aextendedblockstorage.length; ++l) {
			if (aextendedblockstorage[l] != null && (!includeinit || !aextendedblockstorage[l].isEmpty()) && (subid & 1 << l) != 0) {
				extracted.field_150280_b |= 1 << l;

				if (aextendedblockstorage[l].getBlockMSBArray() != null) {
					extracted.field_150281_c |= 1 << l;
					++k;
				}
			}
		}

		for (l = 0; l < aextendedblockstorage.length; ++l) {
			if (aextendedblockstorage[l] != null && (!includeinit || !aextendedblockstorage[l].isEmpty()) && (subid & 1 << l) != 0) {
				byte[] abyte1 = aextendedblockstorage[l].getBlockLSBArray();
				System.arraycopy(abyte1, 0, abyte, j, abyte1.length);
				j += abyte1.length;
			}
		}

		NibbleArray nibblearray;

		for (l = 0; l < aextendedblockstorage.length; ++l) {
			if (aextendedblockstorage[l] != null && (!includeinit || !aextendedblockstorage[l].isEmpty()) && (subid & 1 << l) != 0) {
				nibblearray = aextendedblockstorage[l].getMetadataArray();
				System.arraycopy(nibblearray.getData(), 0, abyte, j, nibblearray.getData().length);
				j += nibblearray.getData().length;
			}
		}

		for (l = 0; l < aextendedblockstorage.length; ++l) {
			if (aextendedblockstorage[l] != null && (!includeinit || !aextendedblockstorage[l].isEmpty()) && (subid & 1 << l) != 0) {
				nibblearray = aextendedblockstorage[l].getBlocklightArray();
				System.arraycopy(nibblearray.getData(), 0, abyte, j, nibblearray.getData().length);
				j += nibblearray.getData().length;
			}
		}

		if (!chunk.getWorld().provider.hasSkyLight()) {
			for (l = 0; l < aextendedblockstorage.length; ++l) {
				if (aextendedblockstorage[l] != null && (!includeinit || !aextendedblockstorage[l].isEmpty()) && (subid & 1 << l) != 0) {
					nibblearray = aextendedblockstorage[l].getSkyLight();
					System.arraycopy(nibblearray.getData(), 0, abyte, j, nibblearray.getData().length);
					j += nibblearray.getData().length;
				}
			}
		}

		if (k > 0) {
			for (l = 0; l < aextendedblockstorage.length; ++l) {
				if (aextendedblockstorage[l] != null && (!includeinit || !aextendedblockstorage[l].isEmpty()) && aextendedblockstorage[l].getBlockMSBArray() != null && (subid & 1 << l) != 0) {
					nibblearray = aextendedblockstorage[l].getBlockMSBArray();
					System.arraycopy(nibblearray.getData(), 0, abyte, j, nibblearray.getData().length);
					j += nibblearray.getData().length;
				}
			}
		}

		if (includeinit) {
			byte[] abyte2 = chunk.getBiomeArray();
			System.arraycopy(abyte2, 0, abyte, j, abyte2.length);
			j += abyte2.length;
		}

		extracted.field_150282_a = new byte[j];
		System.arraycopy(abyte, 0, extracted.field_150282_a, 0, j);
		return extracted;
	}
}
