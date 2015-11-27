package com.burghnerds.minexmpp;


import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.IOException;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.tcp.*;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MineXMPP.MODID, version = MineXMPP.VERSION)
public class MineXMPP
{
	public static final String MODID = "MineXMPP";
	public static final String VERSION = "0.01";

	private XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
	private AbstractXMPPConnection connection;

	private static String username, password, resource, service, host;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());


		config.load();

		username = config.get(config.CATEGORY_GENERAL, "username", "username").getString();
		password = config.get(config.CATEGORY_GENERAL, "password", "password").getString();		
		resource = config.get(config.CATEGORY_GENERAL, "resource", "resource").getString();
		service = config.get(config.CATEGORY_GENERAL, "service", "service").getString();
		host = config.get(config.CATEGORY_GENERAL, "host", "host").getString();

		config.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		System.out.println("Test Upload");


		configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setUsernameAndPassword(username, password);
		configBuilder.setResource(resource);
		configBuilder.setServiceName(service);
		configBuilder.setHost("hephaestus");
		configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		connection = new XMPPTCPConnection(configBuilder.build());

		//SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
		//SASLAuthentication.unBlacklistSASLMechanism("DIGEST-MD5");

		try {
			System.out.println("Attempting to contact XMPP Server");
			connection.connect();
			System.out.println("Connection Successful. Attempting to log in");
			connection.login();
			System.out.println("Login successful");
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
}
