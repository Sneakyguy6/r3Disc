package net.sneak.discordTournamentBot.commands;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.ScoreBoard;
import net.sneak.discordTournamentBot.commands.tournament.StartTournament;
import net.sneak.discordTournamentBot.listener.Listener;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.Query;
import net.sneak.discordTournamentBot.sql.Sql;
import net.sneak.discordTournamentBot.sql.queries.Delete;
import net.sneak.discordTournamentBot.sql.queries.Select;

public class Verify implements ICommand {
	
	private long modifier;
	private boolean active;
	public Verify() {
		this.modifier = -1;
		this.active = false;
	}
	
	@Override
	public String execute(final GuildMessageReceivedEvent e) {
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(active) {
						e.getChannel().sendMessage(e.getMember().getAsMention() + " Someone is already handling a verification. Please wait.").queue();
						return;
					}
					String[] parts = e.getMessage().getContentRaw().split(" ");
					if(parts.length < 4) {
						e.getChannel().sendMessage(e.getMember().getAsMention() + " Not enough arguments!").queue();
						return;
					}
					ResultSet messageRs;
					try {
						messageRs = new Select("VerifyMessages", null, new Args[] {new Args("Game", Operations.EQUALS, Integer.parseInt(parts[3]))}).executeWithReturn();
					} catch (NumberFormatException ex) {
						e.getChannel().sendMessage(e.getMember().getAsMention() + " The 4th argument should be a number (the verification ID)").queue();
						return;
					}
					if(!messageRs.next()) {
						e.getChannel().sendMessage(e.getMember().getAsMention() + " There is no message with this verification ID").queue();
						return;
					}
					
					switch(parts[2])
					{
					case "accept":
						active = true;
						e.getChannel().sendMessage(e.getMember().getAsMention() + " Configure the score update using commands beginning with '&'").queue();
						modifier = e.getMember().getIdLong();
						this.updateGames(messageRs, e.getGuild());
						return;
					case "deny":
						e.getChannel().sendMessage(e.getMember().getAsMention() + " Scores have not been updated. Please notify the team captains to update their footage").queue();
						this.deleteMessage(messageRs, e);
						return;
					default:
						e.getChannel().sendMessage(e.getMember().getAsMention() + " Unrecognised verify command. The options are 'accept' or 'deny'").queue();
						return;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			private void deleteMessage(ResultSet messageRs, GuildMessageReceivedEvent event) throws SQLException {
				messageRs.beforeFirst();
				while(messageRs.next())
					event.getGuild().getMemberById(messageRs.getLong(1)).getUser().openPrivateChannel().complete().deleteMessageById(messageRs.getLong(3)).queue();
				messageRs.first();
				new Delete("VerifyMessages", new Args[] {new Args("Game", Operations.EQUALS, messageRs.getInt(2))}).execute();
				messageRs.close();
			}
			
			private void updateGames(ResultSet messageRs, Guild g) throws SQLException {
				final ResultSet game = new Select("Games", null, new Args[] {new Args("SQLUUID", Operations.EQUALS, messageRs.getInt(2))}).executeWithReturn();
				game.next();
				final boolean[] status = {false, false};
				Listener.getInstance().addCustomCallback((event) -> {
					try {
						String[] parts = event.getMessage().getContentRaw().split(" ");
						if(parts[1].toCharArray()[0] != '&' || event.getMember().getIdLong() != modifier)
							return new boolean[] {false, false};
						switch(parts[1].toLowerCase())
						{
						case "&team1":
							game.updateInt(4, Integer.parseInt(parts[2]));
							status[0] = true;
							e.getChannel().sendMessage(e.getMember().getAsMention() + " Done.").queue();
							break;
						case "&team2":
							game.updateInt(5, Integer.parseInt(parts[2]));
							status[1] = true;
							e.getChannel().sendMessage(e.getMember().getAsMention() + " Done.").queue();
							break;
						case "&update":
							for(int i = 0; i < status.length; i++) {
								if(!status[i]) {
									e.getChannel().sendMessage(e.getMember().getAsMention() + " You have not updated the score for team " + (i+1)).queue();
									return new boolean[] {true, false};
								}
							}
							game.updateBoolean(9, true);
							game.updateRow();
							this.runUpdateGames(game, messageRs, g);
							active = false;
							return new boolean[] {true, true};
						default:
							e.getChannel().sendMessage(e.getMember().getAsMention() + " Unrecognised command!").queue();
							break;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (IndexOutOfBoundsException e) {
						event.getChannel().sendMessage(event.getMember().getAsMention() + " Not enough arguments!").queue();
					} catch (NumberFormatException e) {
						event.getChannel().sendMessage(event.getMember().getAsMention() + " The last argument must be a number (the team's score)").queue();
					}
					return new boolean[] {true, false};
				}, true);
			}
			
			private void runUpdateGames(ResultSet game, ResultSet messageRs, Guild g) throws SQLException {
				final ResultSet gameWithTeams = new Query() {
					private ResultSet result;
					@Override
					public Query execute() throws SQLException {
						this.result = Sql.getInstance().getConnection()
								.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
								.executeQuery("SELECT * FROM ((Games LEFT JOIN Teams ON Games.Team1=Teams.SQLUUID) LEFT JOIN Teams AS Teams2 ON Games.Team2=Teams2.SQLUUID) WHERE Games.SQLUUID = " + game.getInt(1) + ";");
						return this;
					}
					
					public ResultSet executeWithReturn() throws SQLException {
						this.execute();
						return this.result;
					}
				}.executeWithReturn();
				gameWithTeams.next();
				ResultSet videos = new Query() {
					private ResultSet rs;
					@Override
					public Query execute() throws SQLException {
						this.rs = Sql.getInstance().getConnection()
								.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
								.executeQuery("select VideoLink from VerifyMessages where Game = " + game.getInt(1) + " order by HostId ASC limit 2;");
						return this;
					}
					
					public ResultSet executeWithReturn() throws SQLException {
						this.execute();
						return this.rs;
					}
				}.executeWithReturn();
				String videoString = "";
				while(videos.next())
					videoString += "<" + videos.getString(1) + ">\n";
				ResultSet channel = new Select("Channels", new String[] {"ID"}, new Args[] {new Args("`Name`", Operations.EQUALS, "Log")}).executeWithReturn();
				channel.next();
				g.getTextChannelById(channel.getLong(1)).sendMessage(new EmbedBuilder()
						.setColor(Color.YELLOW)
						.setTitle(gameWithTeams.getString(11) + " vs " + gameWithTeams.getString(18))
						.setDescription((gameWithTeams.getInt(4) != gameWithTeams.getInt(5))? ((gameWithTeams.getInt(4) > gameWithTeams.getInt(5))? gameWithTeams.getString(11) : gameWithTeams.getString(18)) + " won." : "The game was a tie")
						.addField("GameMode", gameWithTeams.getString(6), false)
						.addField("Videos", videoString, false)
						.build()
				).queue();
				
				this.deleteMessage(messageRs, e);
				
				if(gameWithTeams.getInt(4) > gameWithTeams.getInt(5))
					ScoreBoard.getInstance().win(gameWithTeams.getInt(2), gameWithTeams.getInt(3));
				else if(gameWithTeams.getInt(4) < gameWithTeams.getInt(5))
					ScoreBoard.getInstance().win(gameWithTeams.getInt(3), gameWithTeams.getInt(2));
				else if(gameWithTeams.getInt(4) == gameWithTeams.getInt(5))
					ScoreBoard.getInstance().draw(gameWithTeams.getInt(2), gameWithTeams.getInt(3));
				ScoreBoard.getInstance().updateLeaderboard(g);
				
				StartTournament.displayNextGame(g, game.getInt(2));
				StartTournament.displayNextGame(g, game.getInt(3));
			}
		}).start();
		return null;
	}
}
