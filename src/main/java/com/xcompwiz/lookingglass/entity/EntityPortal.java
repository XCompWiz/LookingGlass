package com.xcompwiz.lookingglass.entity;

import com.xcompwiz.lookingglass.api.animator.CameraAnimatorPlayer;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Despite it's name, this isn't so much a doorway or window as it is a moving picture. More Harry Potter's portraits than Portal's portals. (Man I wish the
 * best example of portal rendering in games wasn't called Portal.... So hard to reference sanely.)
 */
public class EntityPortal extends Entity {
	// We store the dimension ID we point at in the dataWatcher at this index.
    private static final DataParameter<Integer> targetID = EntityDataManager.<Integer>createKey(EntityPortal.class, DataSerializers.VARINT);

	// How long the window has to live. Functions as a countdown timer.
	private long				lifetime	= 1000L;

	@SideOnly(Side.CLIENT)
	private IWorldView			activeview;

	public EntityPortal(World world) {
		super(world);
		dataManager.register(targetID, 0);
	}

	public EntityPortal(World world, int targetdim, int posX, int posY, int posZ) {
		this(world);
		this.setTarget(targetdim);
		this.setPosition(posX, posY, posZ);
	}

	/** Puts the dim id target in the datawatcher. */
	private void setTarget(int targetdim) {
		dataManager.set(targetID, targetdim);
		//XXX: Technically speaking, it might be wise to design this so that it can change targets, but that's not needed for this class.
		// If it was, we'd have this function kill any active views when the target changed, causing it to open a new view for the new target.
	}

	/** Gets the target dimension id */
	private int getTarget() {
		return dataManager.get(targetID);
	}

	@Override
	protected void entityInit() {}

	@Override
	@SideOnly(Side.CLIENT)
	public void setDead() {
		super.setDead();
		releaseActiveView();
	}

	@Override
	public void onUpdate() {
		// Countdown to die
		--lifetime;
		if (lifetime <= 0) {
			this.setDead();
			return;
		}
		super.onUpdate();
	}

	@SideOnly(Side.CLIENT)
	public IWorldView getActiveView() {
		if (!world.isRemote) return null;
		if (activeview == null) {
			activeview = ProxyWorldManager.createWorldView(getTarget(), null, 160, 240);
			if (activeview != null) {
				// We set the player animator on our portrait. This makes the view move a little depending on how the user looks at it. Not quite a replacement for portal rendering, but cool looking anyway.
				activeview.setAnimator(new CameraAnimatorPlayer(activeview.getCamera(), this, Minecraft.getMinecraft().player));
			}
		}
		return activeview;
	}

	@SideOnly(Side.CLIENT)
	public void releaseActiveView() {
		if (activeview != null) ProxyWorldManager.destroyWorldView((WorldView) activeview);
		activeview = null;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		setTarget(nbt.getInteger("Dimension"));
		lifetime = nbt.getLong("lifetime");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("Dimension", getTarget());
		nbt.setLong("lifetime", lifetime);
	}

}
