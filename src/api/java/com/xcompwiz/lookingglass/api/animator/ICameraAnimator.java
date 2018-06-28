package com.xcompwiz.lookingglass.api.animator;

import net.minecraft.util.math.BlockPos;

public interface ICameraAnimator {

	/**
	 * Sets the look-at target (in block coordinates)
	 * @param target The block target
	 */
	void setTarget(BlockPos target);

	/**
	 * Allows the animator to refresh/reboot its settings
	 */
	void refresh();

	/**
	 * Tick!
	 * @param dt Delta time in milliseconds since last render tick
	 */
	void update(long dt);

}
