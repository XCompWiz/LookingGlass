package com.xcompwiz.lookingglass.entity;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import com.xcompwiz.lookingglass.api.animator.ICameraAnimator;

/**
 * Our camera entity. This is made a player so that we can replace the player client-side when doing rendering.
 * At the bottom of the class we create a bunch of method stubs to override higher level logic, so that our "player" doesn't act like one.
 */
public class EntityCamera extends EntityClientPlayerMP {

	private ICameraAnimator		animator;
	private ChunkCoordinates	target;
	private boolean				defaultSpawn	= false;

	private float				fovmultiplier	= 1;

	public EntityCamera(World worldObj, ChunkCoordinates spawn) {
		super(Minecraft.getMinecraft(), worldObj, Minecraft.getMinecraft().getSession(), null, null);
		this.target = spawn;
		if (target == null) {
			defaultSpawn = true;
			ChunkCoordinates cc = worldObj.provider.getSpawnPoint();
			int y = updateTargetPosition(cc);
			target = new ChunkCoordinates(cc.posX, y, cc.posZ);
		}
		this.setPositionAndUpdate(target.posX, target.posY, target.posZ);
	}

	public void setAnimator(ICameraAnimator animator) {
		this.animator = animator;
		if (this.animator != null) this.animator.setTarget(target);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(1);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D);
	}

	public void updateWorldSpawn(ChunkCoordinates cc) {
		if (defaultSpawn) {
			int y = updateTargetPosition(cc);
			target = new ChunkCoordinates(cc.posX, y, cc.posZ);
			this.setPositionAndUpdate(target.posX, target.posY, target.posZ);
			if (animator != null) animator.setTarget(cc);
			this.refreshAnimator();
		}
	}

	private int updateTargetPosition(ChunkCoordinates target) {
		int x = target.posX;
		int y = target.posY;
		int z = target.posZ;
		if (!this.worldObj.getChunkFromBlockCoords(x, z).isEmpty()) {
			if (this.worldObj.getBlock(x, y, z).getBlocksMovement(this.worldObj, x, y, z)) {
				while (y > 0 && this.worldObj.getBlock(x, --y, z).getBlocksMovement(this.worldObj, x, y, z))
					;
				if (y == 0) y = target.posY;
				else ++y;
			} else {
				while (y < 256 && !this.worldObj.getBlock(x, ++y, z).getBlocksMovement(this.worldObj, x, y, z))
					;
				if (y == 256) y = target.posY;
			}
			return y;
		}
		return target.posY;
	}

	public void refreshAnimator() {
		if (this.animator != null) animator.refresh();
	}

	public void tick(long dt) {
		if (this.animator != null) animator.update(dt);
	}

	@Override
	public float getFOVMultiplier() {
		return fovmultiplier;
	}

	public void setFOVMult(float fovmult) {
		fovmultiplier = fovmult;
	}

	/*
	 * POSSIBLY UNNECESSARY CODE TO PREVENT OTHER CODE FROM RUNNING
	 */
	@Override
	public void onEntityUpdate() {}

	@Override
	public void onLivingUpdate() {}

	@Override
	public void onUpdate() {}

	@Override
	protected int getExperiencePoints(EntityPlayer par1EntityPlayer) {
		return 0;
	}

	@Override
	protected boolean isAIEnabled() {
		return false;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {}

	@Override
	public void setAIMoveSpeed(float par1) {}

	@Override
	protected void updateAITasks() {}

	@Override
	public ItemStack getHeldItem() {
		return null;
	}

	@Override
	public ItemStack getEquipmentInSlot(int par1) {
		return null;
	}

	@Override
	public void setCurrentItemOrArmor(int par1, ItemStack par2ItemStack) {}

	@Override
	public ItemStack[] getLastActiveItems() {
		return null;
	}

	@Override
	protected void dropEquipment(boolean par1, int par2) {}

	@Override
	protected void fall(float par1) {}

	@Override
	protected void updateFallState(double par1, boolean par3) {}

	@Override
	protected void onDeathUpdate() {
		this.setDead();
	}

	@Override
	public EntityLivingBase getAITarget() {
		return null;
	}

	@Override
	public void setRevengeTarget(EntityLivingBase par1) {}

	@Override
	public EntityLivingBase getLastAttacker() {
		return null;
	}

	@Override
	public void setLastAttacker(Entity par1) {}

	@Override
	protected void updatePotionEffects() {}

	@Override
	public void clearActivePotions() {}

	@Override
	public boolean isPotionActive(int par1) {
		return false;
	}

	@Override
	public boolean isPotionActive(Potion par1) {
		return false;
	}

	@Override
	public PotionEffect getActivePotionEffect(Potion par1) {
		return null;
	}

	@Override
	public void addPotionEffect(PotionEffect par1) {}

	@Override
	public boolean isPotionApplicable(PotionEffect par1) {
		return false;
	}

	@Override
	public boolean isEntityUndead() {
		return false;
	}

	@Override
	public void removePotionEffectClient(int par1) {}

	@Override
	public void removePotionEffect(int par1) {}

	@Override
	protected void onNewPotionEffect(PotionEffect par1) {}

	@Override
	protected void onChangedPotionEffect(PotionEffect par1, boolean par2) {}

	@Override
	protected void onFinishedPotionEffect(PotionEffect par1) {}

	@Override
	public void heal(float par1) {}

	@Override
	public boolean attackEntityFrom(DamageSource par1, float par2) {
		return false;
	}

	@Override
	public void renderBrokenItemStack(ItemStack par1) {}

	@Override
	public void onDeath(DamageSource par1) {
		this.worldObj.setEntityState(this, (byte) 3);
	}

	@Override
	public void knockBack(Entity par1Entity, float par2, double par3, double par5) {}

	@Override
	public boolean isOnLadder() {
		return false;
	}

	@Override
	public int getTotalArmorValue() {
		return 0;
	}

	@Override
	protected float applyArmorCalculations(DamageSource par1DamageSource, float par2) {
		return par2;
	}

	@Override
	protected float applyPotionDamageCalculations(DamageSource par1DamageSource, float par2) {
		return par2;
	}

	@Override
	protected void damageEntity(DamageSource par1, float par2) {}

	@Override
	public void swingItem() {}

	@Override
	protected void updateArmSwingProgress() {}

	@Override
	public void setSprinting(boolean par1) {}

	@Override
	protected float getSoundVolume() {
		return 0F;
	}

	@Override
	public void dismountEntity(Entity par1Entity) {}

	@Override
	public void moveEntityWithHeading(float par1, float par2) {}

	@Override
	public void updateRidden() {}

	@Override
	public void setJumping(boolean par1) {}

	@Override
	public void onItemPickup(Entity par1Entity, int par2) {}

	@Override
	public boolean canEntityBeSeen(Entity par1Entity) {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean handleWaterMovement() {
		return false;
	}

	@Override
	public boolean isInsideOfMaterial(Material par1Material) {
		return false;
	}

	@Override
	public boolean handleLavaMovement() {
		return false;
	}

	@Override
	public void moveFlying(float par1, float par2, float par3) {}

	@Override
	public float getBrightness(float par1) {
		return 0;
	}

	@Override
	public void applyEntityCollision(Entity par1Entity) {}

	@Override
	public boolean isBurning() {
		return false;
	}

	@Override
	public boolean isRiding() {
		return false;
	}

	@Override
	public boolean isSneaking() {
		return false;
	}

	@Override
	public boolean isInvisible() {
		return true;
	}

	@Override
	public void onStruckByLightning(EntityLightningBolt par1) {}

	@Override
	public boolean isEntityInvulnerable() {
		return true;
	}

	@Override
	public void travelToDimension(int par1) {}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return false;
	}

	@Override
	protected void collideWithEntity(Entity par1Entity) {}

	@Override
	protected void collideWithNearbyEntities() {}

	@Override
	public boolean doesEntityNotTriggerPressurePlate() {
		return true;
	}
}
