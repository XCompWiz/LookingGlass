package com.xcompwiz.lookingglass.client.proxyworld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

// FIXME: AAHH! Fake world classes! EXTERMINATE!
public class ProxyWorld extends WorldClient {
	public ProxyWorld(int dimensionID) {
		super(Minecraft.getMinecraft().getConnection(), new WorldSettings(0L, GameType.SURVIVAL, true, false, WorldType.DEFAULT), dimensionID, Minecraft.getMinecraft().gameSettings.difficulty, Minecraft.getMinecraft().world.profiler);
	}

	// TODO: In order to eliminate this class we may need an event in this function to allow canceling/redirecting sounds (See import net.minecraftforge.client.event.sound.SoundEvent?)
	@Override
	public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {}

	// TODO: In order to eliminate this class we need to create a redirection wrapper class for the mc.effectRenderer which does this for all views.
	@Override
	public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, NBTTagCompound compound) {
		for (WorldView activeview : ProxyWorldManager.getWorldViews(this.provider.getDimension())) {
			activeview.getParticleManager().addEffect(new ParticleFirework.Starter(this, x, y, z, motionX, motionY, motionZ, activeview.getParticleManager(), compound));
		}
	}
}
