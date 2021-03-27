package net.sneak.discordTournamentBot.commands.team;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.commands.ICommand;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Select;
import net.sneak.discordTournamentBot.sql.queries.Update;

public class Accept implements ICommand {
	@Override
	public String execute(GuildMessageReceivedEvent e) {
		String[] parts = e.getMessage().getContentRaw().split(" ");
		if(parts.length < 4)
			return "Not enought arguments!";
		try {
			ResultSet isTeamFull = new Select("Tournament", new String[] {"NoOfPlayersPerTeam"}, new Args[] {new Args("GuildID", Operations.EQUALS, e.getGuild().getIdLong())}).executeWithReturn();
			isTeamFull.next();
			ResultSet team = new Select("Teams", new String[] {"SQLUUID"}, new Args[] {new Args("Captain", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn();
			if(!team.next())
				return "You are not a captain";
			ResultSet playersInTeam = new Select("Players", new String[] {"IGUUID"}, new Args[] {new Args("Team", Operations.EQUALS, team.getInt(1))}).executeWithReturn();
			playersInTeam.last();
			if(playersInTeam.getRow() == isTeamFull.getInt(1)) {
				//e.getChannel().sendMessage(e.getMember().getAsMention() + " This team is full!").queue();
				return "This team is full";
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		String playerName = "";
		for(int i = 3; i < parts.length; i++)
			playerName += parts[i] + " ";
		playerName = new StringBuilder(playerName).deleteCharAt(playerName.length() - 1).toString();
		System.out.println("ACCEPT playerName: " + playerName);
		System.out.println("ACCEPT: checking tag");
		Member player;
		try {
			player = e.getGuild().getMemberByTag(playerName);
			System.out.println("ACCEPT: checked tag");
		} catch(IllegalArgumentException e1) {
			System.out.println("ACCEPT: no tag");
			List<Member> playersWithName = e.getGuild().getMembersByEffectiveName(playerName, false);
			if(playersWithName.size() > 1)
				return "There is more than 1 person with that name, please use the tag instead (e.g. host#3412)";
			else if(playersWithName.size() == 0) 
				return "There is no member with that name";
			else
				player = playersWithName.get(0);
		}
		System.out.println("ACCEPT: found player");
		try {
			ResultSet captainsTeam = new Select("Teams", new String[] {"SQLUUID", "RoleID"}, new Args[] {new Args("Captain", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn();
			if(!captainsTeam.next())
				return "You are not a captain.";
			ResultSet request = new Select("PendingTeamRequests",
					new String[] {"SQLUUID", "Player"},
					new Args[] {
							new Args("JoinOrInvite", Operations.EQUALS, true),
							new Args("Team", Operations.EQUALS, captainsTeam.getInt(1)),
							new Args("Player", Operations.EQUALS, player.getIdLong())}).executeWithReturn();
			if(!request.next())
				return "This player never requested to join your team.";
			new Update("Players", new Args[] {new Args("Team", Operations.EQUALS, captainsTeam.getInt(1))}, new Args[] {new Args("IGUUID", Operations.EQUALS, request.getLong(2))}).execute();
			request.deleteRow();
			e.getGuild().addRoleToMember(player.getIdLong(), e.getGuild().getRoleById(captainsTeam.getLong(2))).queue();
			return player.getEffectiveName() + " is now part of the team.";
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return null;
	}
}
