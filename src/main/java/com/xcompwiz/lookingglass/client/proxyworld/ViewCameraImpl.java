package com.xcompwiz.lookingglass.client.proxyworld;

import com.xcompwiz.lookingglass.api.view.IViewCamera;
import com.xcompwiz.lookingglass.entity.EntityCamera;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ViewCameraImpl implements IViewCamera {
	private EntityCamera	camera;

	public ViewCameraImpl(EntityCamera camera) {
		this.camera = camera;
	}

	@Override
	public void addRotations(float yaw, int pitch) {
        this.camera.rotationYaw += (yaw*0.15);
        this.camera.rotationPitch -= (pitch*0.15);
        this.camera.rotationPitch = Math.max(-90, Math.min(90, this.camera.rotationPitch));
	}

	@Override
	public void setYaw(float f) {
		this.camera.prevRotationYaw = f;
		this.camera.rotationYaw = f;
	}

	@Override
	public float getYaw() {
		return this.camera.rotationYaw;
	}

	@Override
	public void setPitch(float f) {
		this.camera.prevRotationPitch = f;
		this.camera.rotationPitch = f;
	}

	@Override
	public float getPitch() {
		return this.camera.rotationPitch;
	}

	@Override
	public void setLocation(double x, double y, double z) {
		this.camera.setLocationAndAngles(x, y, z, this.camera.rotationYaw, this.camera.rotationPitch);
	}

	@Override
	public BlockPos getPosition() {
		return this.camera.getPosition();
	}

	@Override
	public IBlockAccess getBlockData() {
		return this.camera.world;
	}

	@Override
	public boolean chunkExists(BlockPos pos) {
		return !camera.world.getChunkFromBlockCoords(pos).isEmpty();
	}

	@Override
	public boolean chunkLevelsExist(BlockPos pos, int startY, int endY) {
		return !camera.world.getChunkFromBlockCoords(pos).isEmptyBetween(startY, endY);
	}

}
