package net.sneak.discordTournamentBot.commands.team;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.commands.ICommand;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Insert;
import net.sneak.discordTournamentBot.sql.queries.Select;

public class Invite implements ICommand {
	@Override
	public String execute(GuildMessageReceivedEvent e) {
		String[] parts = e.getMessage().getContentRaw().split(" ");
		if(parts.length < 4)
			return "Not enough arguments";
		new Thread(() -> {
			try {
				ResultSet rs = new Select("Teams", new String[] {"SQLUUID", "Name"}, new Args[] {new Args("Captain", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn();
				if(!rs.next()) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " You must be a captain to do this").queue();
					return;
				}
				ResultSet isTeamFull = new Select("Tournament", new String[] {"NoOfPlayersPerTeam"}, new Args[] {new Args("GuildID", Operations.EQUALS, e.getGuild().getIdLong())}).executeWithReturn();
				isTeamFull.next();
				ResultSet playersInTeam = new Select("Players", new String[] {"IGUUID"}, new Args[] {new Args("Team", Operations.EQUALS, rs.getInt(1))}).executeWithReturn();
				playersInTeam.last();
				if(playersInTeam.getRow() == isTeamFull.getInt(1)) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " This team is full!").queue();
					return;
				}
				playersInTeam.close();
				isTeamFull.close();
				
				Member player;
				String playerName = "";
				for(int i = 3; i < parts.length; i++)
					playerName += parts[i] + " ";
				playerName = new StringBuilder(playerName).deleteCharAt(playerName.length() - 1).toString();
				try {
					player = e.getGuild().getMemberByTag(playerName);
					System.out.println("ACCEPT: checked tag");
				} catch(IllegalArgumentException e1) {
					System.out.println("ACCEPT: no tag");
					List<Member> playersWithName = e.getGuild().getMembersByEffectiveName(playerName, false);
					if(playersWithName.size() > 1) {
						e.getChannel().sendMessage(e.getMember().getAsMention() + " There is more than 1 person with that name, please use the tag instead (e.g. host#3412)").queue();
						return;
					}
					else if(playersWithName.size() == 0) {
						e.getChannel().sendMessage(e.getMember().getAsMention() + "There is no member with that name").queue();
						return;
					}
					else
						player = playersWithName.get(0);
				}
				
				ResultSet playerSql = new Select("Players", new String[] {"Team"}, new Args[] {new Args("IGUUID", Operations.EQUALS, player.getIdLong())}).executeWithReturn();
				if(!playerSql.next())
					new Insert("Players", new String[] {"IGUUID", "UserTag"}, new Object[] {player.getIdLong(), player.getUser().getAsTag()}).execute();
				else {
					playerSql.getInt(1);
					if(!playerSql.wasNull()) {
						e.getChannel().sendMessage(e.getMember().getAsMention() + " This player is already in a team.").queue();
						return;
					}
				}
				
				if(!new Select("PendingTeamRequests", new String[] {"SQLUUID"}, new Args[] {new Args("JoinOrInvite", Operations.EQUALS, false), new Args("Team", Operations.EQUALS, rs.getInt(1)), new Args("Player", Operations.EQUALS, player.getIdLong())}).executeWithReturn().next())
					new Insert("PendingTeamRequests", new String[] {"JoinOrInvite", "Team", "Player"}, new Object[] {false, rs.getInt(1), player.getIdLong()}).execute();
				else {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " You have already sent a request to this player").queue();
					return;
				}
				player.getUser().openPrivateChannel().complete().sendMessage("You have been invited by **" + e.getMember().getEffectiveName() + "** to join **" + rs.getString(2) + "**").queue();
				e.getChannel().sendMessage(e.getMember().getAsMention() + " Invite sent!").queue();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}).start();
		return null;
	}
}
