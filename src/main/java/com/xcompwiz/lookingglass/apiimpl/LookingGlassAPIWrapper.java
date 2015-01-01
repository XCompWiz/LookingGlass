package com.xcompwiz.lookingglass.apiimpl;

import net.minecraft.util.ChunkCoordinates;

import com.xcompwiz.lookingglass.api.IWorldViewAPI;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This is the API wrapper (instance) class for the WorldView API at version 1.
 * @author xcompwiz
 */
@SuppressWarnings("deprecation")
public class LookingGlassAPIWrapper extends APIWrapper implements IWorldViewAPI {

	public LookingGlassAPIWrapper(String modname) {
		super(modname);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IWorldView createWorldView(Integer dimid, ChunkCoordinates spawn, int width, int height) {
		return ProxyWorldManager.createWorldView(dimid, (spawn != null ? new ChunkCoordinates(spawn) : null), width, height);
	}
}
