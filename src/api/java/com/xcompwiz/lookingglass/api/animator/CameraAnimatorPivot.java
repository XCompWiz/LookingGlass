package com.xcompwiz.lookingglass.api.animator;

import com.xcompwiz.lookingglass.api.view.IViewCamera;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * This is a standard sample implementation of a camera animator. It simply uses the target location as a LookAt target and does a fly pivot around it. It can
 * be extended for more control, if desired.
 * @author xcompwiz
 */
public class CameraAnimatorPivot implements ICameraAnimator {
	/** This is a list of recommended, preset offsets that the animator will choose from. It's use can be overridden via function. */
	private static final int[][]	presets		= { { 2, 5 }, { 5, 9 }, { 9, 15 }, { 1, 3 }, { 2, 1 }, { 0, 2 } };
	/** This is a pair used when the animator cannot find a good path from the presets. It's use can be overridden via function. */
	private static final int[]		defaults	= { 1, 3 };

	private final IViewCamera		camera;
	private BlockPos		target;

	private boolean					positionSet	= false;

	private int						xCenter;
	private int						yCenter;
	private int						zCenter;

	private int						yUp			= 0;
	private int						radius		= 0;
	private float					pitch		= 0;

	public CameraAnimatorPivot(IViewCamera camera) {
		this.camera = camera;
	}

	@Override
	public void setTarget(BlockPos target) {
		this.target = target;
		positionSet = false;
	}

	@Override
	public void update(long dt) {
		if (camera == null) return;
		camera.addRotations(dt*0.1F, 0);
		camera.setPitch(-pitch);

		double x = Math.cos(Math.toRadians(camera.getYaw() + 90)) * radius;
		double z = Math.sin(Math.toRadians(camera.getYaw() + 90)) * radius;
		camera.setLocation(xCenter + 0.5 - x, yCenter - 0.5 + yUp, zCenter + 0.5 - z);
	}

	@Override
	public void refresh() {
		if (camera == null) return;
		if (target == null) return;
		if (!positionSet) this.checkCameraY();

		int chunkX = xCenter >> 4;
		int chunkY = yCenter >> 4;
		int chunkZ = zCenter >> 4;

		int[][] presets = this.getPresets();

		for (int i = 0; i < presets.length; ++i) {
			if (checkPath(presets[i][0], presets[i][1], chunkX, chunkY, chunkZ)) {
				yUp = presets[i][0];
				radius = presets[i][1];
				pitch = (float) Math.toDegrees(Math.atan(((double) -yUp) / radius));
				return;
			}
		}
		int[] defaults = this.getDefaults();
		yUp = defaults[0];
		radius = defaults[1];
		pitch = (float) Math.toDegrees(Math.atan(((double) -yUp) / radius));
	}

	/**
	 * Should the selection from the offsets fail, the pair of values in this array will be used instead.
	 * @return A pair of numbers up and distance in an array of length 2
	 */
	public int[] getDefaults() {
		return defaults;
	}

	/**
	 * Overriding his function allows you specify your own offset presets.
	 * @return An array of integer pairs up and distance from which to select the first functional pair
	 */
	public int[][] getPresets() {
		return presets;
	}

	/**
	 * @author Ken Butler/shadowking97
	 */
	private boolean checkPath(int up, int distance, int chunkX, int chunkY, int chunkZ) {
		if ((yCenter & 15) > 15 - up) {
			if (isAboveNullLayer(chunkX, chunkY, chunkZ)) return false;
			if ((xCenter & 15) < distance) {
				if (isAboveNullLayer(chunkX - 1, chunkY, chunkZ)) return false;
				if ((zCenter & 15) < distance) {
					if (isAboveNullLayer(chunkX - 1, chunkY, chunkZ - 1)) return false;
					if (isAboveNullLayer(chunkX, chunkY, chunkZ - 1)) return false;
				} else if ((zCenter & 15) > 15 - distance) {
					if (isAboveNullLayer(chunkX - 1, chunkY, chunkZ + 1)) return false;
					if (isAboveNullLayer(chunkX, chunkY, chunkZ + 1)) return false;
				}
			} else if ((xCenter & 15) > 15 - distance) {
				if (isAboveNullLayer(chunkX + 1, chunkY, chunkZ)) return false;
				if ((zCenter & 15) < distance) {
					if (isAboveNullLayer(chunkX + 1, chunkY, chunkZ - 1)) return false;
					if (isAboveNullLayer(chunkX, chunkY, chunkZ - 1)) return false;
				} else if ((zCenter & 15) > 15 - distance) {
					if (isAboveNullLayer(chunkX + 1, chunkY, chunkZ + 1)) return false;
					if (isAboveNullLayer(chunkX, chunkY, chunkZ + 1)) return false;
				}
			} else {
				if ((zCenter & 15) < distance) {
					if (isAboveNullLayer(chunkX, chunkY, chunkZ - 1)) return false;
				} else if ((zCenter & 15) > 15 - distance) {
					if (isAboveNullLayer(chunkX, chunkY, chunkZ + 1)) return false;
				}
			}
		}
		for (int j = -distance; j <= distance; ++j) {
			for (int k = -distance; k <= distance; ++k) {
				if (!camera.getBlockData().isAirBlock(new BlockPos(xCenter + j, yCenter + up, zCenter + k))) return false;
			}
		}
		return true;
	}

	/**
	 * @author Ken Butler/shadowking97
	 */
	private boolean isAboveNullLayer(int x, int y, int z) {
		if (y + 1 > 15) return true;
		int x2 = x << 4;
		int z2 = z << 4;
		int y2 = (y << 4) + 15;
		int yl = (y + 1) << 4;
		for (int i = 0; i < 15; i++)
			for (int j = 0; j < 15; j++)
				if (!isBlockNormalCube(camera.getBlockData(), new BlockPos(x2 + i, y2, z2 + i))) return false;
		if (camera.chunkLevelsExist(new BlockPos(x, y, z), yl, yl + 15)) return true;
		return false;
	}

	private boolean isBlockNormalCube(IBlockAccess blockData, BlockPos pos) {
		IBlockState block = blockData.getBlockState(pos);
		return block.isNormalCube();
	}

	private void checkCameraY() {
		BlockPos temp = target;
		if (camera.chunkExists(temp)) {
			if (camera.getBlockData().getBlockState(temp).getMaterial().blocksMovement()) {
				while (temp.getY() > 0 && camera.getBlockData().getBlockState(temp).getMaterial().blocksMovement())
					temp = temp.down();
				
				if (temp.getY() <= 0)
					temp = target;
				else
					temp = temp.up(2);
			} else {
				while (temp.getY() < 256 && !camera.getBlockData().getBlockState(temp).getMaterial().blocksMovement())
					;
				if (temp.getY() >= 256)
					temp = target;
				else
					temp = temp.up();
			}
			this.setCenterPoint(temp.getX(), temp.getY(), temp.getZ());
		}
	}

	private void setCenterPoint(int x, int y, int z) {
		xCenter = x;
		yCenter = y - 1;
		zCenter = z;
		positionSet = true;
	}
}
