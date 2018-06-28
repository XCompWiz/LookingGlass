package com.xcompwiz.lookingglass;

import java.io.File;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import com.google.common.collect.ImmutableList;
import com.xcompwiz.lookingglass.apiimpl.APIProviderImpl;
import com.xcompwiz.lookingglass.command.CommandCreateView;
import com.xcompwiz.lookingglass.core.CommonProxy;
import com.xcompwiz.lookingglass.core.LookingGlassForgeEventHandler;
import com.xcompwiz.lookingglass.imc.IMCHandler;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.network.ServerPacketDispatcher;
import com.xcompwiz.lookingglass.network.packet.PacketChunkInfo;
import com.xcompwiz.lookingglass.network.packet.PacketCloseView;
import com.xcompwiz.lookingglass.network.packet.PacketCreateView;
import com.xcompwiz.lookingglass.network.packet.PacketRequestChunk;
import com.xcompwiz.lookingglass.network.packet.PacketRequestTE;
import com.xcompwiz.lookingglass.network.packet.PacketRequestWorldInfo;
import com.xcompwiz.lookingglass.network.packet.PacketTileEntityNBT;
import com.xcompwiz.lookingglass.network.packet.PacketWorldInfo;
import com.xcompwiz.lookingglass.proxyworld.LookingGlassEventHandler;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;

@Mod(modid = LookingGlass.MODID, name = "LookingGlass", version = LookingGlass.VERSION)
public class LookingGlass {
	public static final String	MODID	= "lookingglass";
	public static final String	VERSION	= "@VERSION@";

	@Instance(LookingGlass.MODID)
	public static LookingGlass	instance;

	@SidedProxy(clientSide = "com.xcompwiz.lookingglass.client.ClientProxy", serverSide = "com.xcompwiz.lookingglass.core.CommonProxy")
	public static CommonProxy	sidedProxy;

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		//Initialize the packet handling
		LookingGlassPacketManager.registerPacketHandler(new PacketCreateView(), (byte) 10);
		LookingGlassPacketManager.registerPacketHandler(new PacketCloseView(), (byte) 11);
		LookingGlassPacketManager.registerPacketHandler(new PacketWorldInfo(), (byte) 100);
		LookingGlassPacketManager.registerPacketHandler(new PacketChunkInfo(), (byte) 101);
		LookingGlassPacketManager.registerPacketHandler(new PacketTileEntityNBT(), (byte) 102);
		LookingGlassPacketManager.registerPacketHandler(new PacketRequestWorldInfo(), (byte) 200);
		LookingGlassPacketManager.registerPacketHandler(new PacketRequestChunk(), (byte) 201);
		LookingGlassPacketManager.registerPacketHandler(new PacketRequestTE(), (byte) 202);

		LookingGlassPacketManager.bus = NetworkRegistry.INSTANCE.newEventDrivenChannel(LookingGlassPacketManager.CHANNEL);
		LookingGlassPacketManager.bus.register(new LookingGlassPacketManager());

		// Load our basic configs
		ModConfigs.loadConfigs(new Configuration(event.getSuggestedConfigurationFile()));

		// Here we use the recommended config file to establish a good place to put a log file for any proxy world error logs.  Used primarily to log the full errors when ticking or rendering proxy worlds. 
		File configroot = event.getSuggestedConfigurationFile().getParentFile();
		// Main tick handler. Handles FML events.
		FMLCommonHandler.instance().bus().register(new LookingGlassEventHandler(new File(configroot.getParentFile(), "logs/proxyworlds.log")));
		// Forge event handler
		MinecraftForge.EVENT_BUS.register(new LookingGlassForgeEventHandler());

		// Initialize the API provider system.  Beware, this way be dragons.
		APIProviderImpl.init();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Our one and only entity.
		EntityRegistry.registerModEntity(com.xcompwiz.lookingglass.entity.EntityPortal.class, "lookingglass.portal", 216, this, 64, 10, false);

		sidedProxy.init();
	}

	@EventHandler
	public void handleIMC(IMCEvent event) {
		// Catch IMC messages and send them off to our IMC handler
		ImmutableList<IMCMessage> messages = event.getMessages();
		IMCHandler.process(messages);
	}

	@EventHandler
	public void postinit(FMLPostInitializationEvent event) {}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		MinecraftServer mcserver = event.getServer();
		// Register commands
		((ServerCommandManager) mcserver.getCommandManager()).registerCommand(new CommandCreateView());
		// Start up the packet dispatcher we use for throttled data to client.
		ServerPacketDispatcher.getInstance().start(); //Note: This might need to be preceded by a force init of the ServerPacketDispatcher.  Doesn't seem to currently have any issues, though.
	}

	@EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		// Shutdown our throttled packet dispatcher
		ServerPacketDispatcher.getInstance().halt();
		ServerPacketDispatcher.shutdown();
	}
}
