package net.sneak.discordTournamentBot.commands.team;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.commands.ICommand;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Select;

public class Join implements ICommand {
	@Override
	public String execute(GuildMessageReceivedEvent e) {
		String[] parts = e.getMessage().getContentRaw().split(" ");
		if(parts.length < 4)
			return "Not enough arguments!";
		new Thread(() -> { 
			try {
				String teamName = "";
				for(int i = 3; i < parts.length; i++)
					teamName += parts[i] + " ";
				teamName = new StringBuilder(teamName).deleteCharAt(teamName.length() - 1).toString();
				
				ResultSet team = new Select("Teams", new String[] {"SQLUUID", "Name", "RoleID"}, new Args[] {new Args("Name", Operations.EQUALS, teamName)}).executeWithReturn();
				if(!team.next()) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " This team does not exist!").queue();
					return;
				}
				ResultSet request = new Select("PendingTeamRequests", new String[] {"SQLUUID", "Player"}, new Args[] {
						new Args("Player", Operations.EQUALS, e.getMember().getIdLong()),
						new Args("Team", Operations.EQUALS, team.getInt(1)),
						new Args("JoinOrInvite", Operations.EQUALS, false)
				}).executeWithReturn();
				if(!request.next()) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " You were not invited to this team").queue();
					return;
				}
				request.deleteRow();
				/*if(new Select("Teams", new String[] {"SQLUUID"}, new Args[] {new Args("Captain", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn().next()) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " The captain cannot leave the team").queue();
					return;
				}*/
				ResultSet playerSql = new Select("Players", new String[] {"Team"}, new Args[] {new Args("IGUUID", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn();
				if(playerSql.next()) {
					playerSql.getInt(1);
					if(!playerSql.wasNull()) {
						e.getChannel().sendMessage(e.getMember().getAsMention() + " You need to leave the current team to join another one.").queue();
						return;
					}
				}
				ResultSet isTeamFull = new Select("Tournament", new String[] {"NoOfPlayersPerTeam"}, new Args[] {new Args("GuildID", Operations.EQUALS, e.getGuild().getIdLong())}).executeWithReturn();
				isTeamFull.next();
				ResultSet playersInTeam = new Select("Players", new String[] {"IGUUID"}, new Args[] {new Args("Team", Operations.EQUALS, team.getInt(1))}).executeWithReturn();
				playersInTeam.last();
				if(playersInTeam.getRow() == isTeamFull.getInt(1)) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " This team is now full and can no longer be joined.").queue();
					return;
				}
				ResultSet player = new Select("Players", new String[] {"IGUUID", "Team"}, new Args[] {new Args("IGUUID", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn();
				player.next();
				player.updateInt(2, team.getInt(1));
				player.updateRow();
				e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(team.getLong(3))).queue();
				e.getChannel().sendMessage(e.getMember().getAsMention() + " You have joined **" + team.getString(2) + "**").queue();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}).start();
		return null;
	}
}
