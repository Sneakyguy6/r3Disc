package net.sneak.r3.commands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandHelp extends Command {
	
	public CommandHelp() {
		super("help");
	}

	@Override
	public String description() {
		return "Gives you a list of all the commands as well as their descriptions";
	}

	@Override
	public boolean run(GuildMessageReceivedEvent e) {
		EmbedBuilder b = new EmbedBuilder();
		b.setDescription("WIP");
		b.setColor(Color.GREEN);
		e.getChannel().sendMessage(b.build()).queue();
		return true;
	}
}
