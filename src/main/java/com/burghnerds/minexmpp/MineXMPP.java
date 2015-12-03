package com.burghnerds.minexmpp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import com.burghnerds.minexmpp.XMPPConnectionHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerDisconnectionFromClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;

@Mod(name = MineXMPP.NAME, modid = MineXMPP.MODID, version = MineXMPP.VERSION)
public class MineXMPP
{
	public static final String MODID = "minexmppslient";
	public static final String VERSION = "0.01";
	public static final String NAME = "Mine-XMPP Server";

	
	
	private List<XMPPConnectionHandler> chats = new ArrayList<XMPPConnectionHandler>();
	private String resource;
	private String service;
	private String host;
	private String chatRoomName;
	private String chatRoomPrefix;

	@Instance(MODID)
	public static MineXMPP instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) 
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());


		config.load();

		resource = config.get(Configuration.CATEGORY_GENERAL, "resource", "MineXMPP").getString();
		service = config.get(Configuration.CATEGORY_GENERAL, "service", "service").getString();
		host = config.get(Configuration.CATEGORY_GENERAL, "host", "localhost").getString();
		chatRoomName = config.get(Configuration.CATEGORY_GENERAL, "room-name", "chat").getString();
		chatRoomPrefix = config.get(Configuration.CATEGORY_GENERAL, "service-chatroom-prefix", "conference").getString();

		config.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)	
	{
		//Register to Event Handlers
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);

	}

	@SubscribeEvent
	public void playerConnected(PlayerLoggedInEvent event)
	{
		String name = event.player.getDisplayName();
		boolean found = false;
		for (int i = 0; i < chats.size(); i++) {
			if (chats.get(i).getUsername().equals(name)) {
				chats.get(i).connect();
			}
		}
		if (!found) {
			XMPPConnectionHandler chat = new XMPPConnectionHandler(name, name, resource, service, host, chatRoomName, chatRoomPrefix);
			chat.connect();
			chats.add(chat);
		}
	}

	@SubscribeEvent
	public void serverStop(Unload event)
	{
		for (int i = 0; i < chats.size(); i++) {
			chats.get(i).disconnect();
		}
}
		
	@SubscribeEvent(priority = EventPriority.LOW)
	public void chatEvent(ServerChatEvent event)
	{
		String name = event.player.getDisplayName();
		for (int i = 0; i < chats.size(); i++) {
			if (chats.get(i).getUsername().equals(name)) {
				chats.get(i).send(event.message);
			}
		}
	}
}
