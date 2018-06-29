package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

/**
 * Based on code from Ken Butler/shadowking97
 */
public class PacketTileEntityNBT extends PacketHandlerBase {

	public static FMLProxyPacket createPacket(int xPos, int yPos, int zPos, NBTTagCompound nbt, int dim) {
		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		PacketBuffer data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

		data.writeInt(dim);
		data.writeInt(xPos);
		data.writeInt(yPos);
		data.writeInt(zPos);
		ByteBufUtils.writeTag(data, nbt);

		return buildPacket(data);
	}

	@Override
	public void handle(PacketBuffer data, EntityPlayer player) {
		int dimension = data.readInt();
		int xPos = data.readInt();
		int yPos = data.readInt();
		int zPos = data.readInt();
		NBTTagCompound nbt = ByteBufUtils.readTag(data);

		BlockPos coords = new BlockPos(xPos, yPos, zPos);
		WorldClient proxyworld = ProxyWorldManager.getProxyworld(dimension);
		if (proxyworld == null) return;
		if (proxyworld.provider.getDimension() != dimension) return;
		if (proxyworld.isBlockLoaded(coords)) {
			TileEntity tileentity = proxyworld.getTileEntity(coords);

			if (tileentity != null) {
				tileentity.readFromNBT(nbt);
			} else {
				//Create tile entity from data
				tileentity = TileEntity.create(proxyworld, nbt);
				if (tileentity != null) {
					proxyworld.addTileEntity(tileentity);
				}
			}
			proxyworld.markChunkDirty(coords, tileentity);
			proxyworld.setTileEntity(coords, tileentity);
			//TODO: Test me proxyworld.markBlockForUpdate(coords);
		}
	}
}
