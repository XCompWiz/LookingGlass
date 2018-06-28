package com.xcompwiz.lookingglass.command;

import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class CommandBaseAdv extends CommandBase {

	public void sendToAdmins(ICommandSender agent, String text, Object[] objects) {
		notifyCommandListener(agent, this, text, objects);
	}

	public static EntityPlayerMP getTargetPlayer(MinecraftServer server, ICommandSender sender, String target) throws PlayerNotFoundException, CommandException {
		EntityPlayerMP entityplayermp = EntitySelector.matchOnePlayer(sender, target);

		if (entityplayermp == null) {
			entityplayermp = server.getPlayerList().getPlayerByUsername(target);
		}
		if (entityplayermp == null) { throw new PlayerNotFoundException("commands.generic.player.unspecified"); }
		return entityplayermp;
	}

	public static Integer getSenderDimension(ICommandSender sender) throws CommandException {
		World w = sender.getEntityWorld();
		if (w == null) throw new CommandException("You must specify a dimension to use this command from the commandline");
		return w.provider.getDimension();
	}

	/**
	 * Returns the given ICommandSender as a EntityPlayer or throw an exception.
	 * @throws CommandException if the tile entity cannot be found
	 */
	public static TileEntity getCommandSenderAsTileEntity(ICommandSender sender) throws CommandException {
		try {
			World world = sender.getEntityWorld();
			BlockPos coords = sender.getPosition();
			return world.getTileEntity(coords);
		} catch (Exception e) {
			throw new CommandException("Could not get tile entity");
		}
	}

	public static double handleRelativeNumber(ICommandSender sender, double origin, String arg) throws NumberInvalidException {
		return handleRelativeNumber(sender, origin, arg, -30000000, 30000000);
	}

	public static double handleRelativeNumber(ICommandSender par1ICommandSender, double origin, String arg, int min, int max) throws NumberInvalidException {
		boolean relative = arg.startsWith("~");
		boolean random = arg.startsWith("?");
		if (random) relative = true;
		double d1 = relative ? origin : 0.0D;

		if (!relative || arg.length() > 1) {
			boolean flag1 = arg.contains(".");

			if (relative) {
				arg = arg.substring(1);
			}

			double d2 = parseDouble(arg);
			if (random) {
				Random rand = new Random();
				d1 += (rand.nextDouble() * 2 - 1) * d2;
			} else {
				d1 += d2;
			}

			if (!flag1 && !relative) {
				d1 += 0.5D;
			}
		}

		if (min != 0 || max != 0) {
			if (d1 < min) { throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[] { Double.valueOf(d1), Integer.valueOf(min) }); }

			if (d1 > max) { throw new NumberInvalidException("commands.generic.double.tooBig", new Object[] { Double.valueOf(d1), Integer.valueOf(max) }); }
		}

		return d1;
	}

	/**
	 * Returns the player for a username as an Entity or throws an exception.
	 */
	public static Entity parsePlayerByName(MinecraftServer server, String name) throws PlayerNotFoundException {
		EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(name);
		if (player != null) { return player; }
		throw new PlayerNotFoundException("commands.myst.generic.player.notfound", new Object[] { name });
	}

	public static float parseFloat(ICommandSender par0ICommandSender, String par1Str) throws NumberInvalidException {
		try {
			return Float.parseFloat(par1Str);
		} catch (NumberFormatException numberformatexception) {
			throw new NumberInvalidException("commands.generic.num.invalid", new Object[] { par1Str });
		}
	}
}
