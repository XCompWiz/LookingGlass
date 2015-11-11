package com.xcompwiz.lookingglass.api.hook;

import net.minecraft.util.ChunkCoordinates;

import com.xcompwiz.lookingglass.api.view.IWorldView;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Available via "view-2" from the API provider
 * @author xcompwiz
 */
public interface WorldViewAPI2 {

	/**
	 * Creates a world viewer object which will handle the rendering and retrieval of the remote location. Can return null.
	 * @param dimid The target dimension
	 * @param coords The coordinates of the target location. If null, world spawn is used.
	 * @param width Texture resolution width
	 * @param height Texture resolution height
	 * @return A IWorldView object for your use or null if something goes wrong.
	 */
	@SideOnly(Side.CLIENT)
	IWorldView createWorldView(Integer dimid, ChunkCoordinates coords, int width, int height);

	/**
	 * This function is available should you wish to explicitly have the world view clean up its framebuffer. You should not use a view after calling this on
	 * the view.
	 * @param worldview The view to clean up (effectively "destroy")
	 */
	void cleanupWorldView(IWorldView worldview);
}
