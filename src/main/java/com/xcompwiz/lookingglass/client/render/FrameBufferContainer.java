package com.xcompwiz.lookingglass.client.render;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentMap;

import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import com.google.common.collect.MapMaker;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.log.LoggerUtils;

import net.minecraftforge.client.MinecraftForgeClient;

public class FrameBufferContainer {
	/**
	 * Using this map we can detect which FBOs should be freed. The map will delete any entry where the world view object is garbage collected. It unfortunately
	 * can't detect that the world view is otherwise leaked, though, just when it's gone.
	 */
	private static ConcurrentMap<WorldView, FrameBufferContainer>	weakfbomap		= new MapMaker().weakKeys().<WorldView, FrameBufferContainer> makeMap();
	private static Collection<FrameBufferContainer>					framebuffers	= new HashSet<FrameBufferContainer>();

	public static FrameBufferContainer createNewFramebuffer(WorldView view, int width, int height) {
		FrameBufferContainer fbo = new FrameBufferContainer(width, height);
		weakfbomap.put(view, fbo);
		framebuffers.add(fbo);
		return fbo;
	}

	public static void removeWorldView(WorldView view) {
		weakfbomap.remove(view);
	}

	public static void clearAll() {
		for (FrameBufferContainer fbo : framebuffers) {
			fbo.release();
		}
		framebuffers.clear();
	}

	public static synchronized void detectFreedWorldViews() {
		Collection<FrameBufferContainer> unpairedFBOs = new HashSet<FrameBufferContainer>(framebuffers);
		unpairedFBOs.removeAll(weakfbomap.values());
		if (unpairedFBOs.isEmpty()) return;
		LoggerUtils.info("Freeing %d loose framebuffers from expired world views", unpairedFBOs.size());
		for (FrameBufferContainer fbo : unpairedFBOs) {
			fbo.release();
		}
		framebuffers.removeAll(unpairedFBOs);
	}

	public final int	width;
	public final int	height;

	private int			framebuffer;
	private int			depthBuffer;
	private int			texture;

	private FrameBufferContainer(int width, int height) {
		this.width = width;
		this.height = height;
		allocateFrameBuffer();
	}

	private void release() {
		freeFrameBuffer();
	}

	public int getFramebuffer() {
		return framebuffer;
	}

	public int getTexture() {
		return texture;
	}

	// Always clean up your allocations
	private synchronized void freeFrameBuffer() {
		try {
			if (this.texture != 0) GL11.glDeleteTextures(this.texture);
			this.texture = 0;
			if (depthBuffer != 0) EXTFramebufferObject.glDeleteRenderbuffersEXT(depthBuffer);
			depthBuffer = 0;
			if (this.framebuffer != 0) EXTFramebufferObject.glDeleteFramebuffersEXT(this.framebuffer);
			this.framebuffer = 0;
		} catch (Exception e) {
			// Just in case, we make sure we don't crash. Because crashing is bad.
			LoggerUtils.error("Error while cleaning up a world view frame buffer.");
		}
	}

	// This method builds the frame buffer and texture references
	private void allocateFrameBuffer() {
		if (this.framebuffer != 0) return;

		this.framebuffer = EXTFramebufferObject.glGenFramebuffersEXT(); //Release via: EXTFramebufferObject.glDeleteFramebuffersEXT(framebuffer);
		this.depthBuffer = EXTFramebufferObject.glGenRenderbuffersEXT(); //Release via: EXTFramebufferObject.glDeleteRenderbuffersEXT(depthBuffer);

		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, this.framebuffer);

		EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthBuffer);
		if (MinecraftForgeClient.getStencilBits() == 0) EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, width, height);
		else EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, org.lwjgl.opengl.EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT, width, height);

		EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthBuffer);
		if (MinecraftForgeClient.getStencilBits() != 0) EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthBuffer);

		this.texture = GL11.glGenTextures(); //Release via: GL11.glDeleteTextures(colorTexture);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_INT, (java.nio.ByteBuffer) null);
		EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, this.texture, 0);

		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
	}
}
