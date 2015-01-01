package com.xcompwiz.lookingglass.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.entity.EntityPortal;

public class RenderPortal extends Render {

	@Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
		if (!(entity instanceof EntityPortal)) return;
		EntityPortal portal = (EntityPortal) entity;
		IWorldView activeview = portal.getActiveView();
		if (activeview == null) return;

		int texture = activeview.getTexture();
		if (texture == 0) return;

		int width = 2;
		int height = 3;
		double left = -width / 2.;
		double top = 0;

		activeview.markDirty();
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);

		GL11.glPushMatrix();
		GL11.glTranslatef((float) d, (float) d1, (float) d2);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		Tessellator tessellator = Tessellator.instance;
		tessellator.setColorRGBA_F(1, 1, 1, 1);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(left, top, 0.0D, 0.0D, 0.0D); //inc=bl out; inc=bl down
		tessellator.addVertexWithUV(width + left, top, 0.0D, 1.0D, 0.0D); //dc=br out; inc=br down
		tessellator.addVertexWithUV(width + left, height + top, 0.0D, 1.0D, 1.0D); //dec=tr out; dec=tr up
		tessellator.addVertexWithUV(left, height + top, 0.0D, 0.0D, 1.0D); //inc=lt out; dec=tl up
		tessellator.draw();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		//XXX: Make the back of the portals a little nicer
		tessellator.setColorRGBA_F(0, 0, 1, 1);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(left, height + top, 0.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV(width + left, height + top, 0.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV(width + left, top, 0.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV(left, top, 0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GL11.glPopMatrix();

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	@Override
	protected void bindEntityTexture(Entity entity) {}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

}
