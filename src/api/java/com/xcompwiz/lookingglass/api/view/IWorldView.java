package com.xcompwiz.lookingglass.api.view;

import com.xcompwiz.lookingglass.api.animator.ICameraAnimator;

public interface IWorldView {

	/**
	 * @return The OpenGL texture id for rendering the view
	 */
	public int getTexture();

	/**
	 * This needs to be called to request LookingGlass rerender the view to the texture
	 */
	public void markDirty();

	/**
	 * This will be activated once the view has chunks and is ready to render
	 * @return True if the view has been rendered to the texture.
	 */
	public boolean isReady();

	/**
	 * Sets the animator object for the camera. This will be updated before each render frame.
	 * @param animator
	 */
	public void setAnimator(ICameraAnimator animator);

	/**
	 * Returns the view's camera object. This allows you to create animators for it and adjust its location locally.
	 * @return the entity object from which rendering is handled
	 */
	public IViewCamera getCamera();

	/**
	 * @deprecated This function no longer does anything and will be removed in an upcoming version.
	 */
	@Deprecated
	public void grab();

	/**
	 * @deprecated This function no longer does anything and will be removed in an upcoming version.
	 */
	@Deprecated
	public boolean release();

}
