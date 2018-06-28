package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.network.ServerPacketDispatcher;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class PacketRequestTE extends PacketHandlerBase {
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
		WorldServer world = MinecraftServer.getServer().worldServerForDimension(dim);
		if (world == null) return;
		TileEntity tile = world.getTileEntity(new BlockPos(xPos, yPos, zPos));
		if (tile != null) {
			//FIXME: This is currently a very "forceful" method of doing this, and not technically guaranteed to produce correct results
			// This would be much better handled by using the getDescriptionPacket method and wrapping that packet in a LookingGlass
			// packet to control delivery timing, allowing for processing the packet while the correct target world is the active world
			// This idea requires that that system be in place, though, so until then this hack will hopefully hold.
			NBTTagCompound tag = new NBTTagCompound();
			tile.writeToNBT(tag);
			ServerPacketDispatcher.getInstance().addPacket(player, PacketTileEntityNBT.createPacket(xPos, yPos, zPos, tag, dim));
		}
	}
}
