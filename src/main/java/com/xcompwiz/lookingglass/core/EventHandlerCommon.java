package com.xcompwiz.lookingglass.core;

import com.xcompwiz.lookingglass.proxyworld.ChunkFinderManager;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * This class is primarily used to listen for tick events.
 */
public class EventHandlerCommon {

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		// On server ticks we try to send clients who need chunk data some of that data they need
		ChunkFinderManager.instance.tick();
	}
}
