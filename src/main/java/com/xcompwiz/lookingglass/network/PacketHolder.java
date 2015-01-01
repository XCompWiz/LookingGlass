package com.xcompwiz.lookingglass.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

/**
 * @author Ken Butler/shadowking97
 */
// TODO: This class doesn't need to exist, it's just a (Player, Packet) tuple
public class PacketHolder {
	EntityPlayer	player;
	FMLProxyPacket	packet;

	public PacketHolder(EntityPlayer player, FMLProxyPacket packet) {
		this.player = player;
		this.packet = packet;
	}

	public boolean belongsToPlayer(EntityPlayer p) {
		return player.equals(p);
	}

	public int sendPacket() {
		if (packet != null) {
			LookingGlassPacketManager.bus.sendTo(packet, (EntityPlayerMP) player);
			return packet.payload().writerIndex();
		}
		return 0;
	}
}
