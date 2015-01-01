package com.xcompwiz.lookingglass.client.proxyworld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFireworkStarterFX;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.WorldType;

// FIXME: AAHH! Fake world classes! EXTERMINATE!
public class ProxyWorld extends WorldClient {
	public ProxyWorld(int dimensionID) {
		super(Minecraft.getMinecraft().getNetHandler(), new WorldSettings(0L, GameType.SURVIVAL, true, false, WorldType.DEFAULT), dimensionID, Minecraft.getMinecraft().gameSettings.difficulty, Minecraft.getMinecraft().theWorld.theProfiler);
	}

	// TODO: In order to eliminate this class we may need an event in this function to allow canceling/redirecting sounds
	@Override
	public void playSound(double par1, double par3, double par5, String par7Str, float par8, float par9, boolean par10) {}

	// TODO: In order to eliminate this class we need to create a redirection wrapper class for the mc.effectRenderer which does this for all views.
	@Override
	public void makeFireworks(double par1, double par3, double par5, double par7, double par9, double par11, NBTTagCompound par13NBTTagCompound) {
		for (WorldView activeview : ProxyWorldManager.getWorldViews(this.provider.dimensionId)) {
			activeview.getEffectRenderer().addEffect(new EntityFireworkStarterFX(this, par1, par3, par5, par7, par9, par11, activeview.getEffectRenderer(), par13NBTTagCompound));
		}
	}
}
