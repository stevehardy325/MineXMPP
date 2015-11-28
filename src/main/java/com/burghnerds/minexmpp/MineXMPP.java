package com.burghnerds.minexmpp;


import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.ServerChatEvent;

import java.io.IOException;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.tcp.*;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = MineXMPP.MODID, version = MineXMPP.VERSION)
public class MineXMPP
{
	public static final String MODID = "MineXMPP";
	public static final String VERSION = "0.01";

	private XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
	private AbstractXMPPConnection connection;
	private MultiUserChatManager manager;
	private MultiUserChat chatroom;

	private static String username, password, resource, service, host, chatRoomName, chatRoomPrefix;
	
	private boolean isConnected = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) 
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());


		config.load();

		username = config.get(config.CATEGORY_GENERAL, "username", "username").getString();
		password = config.get(config.CATEGORY_GENERAL, "password", "").getString();		
		resource = config.get(config.CATEGORY_GENERAL, "resource", "resource").getString();
		service = config.get(config.CATEGORY_GENERAL, "service", "service").getString();
		host = config.get(config.CATEGORY_GENERAL, "host", "host").getString();
		chatRoomName = config.get(config.CATEGORY_GENERAL, "room-name", "chat").getString();
		chatRoomPrefix = config.get(config.CATEGORY_GENERAL, "service-chatroom-prefix", "conference").getString();

		config.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)	
	{
		System.out.println("Test Upload");
		
		//Initialize configurations
		configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setUsernameAndPassword(username, password);
		configBuilder.setResource(resource);
		configBuilder.setServiceName(service);
		configBuilder.setHost(host);
		configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		connection = new XMPPTCPConnection(configBuilder.build());


		try {
			System.out.println("Attempting to contact XMPP Server");
			connection.connect();
			
			System.out.println("Connection Successful. Attempting to log in");
			connection.login();
			
			String fullChatName = chatRoomName+"@"+chatRoomPrefix +"."+service;
			System.out.println("Login successful. Attemtping to join public chatroom " + fullChatName);
			manager = MultiUserChatManager.getInstanceFor(connection);
			chatroom = manager.getMultiUserChat(fullChatName);
			chatroom.join(username);
			
			isConnected = true;
		} catch (XMPPException e) {
			System.out.println("XMPP Exception Occurred");
			e.printStackTrace();
		} catch (SmackException e) {
			System.out.println("An error occurred");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO Exception Occurred");
			e.printStackTrace();
		} finally {
			System.out.println("MineXMPP Finished Loading");
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
    public void chatEvent(ServerChatEvent event)
    {
        if (isConnected)
			try {
				chatroom.sendMessage(event.message);
			} catch (NotConnectedException e) {
				isConnected = false;
				e.printStackTrace();
			}
    }

}
