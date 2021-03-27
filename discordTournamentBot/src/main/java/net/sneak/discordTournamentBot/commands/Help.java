package net.sneak.discordTournamentBot.commands;

import java.awt.Color;
import java.time.Instant;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Help implements ICommand {

	@Override
	public String execute(GuildMessageReceivedEvent e) {
		String out = "";
		for(Commands i : Commands.values())
			if(i.getDescription() != null)
				out += "**" + i.getAlias() + "**:    " + i.getDescription() + "\n";
		e.getChannel().sendMessage(new EmbedBuilder()
				.setTitle("Tournament manager")
				.setColor(Color.RED)
				.setTimestamp(Instant.now())
				.setDescription(out)
				.setFooter("If you need any more help, contact the hosts.")
				.build()
		).queue();
		return null;
	}
}
