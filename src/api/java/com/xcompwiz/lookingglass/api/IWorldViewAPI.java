package com.xcompwiz.lookingglass.api;

import com.xcompwiz.lookingglass.api.view.IWorldView;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @deprecated This interface will be removed in a future version. You should switch to the IWorldViewAPI2.
 * @author xcompwiz
 */
@Deprecated
public interface IWorldViewAPI {

	/**
	 * Creates a world viewer object which will handle the rendering and retrieval of the remote location. Can return null.
	 * @param dimid The target dimension
	 * @param coords The coordinates of the target location. If null, world spawn is used.
	 * @param width Texture resolution width
	 * @param height Texture resolution height
	 * @return A IWorldView object for your use or null if something goes wrong.
	 */
	@SideOnly(Side.CLIENT)
	IWorldView createWorldView(Integer dimid, BlockPos coords, int width, int height);
}
