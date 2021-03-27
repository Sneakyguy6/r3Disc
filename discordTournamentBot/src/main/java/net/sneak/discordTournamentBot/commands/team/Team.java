package net.sneak.discordTournamentBot.commands.team;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.commands.ICommand;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Select;

public class Team implements ICommand {
	private NewTeam newTeam;
	private DisbandTeam disbandTeam;
	private Request request;
	private Accept accept;
	private Leave leave;
	private Invite invite;
	private Join join;
	
	public Team() {
		this.newTeam = new NewTeam();
		this.disbandTeam = new DisbandTeam();
		this.request = new Request();
		this.accept = new Accept();
		this.leave = new Leave();
		this.invite = new Invite();
		this.join = new Join();
	}
	@Override
	public String execute(GuildMessageReceivedEvent e) {
		try {
			ResultSet temp = new Select("Tournament", new String[] {"GuildID", "HasStarted"}, new Args[] {new Args("GuildID", Operations.EQUALS, e.getGuild().getIdLong())}).executeWithReturn();
			if(!temp.next())
				return "The tournament is not ready yet";
			if(temp.getBoolean(2))
				return "The tournament has already started and teams cannot be edited";
		} catch (SQLException e1) {
			e1.printStackTrace();
			return null;
		}
		String[] parts = e.getMessage().getContentRaw().split(" ");
		if(parts.length < 3)
			return "Not enough arguments. Do team help for help"; 
		switch(parts[2].toLowerCase())
		{
		case "new":
			return this.newTeam.execute(e);
		case "disband":
			return this.disbandTeam.execute(e);
		case "help":
			return this.teamHelp(e);
		case "request":
			return this.request.execute(e);
		case "accept":
			return this.accept.execute(e);
		case "join":
			return this.join.execute(e);
		case "invite":
			return this.invite.execute(e);
		case "leave":
			return this.leave.execute(e);
		default:
			return "unrecognised team command. Do 'team help' to get team commands";
		}
	}
	
	private String teamHelp(GuildMessageReceivedEvent e) {
		e.getChannel().sendMessage(new EmbedBuilder()
				.setTitle("Tournament team management")
				.setColor(Color.RED)
				.setTimestamp(Instant.now())
				.setDescription("**new**:\tcreates a new team where you are the captain *syntax: new <name of team>*"
							  + "\n**disband**:\tdisbands the team (can only be done by captains) *syntax: disband*"
							  + "\n**request**:\tsends a request to the team you want to join *syntax: request <team name>*"
							  + "\n**invite**:\tsends a request to the player you want to join your team (can only be done by captains) *syntax: invite <player>*"
							  + "\n**join**:\taccepts an invite to a specified team *syntax: join <teamName>*"
							  + "\n**accept**:\taccepts a request from a player to join your team (can only be done by captains) *syntax: accept <player>*"
							  + "\n**leave**:\tleave the team *syntax: leave*")
				.setFooter("If you need any more help, contact the hosts.")
				.build()
		).queue();
		return null;
	}
}
