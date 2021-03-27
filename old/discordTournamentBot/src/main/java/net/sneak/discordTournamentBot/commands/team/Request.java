package net.sneak.discordTournamentBot.commands.team;

import static net.sneak.discordTournamentBot.sql.Args.Operations.EQUALS;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.commands.ICommand;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Insert;
import net.sneak.discordTournamentBot.sql.queries.Select;

public class Request implements ICommand {
	@Override
	public String execute(GuildMessageReceivedEvent e) {
		try {
			String parts[] = e.getMessage().getContentRaw().split(" ");
			if(parts.length < 4)
				return "Not enough arguments!";
			
			if(parts[3].toLowerCase().equals("cancel")) {
				if(parts.length < 5)
					return "Not enough arguments!";
				String teamName = "";
				for(int i = 3; i < parts.length; i++)
					teamName += parts[i] + " ";
				ResultSet rs = new Select("Teams", new String[] {"SQLUUID"}, new Args[] {new Args("Name", Operations.EQUALS, teamName)}).executeWithReturn();
				if(!rs.next())
					return "This team does not exist!";
				ResultSet cancel = new Select("PendingTeamRequests", new String[] {"SQLUUID"}, new Args[] {new Args("JoinOrInvite", EQUALS, true), new Args("Team", EQUALS, rs.getInt(1)), new Args("Player", EQUALS, e.getMember().getIdLong())}).executeWithReturn();
				if(!cancel.next())
					return "A request was never sent to this team";
				cancel.deleteRow();
				return "Your request has been cancelled.";
			}
			String teamName = "";
			for(int i = 3; i < parts.length; i++)
				teamName += parts[i] + " ";
			ResultSet rs = new Select("Teams", new String[] {"SQLUUID", "Captain", "ChannelsID"}, new Args[] {new Args("Name", Operations.EQUALS, teamName)}).executeWithReturn();
			if(!rs.next())
				return "This team does not exist!";
			ResultSet playerDetails = new Select("Players", null, new Args[] {new Args("IGUUID", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn();
			if(playerDetails.next()) {
				if(playerDetails.getInt(2) == rs.getInt(1))
					return "You are already in this team";
				if(new Select("Teams", new String[] {"SQLUUID"}, new Args[] {new Args("Captain", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn().next())
					return "The captain cannot leave the team";
			}
			//rs.previous();
			ResultSet isTeamFull = new Select("Tournament", new String[] {"NoOfPlayersPerTeam"}, new Args[] {new Args("GuildID", Operations.EQUALS, e.getGuild().getIdLong())}).executeWithReturn();
			isTeamFull.next();
			ResultSet playersInTeam = new Select("Players", new String[] {"IGUUID"}, new Args[] {new Args("Team", Operations.EQUALS, rs.getInt(1))}).executeWithReturn();
			playersInTeam.last();
			if(playersInTeam.getRow() == isTeamFull.getInt(1)) {
				//e.getChannel().sendMessage(e.getMember().getAsMention() + " This team is full!").queue();
				return "This team is full";
			}
			new Insert("Players", new String[] {"IGUUID", "UserTag"}, new Object[] {e.getMember().getIdLong(), e.getMember().getUser().getAsTag()}).onError((insert) -> {}).execute(); //in case the player already exists
			if(!new Select("PendingTeamRequests", new String[] {"SQLUUID"}, new Args[] {new Args("JoinOrInvite", EQUALS, true), new Args("Team", EQUALS, rs.getInt(1)), new Args("Player", EQUALS, e.getMember().getIdLong())}).executeWithReturn().next())
				new Insert("PendingTeamRequests", new String[] {"JoinOrInvite", "Team", "Player"}, new Object[] {true, rs.getInt(1), e.getMember().getIdLong()}).execute();
			else
				return "You have already sent a request to this team";
			e.getChannel().sendMessage(e.getMember().getAsMention() + " Invite sent to " + e.getGuild().getMemberById(rs.getLong(2)).getEffectiveName() + ". You can do *team request cancel <team>* to cancel").queue();
			e.getGuild().getCategoryById(rs.getLong(3)).getTextChannels().get(0).sendMessage("@here " + e.getMember().getEffectiveName() + " would like to join your team! to accept do mention me and say *team accept <player name>*").queue();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
