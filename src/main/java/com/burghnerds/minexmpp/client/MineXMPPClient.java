package com.burghnerds.minexmpp.client;

import com.burghnerds.minexmpp.XMPPConnectionHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;

@Mod(name = MineXMPPClient.NAME, modid = MineXMPPClient.MODID, version = MineXMPPClient.VERSION)
public class MineXMPPClient {
	public static final String MODID = "mine-xmpp_client";
	public static final String VERSION = "0.01";
	public static final String NAME = "Mine-XMPP Client";

	private XMPPConnectionHandler chat;

	@Instance(MineXMPPClient.MODID)
	public static MineXMPPClient instance;

	@EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		final Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();

		final String username = config.get(Configuration.CATEGORY_GENERAL, "username", "username").getString();
		final String password = config.get(Configuration.CATEGORY_GENERAL, "password", "").getString();
		final String resource = config.get(Configuration.CATEGORY_GENERAL, "resource", "MineXMPP").getString();
		final String service = config.get(Configuration.CATEGORY_GENERAL, "service", "service").getString();
		final String host = config.get(Configuration.CATEGORY_GENERAL, "host", "localhost").getString();
		final String chatRoomName = config.get(Configuration.CATEGORY_GENERAL, "room-name", "chat").getString();
		final String chatRoomPrefix = config
				.get(Configuration.CATEGORY_GENERAL, "service-chatroom-prefix", "conference").getString();

		config.save();

		this.chat = new XMPPConnectionHandler(username, password, resource, service, host, chatRoomName,
				chatRoomPrefix);
	}

	@EventHandler
	public void init(final FMLInitializationEvent event) {
		this.chat.setup();

		// Register to Event Handlers
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);

	}

	@SubscribeEvent
	public void connectedToWorld(final Load event) {
		this.chat.connect();
	}

	@SubscribeEvent
	public void serverStop(final Unload event) {
		this.chat.disconnect();
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void chatEvent(final ClientChatReceivedEvent event) {
		this.chat.send(event.message.getUnformattedText());
	}
}
