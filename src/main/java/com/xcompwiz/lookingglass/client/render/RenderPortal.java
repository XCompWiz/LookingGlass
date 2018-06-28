package com.xcompwiz.lookingglass.client.render;

import org.lwjgl.opengl.GL11;

import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.entity.EntityPortal;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderPortal extends Render<EntityPortal> {

	public RenderPortal(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityPortal entity) {
		return null;
	}

	@Override
	public void doRender(EntityPortal entity, double x, double y, double z, float entityYaw, float partial) {
		if (!(entity instanceof EntityPortal)) return;
		EntityPortal portal = (EntityPortal) entity;
		IWorldView activeview = portal.getActiveView();
		if (activeview == null) return;

		int texture = activeview.getTexture();
		if (texture == 0) return;

		int width = 2;
		int height = 3;

		activeview.markDirty();

		GlStateManager.disableAlpha();
		GlStateManager.disableLighting();

		GL11.glPushMatrix();

		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(-entityYaw, 0.0F, 1.0F, 0.0F);

		Tessellator tes = Tessellator.getInstance();
		BufferBuilder vb = tes.getBuffer();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vb.pos(- width/2, 0, 0.01f).tex(0.0D, 1.0D).endVertex();
		vb.pos(width/2, 0, 0.01f).tex(1.0D, 1.0D).endVertex();
		vb.pos(width/2, height, 0.01f).tex(1.0D, 0.0D).endVertex();
		vb.pos(- width/2,  height, 0.01f).tex(0.0D, 0.0D).endVertex();
		tes.draw();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		//XXX: Make the back of the portals a little nicer
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vb.pos(- width/2,  height, -0.01f).tex(0.0D, 1.0D).endVertex();
		vb.pos(width/2, height, -0.01f).tex(1.0D, 1.0D).endVertex();
		vb.pos(width/2, 0, -0.01f).tex(1.0D, 0.0D).endVertex();
		vb.pos(- width/2, 0, -0.01f).tex(0.0D, 0.0D).endVertex();
		tes.draw();
		GL11.glPopMatrix();

		GlStateManager.enableLighting();
		GlStateManager.enableAlpha();
	}


	public static class Factory implements IRenderFactory<EntityPortal> {
		@Override
		public Render<EntityPortal> createRenderFor(RenderManager manager) {
			return new RenderPortal(manager);
		}
	}
}
