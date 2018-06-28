package com.xcompwiz.lookingglass.client;

import com.xcompwiz.lookingglass.client.render.RenderPortal;
import com.xcompwiz.lookingglass.core.CommonProxy;
import com.xcompwiz.lookingglass.entity.EntityPortal;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Our faithful proxy class.  Allows for running code differently dependent on whether we are client- or server-side.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	/**
	 * Run during mod init.
	 */
	@Override
	public void init() {
		// We register the portal renderer here
		Render render;
		render = new RenderPortal();
		render.setRenderManager(RenderManager.instance);
		RenderingRegistry.registerEntityRenderingHandler(EntityPortal.class, render);
	}
}
