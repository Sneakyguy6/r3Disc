package net.sneak.discordTournamentBot.commands.tournament;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.cj.xdevapi.DbDoc;
import com.mysql.cj.xdevapi.JsonArray;
import com.mysql.cj.xdevapi.JsonParser;
import com.mysql.cj.xdevapi.JsonString;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.commands.ICommand;
import net.sneak.discordTournamentBot.listener.Listener;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Insert;
import net.sneak.discordTournamentBot.sql.queries.Select;
import net.sneak.discordTournamentBot.sql.queries.Update;

public class BuildTournament implements ICommand {
	private List<Message> messagesToBeDeleted;
	private long modifier;
	
	public BuildTournament() {
		this.modifier = -1L;
		this.messagesToBeDeleted = new ArrayList<Message>();
	}
	@Override
	public String execute(GuildMessageReceivedEvent e) {
		boolean hasPermission = false;
		for(Role r : e.getMember().getRoles()) {
			if(r.getIdLong() == 706911282556829726L) {
				hasPermission = true;
				break;
			}
		}
		if(!hasPermission) {
			e.getChannel().sendMessage(e.getMember().getAsMention() + " You do not have permission to run this command").queue((m) -> this.messagesToBeDeleted.add(m));
			return null;
		}
		try {
			if(new Select("Tournament", new String[] {"GuildID"}, new Args[] {new Args("GuildID", Operations.EQUALS, e.getGuild().getIdLong())}).executeWithReturn().next()) {
				e.getChannel().sendMessage(e.getMember().getAsMention() + " A tournament already exists").queue((m) -> this.messagesToBeDeleted.add(m));
				return null;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			return null;
		}
		if(this.modifier != -1) {
			e.getChannel().sendMessage(e.getMember().getAsMention() + " Someone is already building a tournament").queue((m) -> this.messagesToBeDeleted.add(m));
			return null;
		}
		new Thread(() -> {
			try {
				this.modifier = e.getMember().getIdLong();
				new Insert("Tournament", new String[] {"GuildID", "QualifyingGameModes", "KnockoutGameModes"}, new Object[] {e.getGuild().getIdLong(), "{}", "{}"}).execute();
				e.getChannel().sendMessage(e.getMember().getAsMention() + " Input the details of the tournament and then type build to build it. Start every command with !(command)").queue((m) -> this.messagesToBeDeleted.add(m));
				Listener.getInstance().addCustomCallback((event) -> {
					String[] parts = event.getMessage().getContentRaw().split(" ");
					if(parts[1].toCharArray()[0] != '!' || event.getMember().getIdLong() != this.modifier)
						return new boolean[] {false, false};
					this.messagesToBeDeleted.add(event.getMessage());
					switch(parts[1])
					{
					case "!groupSize":
						this.groupSize(event, parts);
						break;
					case "!build":
						return new boolean[] {true, this.build(event, parts)};
					case "!teamSize":
						this.teamSize(event, parts);
						break;
					case "!qualifyingGameModes":
						this.qualifyingGameModes(event, parts);
						break;
					case "!knockoutGameModes":
						this.knockoutGameModes(event, parts);
						break;
					case "!addDate":
						this.addDate(event, parts);
						break;
					default:
						event.getChannel().sendMessage(event.getMember().getAsMention() + " Unrecognised tournament build command.").queue((m) -> this.messagesToBeDeleted.add(m));
					}
					return new boolean[] {true, false};
				}, true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start();
		return null;
	}
	
	private void groupSize(GuildMessageReceivedEvent event, String[] parts) {
		if(parts.length < 3) {
			event.getChannel().sendMessage(event.getMember().getAsMention() + " Not enough arguments.").queue((m) -> this.messagesToBeDeleted.add(m));
			return;
		}
		try {
			new Update("Tournament", new Args[] {new Args("NoOfTeamsPerGroup", Operations.EQUALS, Integer.parseInt(parts[2]))}, new Args[] {new Args("GuildID", Operations.EQUALS, event.getGuild().getIdLong())}).execute();
		} catch (NumberFormatException ex) {
			event.getChannel().sendMessage(event.getMember().getAsMention() + " Your second argument needs to be a number.").queue((m) -> this.messagesToBeDeleted.add(m));
			return;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return;
		}
		event.getChannel().sendMessage(event.getMember().getAsMention() + " Done!").queue((m) -> this.messagesToBeDeleted.add(m));
	}
	
	private boolean build(GuildMessageReceivedEvent event, String[] parts) {
		try {
			ResultSet rs = new Select("Tournament", new String[] {"QualifyingGameModes", "KnockoutGameModes"}, new Args[] {new Args("GuildID", Operations.EQUALS, event.getGuild().getIdLong())}).executeWithReturn();
			rs.next();
			for(int i = 1; i <= 2; i++) {
				DbDoc json = JsonParser.parseDoc(rs.getString(i));
				if(!json.containsKey("modes")) {
					event.getChannel().sendMessage(event.getMember().getAsMention() + " You did not specify any " + ((i == 1)? "qualifying":"knockout") + " game modes.").queue((m) -> this.messagesToBeDeleted.add(m));
					return false;
				}
			}
			if(!new Select("Dates", new String[] {"Day"}, new Args[] {}).executeWithReturn().next()) {
				event.getChannel().sendMessage(event.getMember().getAsMention() + " You need to add at least 1 date!").queue((m) -> this.messagesToBeDeleted.add(m));
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
		}
		event.getChannel().sendMessage(event.getMember().getAsMention() + " Building...").queue((m) -> this.messagesToBeDeleted.add(m));
		this.messagesToBeDeleted.forEach((message) -> {
			message.delete().queue();
		});
		this.messagesToBeDeleted.clear();
		event.getChannel().sendMessage(event.getMember().getAsMention() + " Built! When ready, do startTournament to start it").queue((m) -> this.messagesToBeDeleted.add(m));
		this.modifier = -1L;
		try {
			System.out.println("Creating tournament information...");
			ResultSet tournament = new Select("Tournament", null, null).executeWithReturn();
			ResultSet rs = new Select("Channels", new String[] {"ID"}, new Args[] {new Args("Name", Operations.EQUALS, "Information")}).executeWithReturn();
			System.out.println("Fetched data.");
			rs.next();
			tournament.next();
			event.getGuild().getTextChannelById(rs.getLong(1)).getHistory().getRetrievedHistory().forEach((m) -> m.delete().queue());
			StringBuilder qualifyingGameModes = new StringBuilder();
			((JsonArray) JsonParser.parseDoc(tournament.getString(4)).get("modes")).forEach((i) -> qualifyingGameModes.append(i + ", "));
			for(int i = 0; i < 2; i++)
				qualifyingGameModes.deleteCharAt(qualifyingGameModes.length() - 1);
			String qualifyingGameModesFinal = qualifyingGameModes.toString().replace("\"", "");
			System.out.println("Setup qualifying game modes.");
			StringBuilder knockoutGameModes = new StringBuilder();
			((JsonArray) JsonParser.parseDoc(tournament.getString(5)).get("modes")).forEach((i) -> knockoutGameModes.append(i + ", "));
			for(int i = 0; i < 2; i++)
				knockoutGameModes.deleteCharAt(knockoutGameModes.length() - 1);
			String knockoutGameModesFinal = knockoutGameModes.toString().replace("\"", "");
			System.out.println("Setup knockout game modes.");
			ResultSet dates = new Select("Dates", new String[] {"WeekInterval", "Day", "Hour", "Minute"}, null).executeWithReturn();
			String datesString = "";
			while(dates.next()) {
				datesString += "Every " + ((dates.getInt(1) == 1)? "week":dates.getInt(1) + " weeks")
						+ " on " + Day.values()[dates.getInt(2) - 1]
						+ " at " + String.format("%02d", dates.getInt(3))
						+ ":" + String.format("%02d", dates.getInt(4))
						+ " *GMT*\n";
			}
			System.out.println("Setup dates.");
			//event.getChannel().sendMessage(event.getMember().getAsMention() + " updating information...").queue((m) -> this.messagesToBeDeleted.add(m));
			event.getGuild().getTextChannelById(rs.getLong(1)).sendMessage(new EmbedBuilder()
					.setTitle("Details")
					.setColor(Color.GREEN)
					.setDescription("The teams will be placed into groups at random. "
							+ "Each team will play all other teams in that group. "
							+ "The one with the most wins will qualify to the knockout stage. "
							+ "During the knockout stage, if you lose, you will play no more matches "
							+ "Each match will need to be recorded to verify the scores. These recordings will need to be uploaded to youtube and a youtube link sent to the bot using footage [link]. "
							+ "**The details of the tournament are below**")
					.addField("Number of players per team", tournament.getString(3), true)
					.addField("Number of teams per group", tournament.getString(2), true)
					.addField("Game modes in qualifying rounds", qualifyingGameModesFinal, true)
					.addField("Game modes in knockout rounds", knockoutGameModesFinal, true)
					.addField("Dates", datesString, false)
					.build()
			).queue();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private void teamSize(GuildMessageReceivedEvent event, String[] parts) {
		if(parts.length < 3) {
			event.getChannel().sendMessage(event.getMember().getAsMention() + " Not enough arguments!").queue((m) -> this.messagesToBeDeleted.add(m));
			return;
		}
		try {
			new Update("Tournament", new Args[] {new Args("NoOfPlayersPerTeam", Operations.EQUALS, Integer.parseInt(parts[2]))}, new Args[] {new Args("GuildID", Operations.EQUALS, event.getGuild().getIdLong())}).execute();
		} catch (NumberFormatException ex) {
			event.getChannel().sendMessage(event.getMember().getAsMention() + " Your second argument needs to be a number.").queue((m) -> this.messagesToBeDeleted.add(m));
			return;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return;
		}
		event.getChannel().sendMessage(event.getMember().getAsMention() + " Done!").queue((m) -> this.messagesToBeDeleted.add(m));
	}
	
	private void qualifyingGameModes(GuildMessageReceivedEvent event, String[] parts) {
		if(parts.length < 3) {
			event.getChannel().sendMessage(event.getMember().getAsMention() + " Game modes must be specified!").queue((m) -> this.messagesToBeDeleted.add(m));
			return;
		}
		JsonArray array = new JsonArray();
		for(int i = 2; i < parts.length; i++)
			array.add(new JsonString().setValue(parts[i]));
		try {
			new Update("Tournament", new Args[] {new Args("QualifyingGameModes", Operations.EQUALS, "{" + "\"modes\":" + array.toString() + "}")}, new Args[] {new Args("GuildID", Operations.EQUALS, event.getGuild().getIdLong())}).execute();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		event.getChannel().sendMessage(event.getMember().getAsMention() + " Done!").queue((m) -> this.messagesToBeDeleted.add(m));
	}
	
	private void knockoutGameModes(GuildMessageReceivedEvent event, String[] parts) {
		if(parts.length < 3) {
			event.getChannel().sendMessage(event.getMember().getAsMention() + " Game modes must be specified!").queue((m) -> this.messagesToBeDeleted.add(m));
			return;
		}
		JsonArray array = new JsonArray();
		for(int i = 2; i < parts.length; i++)
			array.add(new JsonString().setValue(parts[i]));
		try {
			new Update("Tournament", new Args[] {new Args("KnockoutGameModes", Operations.EQUALS, "{" + "\"modes\":" + array.toString() + "}")}, new Args[] {new Args("GuildID", Operations.EQUALS, event.getGuild().getIdLong())}).execute();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		event.getChannel().sendMessage(event.getMember().getAsMention() + " Done!").queue((m) -> this.messagesToBeDeleted.add(m));
	}
	
	private void addDate(GuildMessageReceivedEvent event, String[] parts) { //interval (weeks), day, hour, minute
		if(parts.length < 6) {
			event.getChannel().sendMessage(event.getMember().getAsMention() + " Not enough arguments!").queue((m) -> this.messagesToBeDeleted.add(m));
			return;
		}
		try {
			int interval = Integer.parseInt(parts[2]);
			int day = Integer.parseInt(parts[3]);
			if(day < 1 || day > 7) {
				event.getChannel().sendMessage(event.getMember().getAsMention() + " Day must be between 1 and 7");
				return;
			}
			int hour = Integer.parseInt(parts[4]);
			if(hour < 1 || hour > 24) {
				event.getChannel().sendMessage(event.getMember().getAsMention() + " Hour must be between 1 and 24");
				return;
			}
			int minute = Integer.parseInt(parts[5]);
			if(minute < 0 || minute > 59) {
				event.getChannel().sendMessage(event.getMember().getAsMention() + " Minute must be between 0 and 59");
				return;
			}
			new Insert("Dates", new String[] {"WeekInterval", "Day", "Hour", "Minute", "TournamentInstance"}, new Object[] {interval, day, hour, minute, event.getGuild().getIdLong()}).execute();
			event.getChannel().sendMessage(event.getMember().getAsMention() + " Done!").queue((m) -> this.messagesToBeDeleted.add(m));
		} catch (NumberFormatException e) {
			event.getChannel().sendMessage(event.getMember().getAsMention() + " Arguments can only be numbers");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
enum Day {
	Monday,
	Tuesday,
	Wednesday,
	Thursday,
	Friday,
	Saturday,
	Sunday;
}
