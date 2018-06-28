package com.xcompwiz.lookingglass.api.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called on the server side to allow mods to send dimension information to clients
 * @author xcompwiz
 */
public class ClientWorldInfoEvent extends Event {

	public final int			dim;
	public final EntityPlayerMP	player;

	public ClientWorldInfoEvent(int dim, EntityPlayerMP player) {
		this.dim = dim;
		this.player = player;
	}

}
