package net.sneak.discordTournamentBot.commands.tournament;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.mysql.cj.xdevapi.JsonArray;
import com.mysql.cj.xdevapi.JsonParser;
import com.mysql.cj.xdevapi.JsonValue;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.commands.ICommand;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.Query;
import net.sneak.discordTournamentBot.sql.Sql;
import net.sneak.discordTournamentBot.sql.queries.Insert;
import net.sneak.discordTournamentBot.sql.queries.Select;

public class StartTournament implements ICommand {
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
			e.getChannel().sendMessage(e.getMember().getAsMention() + " You do not have permission to run this command").queue();
			return null;
		}
		new Thread(() -> {
			try {
				ResultSet rs = new Select("Tournament", new String[] {"NoOfTeamsPerGroup"}, new Args[] {new Args("GuildID", Operations.EQUALS, e.getGuild().getIdLong())}).executeWithReturn();
				if(!rs.next()) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " The tournament has not been built yet!").queue();
					return;
				}
				int noOfTeamsPerGroup = rs.getInt(1);
				rs = new Select("Teams", new String[] {"SQLUUID", "`Group`"}, null).executeWithReturn();
				if(!rs.last()) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " There are no teams yet!").queue();
					return;
				}
				if(rs.getRow() < 2) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " There needs to be at least 2 teams!").queue();
					return;
				}
				
				this.assignGroups(noOfTeamsPerGroup, rs);
				this.setupGroupGames(e);
				
				rs = new Select("Tournament", new String[] {"GuildID", "HasStarted"}, new Args[] {new Args("GuildID", Operations.EQUALS, e.getGuild().getIdLong())}).executeWithReturn();
				rs.next();
				rs.updateBoolean(2, true);
				rs.updateRow();
				
				e.getChannel().sendMessage("Tournament has started!").queue();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}).start();
		return null;
	}
	
	private void assignGroups(int noOfTeamsPerGroup, ResultSet rs) throws SQLException {
		int noOfTeams = rs.getRow();
		List<Integer> teamNumbers = new ArrayList<Integer>();
		for(int i = 1; i <= noOfTeams; i++)
			teamNumbers.add(i);
		Collections.shuffle(teamNumbers);
		for(int i = 1; i <= noOfTeams / noOfTeamsPerGroup; i++) {
			new Insert("`Groups`", null, new Object[] {i}).execute();
			for(int j = (i - 1) * noOfTeamsPerGroup; j < ((i - 1) * noOfTeamsPerGroup) + noOfTeamsPerGroup; j++) {
				rs.beforeFirst();
				for(int c = 0; c < teamNumbers.get(j); c++)
					rs.next();
				rs.updateInt(2, i);
				rs.updateRow();
			}
		}
		int teamsLeft = noOfTeams % noOfTeamsPerGroup;
		if(teamsLeft != 0) {
			int group = (noOfTeams / noOfTeamsPerGroup) + 1;
			new Insert("`Groups`", null, new Object[] {group}).execute();
			for(int i = teamNumbers.size() - 1; i >= teamNumbers.size() - teamsLeft; i--) {
				rs.beforeFirst();
				for(int c = 0; c < teamNumbers.get(i); c++)
					rs.next();
				rs.updateInt(2, group);
				rs.updateRow();
			}
		}
	}
	
	private void setupGroupGames(GuildMessageReceivedEvent e) throws SQLException {
		class Game {
			private int a;
			private int b;
			private String gameMode;
			public Game(int a, int b, String gameMode) {
				this.a = a;
				this.b = b;
				this.gameMode = gameMode;
			}
		}
		ResultSet groups = new Select("`Groups`", null, null).executeWithReturn();
		ResultSet temp = new Select("Tournament", new String[] {"QualifyingGameModes"}, new Args[] {new Args("GuildID", Operations.EQUALS, e.getGuild().getIdLong())}).executeWithReturn();
		temp.next();
		JsonArray gameModes = (JsonArray) JsonParser.parseDoc(temp.getString(1)).get("modes");
		while(groups.next()) {
			List<Game> games = new LinkedList<Game>();
			ResultSet teams = new Select("Teams", null, new Args[] {new Args("`Group`", Operations.EQUALS, groups.getInt(1))}).executeWithReturn();
			for(JsonValue gameMode : gameModes) {
				teams.last();
				int noOfTeams = teams.getRow();
				teams.beforeFirst();
				int counter = 1;
				while(counter <= noOfTeams) {
					for(int i = 0; i < counter; i++)
						teams.next();
					int teamAId = teams.getInt(1);
					for(int i = counter + 1; i <= noOfTeams; i++) {
						teams.next();
						games.add(new Game(teamAId, teams.getInt(1), gameMode.toString()));
					}
					teams.beforeFirst();
					counter++;
				}
			}
			
			ResultSet dates = new Select("Dates", new String[] {"WeekInterval", "Day", "Hour", "Minute"}, null).executeWithReturn();
			dates.last();
			System.out.println(dates.getRow());
			dates.beforeFirst();
			LocalDateTime nowTime = LocalDateTime.now();
			Set<Integer> teamsFree = new HashSet<Integer>();
			teams.beforeFirst();
			int weekInterval = 0;
			//int dateCounter = 1;
			while(games.size() != 0) {
				System.out.println("ADDING");
				while(teams.next())
					teamsFree.add(teams.getInt(1));
				teams.beforeFirst();
				//System.out.println("DateRow before: " + dates.getRow());
				//System.out.println("Dates.next() : " + dates.next());
				//weekInterval = 0;
				//dates.previous();
				if(!dates.next()) {
					//System.out.println("incrementing");
					weekInterval++;
					dates.first();
				}
				System.out.println("dateRow: " + dates.getRow());
				Iterator<Game> gamesIt = games.iterator();
				while(gamesIt.hasNext()) {
					Game g = gamesIt.next();
					for(Iterator<Integer> it = teamsFree.iterator(); it.hasNext();)
						System.out.print(it.next());
					System.out.println();
					if(teamsFree.contains(g.a) && teamsFree.contains(g.b)) {
						int weekIntervalLambda = weekInterval;
						new Insert("Games", new String[] {"Team1", "Team2", "GameMode", "`Group`", "Date"}, new Object[] {
								g.a,
								g.b,
								g.gameMode,
								groups.getInt(1),
								Timestamp.valueOf(
										LocalDateTime.of(nowTime.getYear(), nowTime.getMonth(), nowTime.getDayOfMonth(), dates.getInt(3) + 1, dates.getInt(4))
										.with(TemporalAdjusters.next(DayOfWeek.values()[dates.getInt(2) - 1]))
										.with((t) -> {
											try {
												System.out.println("WeekInterval: " + (dates.getInt(1) * weekIntervalLambda * 7));
												return t.plus(Period.ofDays(dates.getInt(1) * weekIntervalLambda * 7));
											} catch (SQLException e1) {
												e1.printStackTrace();
											}
											return t;
										}))}).execute();
						teamsFree.removeIf(x -> x.equals(g.a) || x.equals(g.b));
						gamesIt.remove();
					}
				}
			}
		}
		ResultSet teamsDisplay = new Select("Teams", new String[] {"SQLUUID"}, null).executeWithReturn();
		while(teamsDisplay.next())
			displayNextGame(e.getGuild(), teamsDisplay.getInt(1));
	}
	
	public static void displayNextGame(Guild g, int teamId) {
		try {
			ResultSet gamesSql = new Query() {
				private ResultSet result;
				@Override
				public Query execute() throws SQLException {
					this.result = Sql.getInstance().getConnection()
							.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
							.executeQuery("SELECT * FROM ((Games LEFT JOIN Teams ON Games.Team1=Teams.SQLUUID) LEFT JOIN Teams AS Teams2 ON Games.Team2=Teams2.SQLUUID) WHERE (Games.Team1 = " + teamId + " OR Games.Team2 = " + teamId + ") AND Games.Played = false ORDER BY Date ASC LIMIT 1;");
					return this;
				}
				
				public ResultSet executeWithReturn() throws SQLException {
					this.execute();
					return this.result;
				}
			}.executeWithReturn();
			SimpleDateFormat time = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			time.setTimeZone(TimeZone.getTimeZone("GMT"));
			if(!gamesSql.next()) {
				ResultSet category = new Select("Teams", new String[] {"ChannelsID"}, new Args[] {new Args("SQLUUID", Operations.EQUALS, teamId)}).executeWithReturn();
				category.next();
				g.getCategoryById(category.getLong(1)).getTextChannels().get(0).sendMessage(new EmbedBuilder()
						.setColor(Color.YELLOW)
						.setTitle("Game update")
						.setDescription("All your games have been played!. Please wait until further notice")
						.build()
				).queue();
			}
			gamesSql.beforeFirst();
			while(gamesSql.next()) {
				boolean team1Or2 = gamesSql.getInt(2) == teamId;
				g.getCategoryById(gamesSql.getLong((team1Or2)? 15 : 22)).getTextChannels().get(0).sendMessage(new EmbedBuilder()
						.setColor(Color.YELLOW)
						.setDescription("**Your next game is detailed below**")
						.addField("Opposing team", gamesSql.getString((team1Or2)? 18 : 11), false)
						.addField("Date and Time", time.format(gamesSql.getTimestamp(8).getTime()), false)
						.addField("Game mode", gamesSql.getString(6).replace("\"", ""), false)
						.addField("From group", gamesSql.getInt(7) + "", false)
						.build()
				).queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void displayNextGame(Guild g, String teamName) {
		try {
			ResultSet rs = new Select("Teams", new String[] {"SQLUUID"}, new Args[] {new Args("Name", Operations.EQUALS, teamName)}).executeWithReturn();
			rs.next();
			int teamId = rs.getInt(1);
			rs.close();
			
			displayNextGame(g, teamId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
