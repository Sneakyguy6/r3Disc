package net.sneak.discordTournamentBot.commands.team;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.commands.ICommand;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Delete;
import net.sneak.discordTournamentBot.sql.queries.Select;

public class DisbandTeam implements ICommand {
	@Override
	public String execute(GuildMessageReceivedEvent e) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ResultSet rs = new Select("Teams", new String[] {"RoleID", "ChannelsID"}, new Args[] {new Args("Captain", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn();
					if(!rs.next()) {
						e.getChannel().sendMessage(e.getMember().getAsMention() + " You are not a captain!").queue();
						return;
					}
					Role r = e.getGuild().getRoleById(rs.getLong(1));
					Category c = e.getGuild().getCategoryById(rs.getLong(2));
					c.getChannels().forEach((l) -> {
						l.delete().queue();
					});
					c.delete().queue();
					r.delete().queue();
					new Delete("Teams", new Args[] {new Args("Captain", Operations.EQUALS, Long.toString(e.getMember().getIdLong()))}).execute();
					e.getChannel().sendMessage(e.getMember().getAsMention() + " Your team has been disbanded!").queue();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}).start();
		return null; //handled here
	}
}
