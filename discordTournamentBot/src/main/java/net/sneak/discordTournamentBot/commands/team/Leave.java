package net.sneak.discordTournamentBot.commands.team;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.commands.ICommand;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Select;

public class Leave implements ICommand {
	@Override
	public String execute(GuildMessageReceivedEvent e) {
		new Thread(() -> {
			try {
				ResultSet rs = new Select("Players", new String[] {"IGUUID", "Team"}, new Args[] {new Args("IGUUID", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn();
				if(!rs.next()) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " You are not in a team.").queue();
					return;
				}
				if(rs.getInt(2) == 0) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " You are not in a team.").queue();
					return;
				}
				ResultSet team = new Select("Teams", new String[] {"Captain", "RoleID"}, new Args[] {new Args("SQLUUID", Operations.EQUALS, rs.getInt(2))}).executeWithReturn();
				team.next();
				if(e.getMember().getIdLong() == team.getLong(1)) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " The captain cannot leave the team").queue();
					return;
				}
				rs.updateNull(2);
				rs.updateRow();
				e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(team.getLong(2))).queue();
				e.getChannel().sendMessage(e.getMember().getAsMention() + " You have left the team").queue();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}).start();
		return null;
	}
}
