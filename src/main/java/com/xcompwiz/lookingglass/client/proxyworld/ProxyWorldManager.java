package com.xcompwiz.lookingglass.client.proxyworld;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.DimensionManager;

import com.xcompwiz.lookingglass.client.render.FrameBufferContainer;
import com.xcompwiz.lookingglass.entity.EntityCamera;
import com.xcompwiz.lookingglass.log.LoggerUtils;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.network.packet.PacketCreateView;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProxyWorldManager {
	private static Map<Integer, WorldClient>			proxyworlds		= new HashMap<Integer, WorldClient>();
	private static Collection<WorldClient>				proxyworldset	= Collections.unmodifiableCollection(proxyworlds.values());
	/** We actually populate this with weak sets. This allows for the world views to be freed without us needing to do anything. */
	private static Map<Integer, Collection<WorldView>>	worldviewsets	= new HashMap<Integer, Collection<WorldView>>();

	/**
	 * This is a complex bit. As we want to reuse the current client world when rendering, if possible, we need to handle when that world changes. We could
	 * simply destroy all the views pointing to the existing proxy world, but that would be annoying to mods using the API. Instead, we replace our proxy world
	 * with the new client world. This should only be called by LookingGlass, and only from the handling of the client world change detection.
	 * @param world The new client world
	 */
	public static void handleWorldChange(WorldClient world) {
		if (ModConfigs.disabled) return;
		if (world == null) return;
		int dimid = world.provider.dimensionId;
		if (!proxyworlds.containsKey(dimid)) return; //BEST CASE! We don't have to do anything!
		proxyworlds.put(dimid, world);
		Collection<WorldView> worldviews = worldviewsets.get(dimid);
		for (WorldView view : worldviews) {
			// Handle the change on the view object
			view.replaceWorldObject(world);
		}
	}

	public static synchronized void detectFreedWorldViews() {
		FrameBufferContainer.detectFreedWorldViews();
		//TODO: closeViewConnection(worldviewID);
		HashSet<Integer> emptyLists = new HashSet<Integer>();
		for (Map.Entry<Integer, Collection<WorldView>> entry : worldviewsets.entrySet()) {
			if (entry.getValue().isEmpty()) emptyLists.add(entry.getKey());
		}
		for (Integer dimId : emptyLists) {
			unloadProxyWorld(dimId);
		}
	}

	public static synchronized WorldClient getProxyworld(int dimid) {
		if (ModConfigs.disabled) return null;
		WorldClient proxyworld = proxyworlds.get(dimid);
		if (proxyworld == null) {
			if (!DimensionManager.isDimensionRegistered(dimid)) return null;
			// We really don't want to be doing this during a render cycle
			if (Minecraft.getMinecraft().thePlayer instanceof EntityCamera) return null; //TODO: This check probably needs to be altered
			WorldClient theWorld = Minecraft.getMinecraft().theWorld;
			if (theWorld != null && theWorld.provider.dimensionId == dimid) proxyworld = theWorld;
			if (proxyworld == null) proxyworld = new ProxyWorld(dimid);
			proxyworlds.put(dimid, proxyworld);
			worldviewsets.put(dimid, Collections.newSetFromMap(new WeakHashMap<WorldView, Boolean>()));
		}
		return proxyworld;
	}

	private static void unloadProxyWorld(int dimId) {
		Collection<WorldView> set = worldviewsets.remove(dimId);
		if (set != null && set.size() > 0) LoggerUtils.warn("Unloading ProxyWorld with live views");
		WorldClient proxyworld = proxyworlds.remove(dimId);
		WorldClient theWorld = Minecraft.getMinecraft().theWorld;
		if (theWorld != null && theWorld == proxyworld) return;
		if (proxyworld != null) net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Unload(proxyworld));
	}

	public static void clearProxyworlds() {
		while (!proxyworlds.isEmpty()) {
			unloadProxyWorld(proxyworlds.keySet().iterator().next());
		}
	}

	public static Collection<WorldClient> getProxyworlds() {
		return proxyworldset;
	}

	public static Collection<WorldView> getWorldViews(int dimid) {
		Collection<WorldView> set = worldviewsets.get(dimid);
		if (set == null) return Collections.EMPTY_SET;
		return Collections.unmodifiableCollection(set);
	}

	public static WorldView createWorldView(int dimid, ChunkCoordinates spawn, int width, int height) {
		if (ModConfigs.disabled) return null;
		if (!DimensionManager.isDimensionRegistered(dimid)) return null;

		WorldClient proxyworld = ProxyWorldManager.getProxyworld(dimid);
		if (proxyworld == null) return null;

		Collection<WorldView> worldviews = worldviewsets.get(dimid);
		if (worldviews == null) return null;

		WorldView view = new WorldView(proxyworld, spawn, width, height);

		// Initialize the view rendering system
		Minecraft mc = Minecraft.getMinecraft();
		EntityLivingBase backup = mc.renderViewEntity;
		mc.renderViewEntity = view.camera;
		view.getRenderGlobal().setWorldAndLoadRenderers(proxyworld);
		mc.renderViewEntity = backup;

		// Inform the server of the new view
		LookingGlassPacketManager.bus.sendToServer(PacketCreateView.createPacket(view));
		worldviews.add(view);
		return view;
	}

	//TODO: private static void closeViewConnection(long worldviewID) {
		//LookingGlassPacketManager.bus.sendToServer(PacketCloseView.createPacket(worldviewID));
	//}

	/**
	 * Handles explicit shutdown of a world view. Tells the view to clean itself up and removes it from the tracked world views here (encouraging the world to unload).
	 * @param view The view to kill
	 */
	public static void destroyWorldView(WorldView view) {
		Collection<WorldView> set = worldviewsets.get(view.getWorldObj().provider.dimensionId);
		if (set != null) set.remove(view);
		//TODO: closeViewConnection(worldviewID);
		view.cleanup();
	}
}
