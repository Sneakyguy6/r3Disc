package net.sneak.discordTournamentBot.commands;

import java.awt.Color;
import java.time.Instant;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Description implements ICommand {
	@Override
	public String execute(GuildMessageReceivedEvent e) {
		e.getChannel().sendMessage(new EmbedBuilder()
				.setTitle("Tournament manager")
				.setColor(Color.YELLOW)
				.setTimestamp(Instant.now())
				.setDescription("**WIP**")
				.setFooter("If you need any more help, contact the hosts.")
				.build()
		).queue();
		return null;
	}
}
