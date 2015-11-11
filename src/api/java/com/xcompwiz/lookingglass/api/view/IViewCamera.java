package com.xcompwiz.lookingglass.api.view;

import net.minecraft.world.IBlockAccess;

public interface IViewCamera {

	/**
	 * Adds par1*0.15 to the entity's yaw, and *subtracts* par2*0.15 from the pitch. Clamps pitch from -90 to 90. Both arguments in degrees.
	 * @param yaw The yaw to be added
	 * @param pitch The pitch to be subtracted
	 */
	public void addRotations(float yaw, int pitch);

	/**
	 * Sets the yaw rotation of the camera
	 * @param f The camera's new yaw in degrees
	 */
	public void setYaw(float f);

	/**
	 * @return The camera's yaw in degrees
	 */
	public float getYaw();

	/**
	 * Sets the pitch rotation of the camera
	 * @param f The camera's new pitch in degrees
	 */
	public void setPitch(float f);

	/**
	 * @return The camera's pitch in degrees
	 */
	public float getPitch();

	/**
	 * Sets the position of the camera
	 * @param x X Coordinate (Block space)
	 * @param y Y Coordinate (Block space)
	 * @param z Z Coordinate (Block space)
	 */
	public void setLocation(double x, double y, double z);

	/**
	 * @return The camera's X coordinate (Block space)
	 */
	public double getX();

	/**
	 * @return The camera's Y coordinate (Block space)
	 */
	public double getY();

	/**
	 * @return The camera's Z coordinate (Block space)
	 */
	public double getZ();

	/**
	 * This is provided to allow for checking for air blocks and similar. Technically, it is a WorldClient object, but it is provided as a IBlockAccess to
	 * discourage modifying the world. Modifying the world object would break things for everyone, so please don't do that. Should it become an issue, this will
	 * be replaced with something that isn't a WorldClient, so be wary of casting it.
	 * @return A read-only reference to the world which the camera inhabits
	 */
	public IBlockAccess getBlockData();

	/**
	 * An easy check for if a chunk exists in the local data copy
	 * @param x The X coordinate in block space
	 * @param z The Z coordinate in block space
	 * @return True if the chunk at those coordinates is available locally
	 */
	public boolean chunkExists(int x, int z);

	/**
	 * Since LookingGlass utilizes partial chunk loading to minimize data traffic and storage, it's useful to be able to check if certain levels of a chunk are
	 * loaded locally.
	 * @param x The X coordinate in block space
	 * @param z The Z coordinate in block space
	 * @param yl1 The lower (closer to 0) y coordinate of the levels to check for in block space
	 * @param yl2 The larger (farther from 0) y coordinate of the levels to check for in block space
	 * @return True if the levels are loaded locally
	 */
	public boolean chunkLevelsExist(int x, int z, int yl1, int yl2);
}
