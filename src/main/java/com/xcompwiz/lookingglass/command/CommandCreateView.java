package com.xcompwiz.lookingglass.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import com.xcompwiz.lookingglass.entity.EntityPortal;

public class CommandCreateView extends CommandBaseAdv {
	@Override
	public String getCommandName() {
		return "lg-viewdim";
	}

	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender) {
		return "/" + this.getCommandName() + " targetdim [dim, x, y, z]";
	}

	@Override
	public void processCommand(ICommandSender agent, String[] args) {
		int targetdim = 0;
		Integer dim = null;
		ChunkCoordinates coords = null;

		//XXX: Set Coordinates of view location?
		if (args.length > 0) {
			String sTarget = args[0];
			targetdim = parseInt(agent, sTarget);
		} else {
			throw new WrongUsageException("Could not parse command.");
		}
		if (args.length > 4) {
			dim = parseInt(agent, args[1]);
			Entity caller = null;
			try {
				caller = getCommandSenderAsPlayer(agent);
			} catch (Exception e) {
			}
			int x = (int) handleRelativeNumber(agent, (caller != null ? caller.posX : 0), args[2]);
			int y = (int) handleRelativeNumber(agent, (caller != null ? caller.posY : 0), args[3], 0, 0);
			int z = (int) handleRelativeNumber(agent, (caller != null ? caller.posZ : 0), args[4]);
			coords = new ChunkCoordinates(x, y, z);
		}
		if (coords == null) {
			dim = getSenderDimension(agent);
			coords = agent.getPlayerCoordinates();
		}
		if (coords == null) throw new WrongUsageException("Location Required");

		WorldServer worldObj = DimensionManager.getWorld(dim);
		if (worldObj == null) { throw new CommandException("The target world is not loaded"); }

		EntityPortal portal = new EntityPortal(worldObj, targetdim, coords.posX, coords.posY, coords.posZ);
		worldObj.spawnEntityInWorld(portal);

		sendToAdmins(agent, "A window to dimension " + targetdim + " has been created.", new Object[0]);
	}
}
