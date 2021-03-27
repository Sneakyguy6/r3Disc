package net.sneak.discordTournamentBot.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface ICommand {
	/**
	 * executes the command
	 * @return a return message
	 */
	public String execute(GuildMessageReceivedEvent e);
}
