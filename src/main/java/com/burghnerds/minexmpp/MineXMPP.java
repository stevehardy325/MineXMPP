package com.burghnerds.minexmpp;


import java.io.IOException;

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

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.ServerChatEvent;

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

	@Instance(MODID)
	public static MineXMPP instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) 
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());


		config.load();

		username = config.get(Configuration.CATEGORY_GENERAL, "username", "username").getString();
		password = config.get(Configuration.CATEGORY_GENERAL, "password", "").getString();		
		resource = config.get(Configuration.CATEGORY_GENERAL, "resource", "resource").getString();
		service = config.get(Configuration.CATEGORY_GENERAL, "service", "service").getString();
		host = config.get(Configuration.CATEGORY_GENERAL, "host", "host").getString();
		chatRoomName = config.get(Configuration.CATEGORY_GENERAL, "room-name", "chat").getString();
		chatRoomPrefix = config.get(Configuration.CATEGORY_GENERAL, "service-chatroom-prefix", "conference").getString();

		config.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)	
	{
		//Initialize configurations
		configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setUsernameAndPassword(username, password);
		configBuilder.setResource(resource);
		configBuilder.setServiceName(service);
		configBuilder.setHost(host);
		configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		connection = new XMPPTCPConnection(configBuilder.build());

		//Register to Event Handlers
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);

	}

	@EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
		try 
		{
			final String fullChatName = chatRoomName+"@"+chatRoomPrefix +"."+service;

			System.out.println("Attempting to contact XMPP Server");
			connection.connect();

			System.out.println("Connection Successful. Attempting to log in");
			connection.login();

			System.out.println("Login successful. Attemtping to join public chatroom " + fullChatName);
			manager = MultiUserChatManager.getInstanceFor(connection);
			chatroom = manager.getMultiUserChat(fullChatName);
			MessageListener listener = new MessageListener() 
			{
				@Override
				public void processMessage(Message message) 
				{
					String sender = message.getFrom().replace(fullChatName+"/", "");
					if (!sender.equals(username)) {
						System.out.println(sender + username);
						String text = message.getBody();
						String chatMessage = "<" + sender + "> " + text;
						IChatComponent iChatMessage = new ChatComponentText(chatMessage);
						Minecraft.getMinecraft().thePlayer.addChatMessage(iChatMessage);	
					}

				}
			};
			chatroom.addMessageListener(listener);
			chatroom.join(username);
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

	@EventHandler
	public void serverStop(FMLServerStoppingEvent event)
	{
		connection.disconnect();
	}
		
	@SubscribeEvent(priority = EventPriority.LOW)
	public void chatEvent(ServerChatEvent event)
	{
		EntityPlayerMP player = event.player;
		if (player.getEntityId() == Minecraft.getMinecraft().thePlayer.getEntityId())
		{
			String message = event.message;
			try 
			{
				chatroom.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	

}
