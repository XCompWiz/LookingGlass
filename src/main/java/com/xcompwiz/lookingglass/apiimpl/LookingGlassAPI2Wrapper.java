package com.xcompwiz.lookingglass.apiimpl;

import net.minecraft.util.ChunkCoordinates;

import com.xcompwiz.lookingglass.api.hook.WorldViewAPI2;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This is the API wrapper (instance) class for the WorldView API at version 2.
 * @author xcompwiz
 */
public class LookingGlassAPI2Wrapper extends APIWrapper implements WorldViewAPI2 {

	public LookingGlassAPI2Wrapper(String modname) {
		super(modname);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IWorldView createWorldView(Integer dimid, ChunkCoordinates spawn, int width, int height) {
		return ProxyWorldManager.createWorldView(dimid, (spawn != null ? new ChunkCoordinates(spawn) : null), width, height);
	}

	@Override
	public void cleanupWorldView(IWorldView worldview) {
		if (worldview == null) return;
		if (!(worldview instanceof WorldView)) throw new RuntimeException("[%s] is misusing the LookingGlass API. Cannot cleanup custom IWorldView objects.");
		ProxyWorldManager.destroyWorldView((WorldView) worldview);
	}
}
