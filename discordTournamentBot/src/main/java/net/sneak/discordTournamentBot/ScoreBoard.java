package net.sneak.discordTournamentBot;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.Query;
import net.sneak.discordTournamentBot.sql.Sql;
import net.sneak.discordTournamentBot.sql.queries.Select;
import net.sneak.discordTournamentBot.sql.queries.Update;

public class ScoreBoard {
	private static ScoreBoard instance;
	
	public static void init() {
		if(instance == null)
			instance = new ScoreBoard();
	}
	
	public static ScoreBoard getInstance() {
		return instance;
	}
	
	private ScoreBoard() {
		
	}
	
	public void win(int team1, int team2) {
		try {
			ResultSet rs = new Query() {
				private ResultSet result;
				
				@Override
				public Query execute() throws SQLException {
					this.result = Sql.getInstance().getConnection()
							.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
							.executeQuery("Select Points from Teams where SQLUUID = " + team1 + ";");
					return this;
				}
				
				public ResultSet executeWithReturn() throws SQLException {
					this.execute();
					return this.result;
				}
			}.executeWithReturn();
			rs.next();
			new Update("Teams", new Args[] {new Args("Points", Operations.EQUALS, rs.getInt(1) + 3)}, new Args[] {new Args("SQLUUID", Operations.EQUALS, team1)}).execute();
			rs = new Query() {
				private ResultSet result;
				
				@Override
				public Query execute() throws SQLException {
					this.result = Sql.getInstance().getConnection()
							.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
							.executeQuery("Select Points from Teams where SQLUUID = " + team2 + ";");
					return this;
				}
				
				public ResultSet executeWithReturn() throws SQLException {
					this.execute();
					return this.result;
				}
			}.executeWithReturn();
			rs.next();
			new Update("Teams", new Args[] {new Args("Points", Operations.EQUALS, rs.getInt(1) + 1)}, new Args[] {new Args("SQLUUID", Operations.EQUALS, team2)}).execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void draw(int team1, int team2) {
		try {
			ResultSet rs = new Query() {
				private ResultSet result;
				
				@Override
				public Query execute() throws SQLException {
					this.result = Sql.getInstance().getConnection()
							.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
							.executeQuery("Select Points from Teams where SQLUUID = " + team1 + ";");
					return this;
				}
				
				public ResultSet executeWithReturn() throws SQLException {
					this.execute();
					return this.result;
				}
			}.executeWithReturn();
			rs.next();
			new Update("Teams", new Args[] {new Args("Points", Operations.EQUALS, rs.getInt(1) + 2)}, new Args[] {new Args("SQLUUID", Operations.EQUALS, team1)}).execute();
			rs = new Query() {
				private ResultSet result;
				
				@Override
				public Query execute() throws SQLException {
					this.result = Sql.getInstance().getConnection()
							.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
							.executeQuery("Select Points from Teams where SQLUUID = " + team2 + ";");
					return this;
				}
				
				public ResultSet executeWithReturn() throws SQLException {
					this.execute();
					return this.result;
				}
			}.executeWithReturn();
			rs.next();
			new Update("Teams", new Args[] {new Args("Points", Operations.EQUALS, rs.getInt(1) + 2)}, new Args[] {new Args("SQLUUID", Operations.EQUALS, team2)}).execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateLeaderboard(Guild g) {
		new Thread(() -> {
			try {
				ResultSet channel = new Select("Channels", new String[] {"ID"}, new Args[] {new Args("`Name`", Operations.EQUALS, "Leaderboard")}).executeWithReturn();
				channel.next();
				g.getTextChannelById(channel.getLong(1)).getHistory().getRetrievedHistory().forEach(m -> m.delete().queue());
				
				EmbedBuilder message = new EmbedBuilder();
				ResultSet teams = new Query() {
					private ResultSet result;
					@Override
					public Query execute() throws SQLException {
						this.result = Sql.getInstance().getConnection()
								.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
								.executeQuery("select `Name`, Points, `Group` from Teams order by `Group` ASC, Points DESC;");
						return this;
					}
					
					public ResultSet executeWithReturn() throws SQLException {
						this.execute();
						return this.result;
					}
				}.executeWithReturn();
				
				Map<Integer, String> groupStrings = new HashMap<Integer, String>();
				while(teams.next()) {
					if(!groupStrings.containsKey(teams.getInt(3)))
						groupStrings.put(teams.getInt(3), "");
					groupStrings.put(teams.getInt(3), groupStrings.get(teams.getInt(3)).concat(teams.getString(1) + ": **" + teams.getInt(2) + "** points\n"));
				}
				for(Map.Entry<Integer, String> i : groupStrings.entrySet())
					message.addField("**Group " + i.getKey() + "**", i.getValue(), false);
				message.setColor(Color.YELLOW);
				g.getTextChannelById(channel.getLong(1)).sendMessage(message.build()).queue();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}).start();
	}
}
