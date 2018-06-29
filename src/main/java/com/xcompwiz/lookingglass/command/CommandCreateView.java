package com.xcompwiz.lookingglass.command;

import com.xcompwiz.lookingglass.entity.EntityPortal;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class CommandCreateView extends CommandBaseAdv {
	@Override
	public String getName() {
		return "lg-viewdim";
	}

	/**
	 * Return the required permission level for this command.
	 */
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender par1ICommandSender) {
		return "/" + this.getName() + " targetdim [dim, x, y, z]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		int targetdim = 0;
		Integer dim = null;
		double x = 0;
		double y = 0;
		double z = 0;
		float yaw = 0;
		boolean coordsSet = false;

		//TODO: Set Coordinates of view location?
		if (args.length > 0) {
			String sTarget = args[0];
			targetdim = parseInt(sTarget);
		} else {
			throw new WrongUsageException("Could not parse command.");
		}

		Entity caller = null;
		try {
			caller = getCommandSenderAsPlayer(sender);
		} catch (Exception e) {
		}

		if (args.length > 4) {
			dim = parseInt(args[1]);
			x = handleRelativeNumber(sender, (caller != null ? caller.posX : 0), args[2]);
			y = handleRelativeNumber(sender, (caller != null ? caller.posY : 0), args[3], 0, 0);
			z = handleRelativeNumber(sender, (caller != null ? caller.posZ : 0), args[4]);
			coordsSet = true;
			if (args.length > 5) {
				yaw = (float) handleRelativeNumber(sender, (caller != null ? caller.posZ : 0), args[5]);
			}
		}
		if (!coordsSet) {
			dim = getSenderDimension(sender);
			if (caller == null)
			{
				BlockPos coords = sender.getPosition();
				x = coords.getX();
				y = coords.getY();
				z = coords.getZ();
			}
			else
			{
				x = caller.posX;
				y = caller.posY;
				z = caller.posZ;
			}
			yaw = sender.getCommandSenderEntity().rotationYaw;
		}
		else
			throw new WrongUsageException("Location Required");

		WorldServer worldObj = DimensionManager.getWorld(dim);
		if (worldObj == null) { throw new CommandException("The target world is not loaded"); }

		EntityPortal portal = new EntityPortal(worldObj, targetdim, x, y, z, yaw, 1000);
		worldObj.spawnEntity(portal);

		sendToAdmins(sender, "A window to dimension " + targetdim + " has been created.", new Object[0]);
	}
}
