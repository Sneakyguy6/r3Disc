package net.sneak.discordTournamentBot.listener;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@FunctionalInterface
public interface CustomCallback {
	boolean[] run(GuildMessageReceivedEvent e);
}
