package net.sneak.discordTournamentBot.listener;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Select;

public class OnReady extends ListenerAdapter {
	private static final String[] categoryList = {
			"Tournament"
	};
	private static final String[][] textChannelList = {
			{"Tournament", "Information",},
			{"Tournament", "Announcements"},
			{"Tournament", "Log"},
			{"Tournament", "Leaderboard"}
	};
	
	@Override
	public void onGuildReady(GuildReadyEvent e) {
		this.setupChannelsAndCategories(e.getGuild());
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent e) {
		this.setupChannelsAndCategories(e.getGuild());
	}
	
	private void setupChannelsAndCategories(Guild e) {
		new Thread(() -> {
			try {
				ResultSet categories = new Select("Categories", null, null).executeWithReturn();
				loop:for(String category : categoryList) {
					while(categories.next()) {
						if(category.equals(categories.getString(1))) {
							categories.beforeFirst();
							continue loop;
						}
					}
					try {
						categories.moveToInsertRow();
						categories.updateString(1, category);
						categories.updateLong(2, e.createCategory(category).complete().getIdLong());
						categories.insertRow();
						categories.moveToCurrentRow();
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				}
				
				ResultSet textChannels = new Select("Channels", null, null).executeWithReturn();
				loop:for(String[] channel : textChannelList) {
					while(textChannels.next()) {
						if(channel[1].equals(textChannels.getString(1))) {
							textChannels.beforeFirst();
							continue loop;
						}
					}
					ResultSet category = new Select ("Categories", new String[] {"ID"}, new Args[] {new Args("Name", Operations.EQUALS, channel[0])}).executeWithReturn();
					category.next();
					try {
						textChannels.moveToInsertRow();
						textChannels.updateString(1, channel[1]);
						textChannels.updateLong(2, e.getCategoryById(category.getLong(1)).createTextChannel(channel[1]).complete().getIdLong());
						textChannels.updateString(3, channel[0]);
						textChannels.insertRow();
						textChannels.moveToCurrentRow();
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start();
	}
}
