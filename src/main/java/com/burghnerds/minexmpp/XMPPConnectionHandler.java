package com.burghnerds.minexmpp;

import java.io.IOException;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.AlreadyLoggedInException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class XMPPConnectionHandler {

	private XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
	private AbstractXMPPConnection connection;
	private MultiUserChatManager manager;
	private MultiUserChat chatroom;
	private MessageListener listener;

	public static String username, password, resource, service, host, chatRoomName, chatRoomPrefix;

	private static boolean connected = false;

	public XMPPConnectionHandler(final String username, final String password, final String resource,
			final String service, final String host, final String chatRoomName, final String chatRoomPrefix) {
		XMPPConnectionHandler.username = username;
		XMPPConnectionHandler.password = password;
		XMPPConnectionHandler.resource = resource;
		XMPPConnectionHandler.service = service;
		XMPPConnectionHandler.host = host;
		XMPPConnectionHandler.chatRoomName = chatRoomName;
		XMPPConnectionHandler.chatRoomPrefix = chatRoomPrefix;
	}

	public void setup() {
		this.configBuilder = XMPPTCPConnectionConfiguration.builder();
		this.configBuilder.setUsernameAndPassword(XMPPConnectionHandler.username, XMPPConnectionHandler.password);
		this.configBuilder.setResource(XMPPConnectionHandler.resource);
		this.configBuilder.setServiceName(XMPPConnectionHandler.service);
		this.configBuilder.setHost(XMPPConnectionHandler.host);
		this.configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		this.connection = new XMPPTCPConnection(this.configBuilder.build());
	}

	public void connect() {
		if (!XMPPConnectionHandler.connected) {
			final String fullChatName = XMPPConnectionHandler.chatRoomName + "@" + XMPPConnectionHandler.chatRoomPrefix
					+ "." + XMPPConnectionHandler.service;

			System.out.println("Attempting to contact XMPP Server");
			try {
				this.connection.connect();
				System.out.println("Connection Successful. Attempting to log in");
			} catch (final Exception e) {

			}

			try {
				this.connection.login();
				System.out.println("Login successful. Attemtping to join public chatroom " + fullChatName);
			} catch (final AlreadyLoggedInException e) {
				System.out.println("Already logged in. Attempting to join public chatroom " + fullChatName);
			} catch (final XMPPException e) {
				e.printStackTrace();
			} catch (final SmackException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}

			this.manager = MultiUserChatManager.getInstanceFor(this.connection);
			this.chatroom = this.manager.getMultiUserChat(fullChatName);
			this.listener = new MessageListener() {
				@Override
				public void processMessage(final Message message) {
					final String sender = message.getFrom().replace(fullChatName + "/", "");
					if (!sender.equals(XMPPConnectionHandler.username)) {
						System.out.println(sender + XMPPConnectionHandler.username);
						final String text = message.getBody();
						final String chatMessage = "<" + sender + "> " + text;
						final IChatComponent iChatMessage = new ChatComponentText(chatMessage);
						Minecraft.getMinecraft().thePlayer.addChatMessage(iChatMessage);
					}

				}
			};

			this.chatroom.addMessageListener(this.listener);
			try {
				this.chatroom.join(XMPPConnectionHandler.username);
			} catch (final NoResponseException e) {
				e.printStackTrace();
			} catch (final XMPPErrorException e) {
				e.printStackTrace();
			} catch (final NotConnectedException e) {
				e.printStackTrace();
			}
			XMPPConnectionHandler.connected = true;
			System.out.println("MineXMPP Finished Loading");
		}
	}

	public void disconnect() {
		if (XMPPConnectionHandler.connected) {
			try {
				this.chatroom.leave();
				this.chatroom.removeMessageListener(this.listener);
				final Presence offlinePres = new Presence(Presence.Type.unavailable, "", 1, Presence.Mode.away);
				this.connection.disconnect(offlinePres);
			} catch (final Exception e) {

			} finally {
				XMPPConnectionHandler.connected = false;
			}
		}

	}

	public void send(final String rawText) {
		final String sender = rawText.replaceFirst("<", "").replaceFirst(">* \\w*", "");
		if (sender.equals(Minecraft.getMinecraft().thePlayer.getDisplayName())) {
			final String message = rawText.replaceFirst("^<\\w*> ", "");
			try {
				this.chatroom.sendMessage(message);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String getUsername() {
		return XMPPConnectionHandler.username;
	}

}
