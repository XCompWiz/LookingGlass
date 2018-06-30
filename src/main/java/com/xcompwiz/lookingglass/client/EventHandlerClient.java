package com.xcompwiz.lookingglass.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.entity.EntityPortal;
import com.xcompwiz.lookingglass.log.LoggerUtils;
import com.xcompwiz.lookingglass.render.WorldViewRenderManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

/**
 * This class handles the FML events. Primarily it is used to listen for tick events.
 */
public class EventHandlerClient {

	/** An output stream we can use for proxy world logging */
	private final PrintStream printstream;

	/** The client world as we last saw it. We use this to check if the client has changed worlds */
	private WorldClient previousWorld;

	/** A simple accumulator to handle triggering freeing world views and such */
	private int tickcounter;

	public EventHandlerClient(File logfile) {
		PrintStream stream = null;
		try {
			stream = new PrintStream(logfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			printstream = stream;
		}
		if (printstream == null) throw new RuntimeException("Could not set up debug exception logger file for Proxy World system");
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		// If no client world we're not playing. Abort.
		if (mc.world == null) return;
		// We don't want to tick twice per tick loop. Just once.  We choose to tick at the start of the tick loop.
		if (event.phase != TickEvent.Phase.START) return;

		// Every now and then we want to check to see if there are frame buffers we could free 
		if (++this.tickcounter % 200 == 0) ProxyWorldManager.detectFreedWorldViews();

		// Handle whenever the client world has changed since we last looked
		if (mc.world != previousWorld) {
			// We need to handle the removal of the old world.  Particularly, the player will still be visible in it.
			// We may consider replacing the old client world with a new proxy world.
			if (previousWorld != null) previousWorld.removeAllEntities(); //TODO: This is hardly an ideal solution (It also doesn't seem to work well)
			previousWorld = mc.world; // At this point we can safely assert that the client world has changed

			// We let our local world manager know that the client world changed.
			ProxyWorldManager.handleWorldChange(mc.world);
		}

		// Tick loop for our own worlds.
		WorldClient worldBackup = mc.world;
		for (WorldClient proxyworld : ProxyWorldManager.getProxyworlds()) {
			if (proxyworld.getLastLightningBolt() > 0) proxyworld.setLastLightningBolt(proxyworld.getLastLightningBolt() - 1);
			if (worldBackup == proxyworld) continue; // This prevents us from double ticking the client world.
			try {
				mc.world = proxyworld;
				//TODO: relays for views (renderGlobal and effectRenderer) (See ProxyWorld.makeFireworks ln23) 
				proxyworld.tick();
			} catch (Exception e) {
				LoggerUtils.error("Client Proxy Dim had error while ticking: %s", e.getLocalizedMessage());
				e.printStackTrace(printstream);
			}
		}
		mc.world = worldBackup;
	}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		// If no client world we're not playing. Abort.
		if (Minecraft.getMinecraft().world == null) return;
		if (event.phase == TickEvent.Phase.END) {
			// We render the world views at the end of the render tick.  
			WorldViewRenderManager.onRenderTick(printstream);
			return;
		}
	}

	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		if (!event.getWorld().isRemote) return;
		Chunk chunk = event.getChunk();
		// When we unload a chunk client side, we want to make sure that any view entities clean up. Not strictly necessary, but a good practice.
		// I don't trust vanilla to unload entity references quickly/correctly/completely.
		for (int i = 0; i < chunk.getEntityLists().length; ++i) {
			ClassInheritanceMultiMap<Entity> list = chunk.getEntityLists()[i];
			for (Entity entity : list) {
				if (entity instanceof EntityPortal) ((EntityPortal) entity).releaseActiveView();
			}
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if (!event.getWorld().isRemote) return;
		// When we unload a world client side, we want to make sure that any view entities clean up. Not strictly necessary, but a good practice.
		// I don't trust vanilla to unload entity references quickly/correctly/completely.
		for (Object entity : event.getWorld().loadedEntityList) {
			if (entity instanceof EntityPortal) ((EntityPortal) entity).releaseActiveView();
		}
	}

	@SubscribeEvent
	public void onClientDisconnect(ClientDisconnectionFromServerEvent event) {
		// Abandon ship!
		ProxyWorldManager.clearProxyworlds();
	}
}
