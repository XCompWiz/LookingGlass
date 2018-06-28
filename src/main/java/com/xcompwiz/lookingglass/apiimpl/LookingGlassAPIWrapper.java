package com.xcompwiz.lookingglass.apiimpl;

import com.xcompwiz.lookingglass.api.IWorldViewAPI;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	public IWorldView createWorldView(Integer dimid, BlockPos spawn, int width, int height) {
		return ProxyWorldManager.createWorldView(dimid, (spawn != null ? new BlockPos(spawn) : null), width, height);
	}
}
