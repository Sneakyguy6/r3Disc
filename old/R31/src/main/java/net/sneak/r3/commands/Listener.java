package net.sneak.r3.commands;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.reflections.Reflections;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {
	private Map<String, Command> commands;
	
	private Listener() {
		this.commands = new HashMap<String, Command>();
	}
	
	public void initCommands() throws InstantiationException, IllegalAccessException {
		Reflections r = new Reflections("net.sneak.r3");
		for(Class<? extends Object> i : r.getSubTypesOf(Command.class))
			i.newInstance();
		System.out.println("COMMANDS: ");
		this.commands.forEach((k, v) -> System.out.println(k + ": " + v));
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		String text = e.getMessage().getContentRaw();
		try {
			if(!text.substring(0, 3).equalsIgnoreCase("!R3"))
				return;
		} catch (StringIndexOutOfBoundsException ex) {
			return;
		}
		System.out.println("COMMAND: " + text);
		String[] parts = e.getMessage().getContentRaw().split(" ");
		if(parts.length < 2) {
			e.getChannel().sendMessage(new EmbedBuilder()
					.setDescription("Not enough arguments")
					.setColor(Color.RED)
					.build()).queue();
			return;
		}
		
		try {
			if(!this.commands.get(parts[1]).run(e))
				e.getChannel().sendMessage("Something went wrong or this command should not be run in the client. Check help or ask the administrators.").queue();
		} catch (NullPointerException ex) {
			e.getChannel().sendMessage(new EmbedBuilder()
					.setDescription("Unrecognised command. Use '!R3 help' to get the list of commands and command descriptions.")
					.setColor(Color.RED)
					.build()).queue();
		}
	}
	
	public void registerCommand(String name, Command exec) {
		this.commands.put(name.toLowerCase(), exec);
	}
	
	public Map<String, Command> getCommands() {
		return this.commands;
	}
	
	private static Listener instance;
	
	public static void init() {
		if(instance == null)
			instance = new Listener();
	}
	
	public static Listener getInstance() {
		return instance;
	}
}
