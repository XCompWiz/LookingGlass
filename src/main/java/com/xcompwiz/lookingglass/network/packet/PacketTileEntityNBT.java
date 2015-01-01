package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

/**
 * Based on code from Ken Butler/shadowking97
 */
public class PacketTileEntityNBT extends PacketHandlerBase {

	public static FMLProxyPacket createPacket(int xPos, int yPos, int zPos, NBTTagCompound nbt, int dim) {
		// This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
		ByteBuf data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

		data.writeInt(dim);
		data.writeInt(xPos);
		data.writeInt(yPos);
		data.writeInt(zPos);
		ByteBufUtils.writeTag(data, nbt);

		return buildPacket(data);
	}

	@Override
	public void handle(ByteBuf data, EntityPlayer player) {
		int dimension = data.readInt();
		int xPos = data.readInt();
		int yPos = data.readInt();
		int zPos = data.readInt();
		NBTTagCompound nbt = ByteBufUtils.readTag(data);

		WorldClient proxyworld = ProxyWorldManager.getProxyworld(dimension);
		if (proxyworld == null) return;
		if (proxyworld.provider.dimensionId != dimension) return;
		if (proxyworld.blockExists(xPos, yPos, zPos)) {
			TileEntity tileentity = proxyworld.getTileEntity(xPos, yPos, zPos);

			if (tileentity != null) {
				tileentity.readFromNBT(nbt);
			} else {
				//Create tile entity from data
				tileentity = TileEntity.createAndLoadEntity(nbt);
				if (tileentity != null) {
					proxyworld.addTileEntity(tileentity);
				}
			}
			proxyworld.markTileEntityChunkModified(xPos, yPos, zPos, tileentity);
			proxyworld.setTileEntity(xPos, yPos, zPos, tileentity);
			proxyworld.markBlockForUpdate(xPos, yPos, zPos);
		}
	}
}
