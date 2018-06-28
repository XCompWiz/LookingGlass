package com.xcompwiz.lookingglass.core;

import java.util.List;

import com.xcompwiz.lookingglass.entity.EntityPortal;

import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LookingGlassForgeEventHandler {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		if (!event.world.isRemote) return;
		Chunk chunk = event.getChunk();
		// When we unload a chunk client side, we want to make sure that any view entities clean up. Not strictly necessary, but a good practice.
		// I don't trust vanilla to unload entity references quickly/correctly/completely.
		for (int i = 0; i < chunk.entityLists.length; ++i) {
			List<Entity> list = chunk.entityLists[i];
			for (Entity entity : list) {
				if (entity instanceof EntityPortal) ((EntityPortal) entity).releaseActiveView();
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if (!event.world.isRemote) return;
		// When we unload a world client side, we want to make sure that any view entities clean up. Not strictly necessary, but a good practice.
		// I don't trust vanilla to unload entity references quickly/correctly/completely.
		for (Object entity : event.world.loadedEntityList) {
			if (entity instanceof EntityPortal) ((EntityPortal) entity).releaseActiveView();
		}
	}
}
