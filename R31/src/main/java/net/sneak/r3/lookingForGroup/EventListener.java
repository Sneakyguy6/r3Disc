package net.sneak.r3.lookingForGroup;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {
	
	public EventListener() {
		
	}
	
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent e) {
		Group g = LookingForGroup.getInstance().getGroupByCategory(e.getChannelJoined().getParent());
		if(g != null)
			g.resetInactivityCounter();
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		Group g = LookingForGroup.getInstance().getGroupByCategory(e.getChannel().getParent());
		if(g != null)
			g.resetInactivityCounter();
	}
	
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
		System.out.println("Someone reacted to: " + e.getMessageId());
		for(Group g : LookingForGroup.getInstance().getGroups()) {
			System.out.println("\tComparing: " + g.getPost().getIdLong() + "" + e.getMessageIdLong());
			if(g.getPost().getIdLong() == e.getMessageIdLong()) {
				e.getMember().getGuild().addRoleToMember(e.getMember(), g.getRole()).queue();
				g.getCategory().getTextChannels().get(0).sendMessage(
						new EmbedBuilder().setDescription(e.getMember().getEffectiveName() + " has joined the group").setColor(Color.GREEN).build()).queue();
				break;
			}
		}
	}
}
