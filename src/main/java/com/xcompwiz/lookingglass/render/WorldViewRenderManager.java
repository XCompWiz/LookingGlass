package com.xcompwiz.lookingglass.render;

import java.io.PrintStream;
import java.util.Collection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.client.render.RenderUtils;
import com.xcompwiz.lookingglass.log.LoggerUtils;

public class WorldViewRenderManager {
	public static void onRenderTick(PrintStream printstream) {
		Minecraft mc = Minecraft.getMinecraft();
		Collection<WorldClient> worlds = ProxyWorldManager.getProxyworlds();
		if (worlds == null || worlds.isEmpty()) return;

		long renderT = Minecraft.getSystemTime();
		//TODO: This and the renderWorldToTexture need to be remixed
		WorldClient worldBackup = mc.theWorld;
		RenderGlobal renderBackup = mc.renderGlobal;
		EffectRenderer effectBackup = mc.effectRenderer;
		EntityClientPlayerMP playerBackup = mc.thePlayer;
		EntityLivingBase viewportBackup = mc.renderViewEntity;

		//TODO: This is a hack to work around some of the vanilla rendering hacks... Yay hacks.
		float fovmult = playerBackup.getFOVMultiplier();
		ItemStack currentClientItem = playerBackup.inventory.getCurrentItem();

		for (WorldClient proxyworld : worlds) {
			if (proxyworld == null) continue;
			mc.theWorld = proxyworld;
			RenderManager.instance.set(mc.theWorld);
			for (WorldView activeview : ProxyWorldManager.getWorldViews(proxyworld.provider.dimensionId)) {
				if (activeview.hasChunks() && activeview.markClean()) {
					activeview.startRender(renderT);

					mc.renderGlobal = activeview.getRenderGlobal();
					mc.effectRenderer = activeview.getEffectRenderer();
					mc.renderViewEntity = activeview.camera;
					mc.thePlayer = activeview.camera;
					//Other half of hack
					activeview.camera.setFOVMult(fovmult); //Prevents the FOV from flickering
					activeview.camera.inventory.currentItem = playerBackup.inventory.currentItem;
					activeview.camera.inventory.mainInventory[playerBackup.inventory.currentItem] = currentClientItem; //Prevents the hand from flickering

					try {
						mc.renderGlobal.updateClouds();
						mc.theWorld.doVoidFogParticles(MathHelper.floor_double(activeview.camera.posX), MathHelper.floor_double(activeview.camera.posY), MathHelper.floor_double(activeview.camera.posZ));
						mc.effectRenderer.updateEffects();
					} catch (Exception e) {
						LoggerUtils.error("Client Proxy Dim had error while updating render elements: %s", e.getLocalizedMessage());
						e.printStackTrace(printstream);
					}

					try {
						RenderUtils.renderWorldToTexture(0.1f, activeview.getFramebuffer(), activeview.width, activeview.height);
					} catch (Exception e) {
						LoggerUtils.error("Client Proxy Dim had error while rendering: %s", e.getLocalizedMessage());
						e.printStackTrace(printstream);
					}
				}
			}
		}
		mc.renderViewEntity = viewportBackup;
		mc.thePlayer = playerBackup;
		mc.effectRenderer = effectBackup;
		mc.renderGlobal = renderBackup;
		mc.theWorld = worldBackup;
		RenderManager.instance.set(mc.theWorld);
	}

}
