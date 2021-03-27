package net.sneak.r3.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public abstract class Command {
	private Set<TextChannel> specificChannels;
	
	protected Command(String name) {
		Listener.getInstance().registerCommand(name, this);
	}
	
	protected Command(String name, TextChannel... specificChannels) {
		this.specificChannels = new HashSet<TextChannel>();
		this.specificChannels.addAll(Arrays.asList(specificChannels));
	}
	
	public String description() {
		return null;
	}
	
	public final boolean handleCommandFromClient(GuildMessageReceivedEvent e) {
		if(this.specificChannels != null) {
			if(this.specificChannels.contains(e.getChannel()))
				return this.run(e);
			else
				return true;
		} else
			return this.run(e);
	}
	
	public boolean run(GuildMessageReceivedEvent e) {
		return false;
	}
	
	public String terminalRun(String command) {
		return "This command does not support execution from the terminal";
	}
}
