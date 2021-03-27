package net.sneak.discordTournamentBot.commands;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Query;
import net.sneak.discordTournamentBot.sql.Sql;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Select;

public class UploadFootage implements ICommand {

	@Override
	public String execute(GuildMessageReceivedEvent e) {
		new Thread(() -> {
			try {
				ResultSet rs = new Select("Tournament", new String[] {"HasStarted"}, new Args[] {new Args("GuildID", Operations.EQUALS, e.getGuild().getIdLong())}).executeWithReturn();
				if(!rs.next()) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " The tournament has not started yet.").queue();
					return;
				}
				if(!rs.getBoolean(1)) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " The tournament has not started yet.").queue();
					return;
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			String parts[] = e.getMessage().getContentRaw().split(" ");
			if(parts.length < 3) {
				e.getChannel().sendMessage(e.getMember().getAsMention() + " Not enough arguments!").queue();
				return;
			}
			try {
				if(!parts[2].split("/")[2].split("\\.")[1].equals("youtube")) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " That is not a youtube link. You need to type the full address (e.g. <https://www.youtube.com/watch?v=dQw4w9WgXcQ>).").queue();
					return;
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
				e.getChannel().sendMessage(e.getMember().getAsMention() + " That is not a youtube link. You need to type the full address (e.g. <https://www.youtube.com/watch?v=dQw4w9WgXcQ>).").queue();
				return;
			}
			
			try {
				ResultSet teamRs = new Select("Teams", new String[] {"SQLUUID"}, new Args[] {new Args("Captain", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn();
				if(!teamRs.next()) {
					e.getChannel().sendMessage(e.getMember().getAsMention() + " You are not the captain!").queue();
					return;
				}
				int teamId = teamRs.getInt(1);
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
			if(!gamesSql.next()) {
				e.getChannel().sendMessage(e.getMember().getAsMention() + " There is no game for you to upload footage for").queue();
				return;
			}
			MessageEmbed message = new EmbedBuilder()
					.setColor(Color.RED)
					.setTitle("A video has been uploaded!")
					.setDescription(e.getMember().getAsMention() + " Has uploaded a video of the game for verification. To verify, mention me in one of the server channels and type *verify [accept|deny]*")
					.addField("Team 1", gamesSql.getString(11), false)
					.addField("Team 2", gamesSql.getString(18), true)
					.addField("GameMode", gamesSql.getString(6), false)
					.addField("Group", gamesSql.getInt(7) + "", false)
					.addField("Video", parts[2], false)
					.addField("Verification ID", gamesSql.getInt(1) + "", false)
					.build();
			ResultSet messageIdRs = new Select("VerifyMessages", null, null).executeWithReturn();
			e.getGuild().getMembersWithRoles(e.getGuild().getRoleById(706911282556829726L)).forEach((host) ->
				host.getUser().openPrivateChannel().queue((c) ->
					c.sendMessage(message).queue((m) -> {
						try {
							messageIdRs.moveToInsertRow();
							messageIdRs.updateLong(1, host.getIdLong());
							messageIdRs.updateInt(2, gamesSql.getInt(1));
							messageIdRs.updateLong(3, m.getIdLong());
							messageIdRs.updateString(4, parts[2]);
							messageIdRs.insertRow();
							messageIdRs.moveToCurrentRow();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					})));
			e.getChannel().sendMessage(e.getMember().getAsMention() + " Link sent to hosts for verification. The scores will be updated when verification is complete.").queue();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}).start();
		return null;
	}
	
}
//e.getMember().getAsMention() + " Has uploaded proof. -> " + parts[2] + " <- Please verify it and update the scores."