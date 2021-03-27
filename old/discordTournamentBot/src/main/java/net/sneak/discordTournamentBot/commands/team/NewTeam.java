package net.sneak.discordTournamentBot.commands.team;

import java.awt.Color;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.sneak.discordTournamentBot.commands.ICommand;
import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Args.Operations;
import net.sneak.discordTournamentBot.sql.queries.Insert;
import net.sneak.discordTournamentBot.sql.queries.Select;
import net.sneak.discordTournamentBot.sql.queries.Update;

public class NewTeam implements ICommand {
	private int colourCounter;
	private List<Color> colours;
	
	public NewTeam() {
		this.colourCounter = 2;
		this.colours = new ArrayList<Color>();
		this.colours.add(Color.BLUE);
		this.colours.add(Color.CYAN);
		this.colours.add(Color.GREEN);
		this.colours.add(Color.MAGENTA);
		this.colours.add(Color.ORANGE);
		this.colours.add(Color.RED);
		this.colours.add(Color.YELLOW);
		this.colours.add(Color.BLACK);
		this.colours.add(Color.GRAY);
		this.colours.add(Color.LIGHT_GRAY);
	}
	
	@Override
	public String execute(GuildMessageReceivedEvent e) {
		try {
			String[] parts = e.getMessage().getContentRaw().split(" ");
			if(parts.length < 4)
				return "Not enough arguments";
			String name = "";
			for(int i = 3; i < parts.length; i++)
				name += parts[i] + " ";
			name = new StringBuilder(name).deleteCharAt(name.length() - 1).toString();
			
			if(name.equals("cancel"))
				return "This name is a reserved key word, please choose another";
			
			ResultSet temp = new Select("Players", new String[] {"Team"}, new Args[] {new Args("IGUUID", Operations.EQUALS, e.getMember().getIdLong())}).executeWithReturn();
			if(temp.next()) {
				temp.getObject(1);
				if(!temp.wasNull())
					return "You are already in a team!";
			}
			if(new Select("Teams", new String[] {"Name"}, new Args[] {new Args("Name", Operations.EQUALS, name)}).executeWithReturn().next()) {
				e.getChannel().sendMessage(e.getMember().getAsMention() + " That name has already been taken").queue();
				return null;
			}
			
			Role r = e.getGuild().createRole().setColor(this.colours.get(this.colourCounter)).setName(name).complete();
			e.getGuild().addRoleToMember(e.getMember().getIdLong(), r);
			this.colourCounter = (this.colourCounter + 1) % this.colours.size();
			Category c = e.getGuild().createCategory(name).complete();
			this.newTextChannel(e.getGuild(), r, e.getMember(), c);
			this.newVoiceChannel(e.getGuild(), r, e.getMember(), c);
			
			new Insert("Players", new String[] {"IGUUID", "UserTag"}, new Object[] {e.getMember().getIdLong(), e.getMember().getUser().getAsTag()}).onError((insert) -> {}).execute(); //in case the player already exists
			new Insert("Teams", new String[] {"Name", "Captain", "RoleID", "ChannelsID"}, new Object[] {name, e.getMember().getIdLong(), r.getIdLong(), c.getIdLong()}).execute();
			temp = new Select("Teams", new String[] {"SQLUUID"}, new Args[] {new Args("Name", Operations.EQUALS, name)}).executeWithReturn();
			temp.next();
			new Update("Players", new Args[] {new Args("Team", Operations.EQUALS, Integer.toString(temp.getInt(1)))}, new Args[] {new Args("IGUUID", Operations.EQUALS, Long.toString(e.getMember().getIdLong()))}).execute();
			
			e.getGuild().addRoleToMember(e.getMember().getIdLong(), r).queue();
			e.getChannel().sendMessage(e.getMember().getAsMention() + " Your team has been created with private voice and text channel.").queue();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private void newTextChannel(final Guild g, final Role r, final Member captain, final Category c) {
		final ChannelAction<TextChannel> newChannel = c.createTextChannel("text");
		EnumSet<Permission> allowedPermissions = EnumSet.of(Permission.MESSAGE_READ);
		allowedPermissions.add(Permission.MESSAGE_ADD_REACTION);
		allowedPermissions.add(Permission.MESSAGE_ATTACH_FILES);
		allowedPermissions.add(Permission.MESSAGE_EMBED_LINKS);
		allowedPermissions.add(Permission.MESSAGE_EXT_EMOJI);
		allowedPermissions.add(Permission.MESSAGE_HISTORY);
		allowedPermissions.add(Permission.MESSAGE_MENTION_EVERYONE);
		allowedPermissions.add(Permission.MESSAGE_TTS);
		allowedPermissions.add(Permission.MESSAGE_WRITE);
		EnumSet<Permission> deniedPermissions = EnumSet.of(Permission.MANAGE_CHANNEL);
		deniedPermissions.add(Permission.MANAGE_PERMISSIONS);
		deniedPermissions.add(Permission.MANAGE_WEBHOOKS);
		deniedPermissions.add(Permission.CREATE_INSTANT_INVITE);
		deniedPermissions.add(Permission.MESSAGE_MANAGE);
		newChannel.addRolePermissionOverride(r.getIdLong(), allowedPermissions, deniedPermissions);
		final EnumSet<Permission> allPerms = allowedPermissions.clone();
		allPerms.addAll(deniedPermissions);
		g.getRoles().forEach((role) -> {
			if(role.getIdLong() != r.getIdLong())
				newChannel.addRolePermissionOverride(role.getIdLong(), EnumSet.of(Permission.NICKNAME_CHANGE), allPerms);
		});
		EnumSet<Permission> captainPerms = EnumSet.of(Permission.MESSAGE_MANAGE);
		captainPerms.addAll(allowedPermissions);
		deniedPermissions.remove(Permission.MESSAGE_MANAGE);
		newChannel.addMemberPermissionOverride(captain.getIdLong(), captainPerms, deniedPermissions);
		newChannel.queue();
	}
	
	private void newVoiceChannel(final Guild g, final Role r, final Member captain, final Category c) {
		final ChannelAction<VoiceChannel> newVoiceChannel = c.createVoiceChannel("voice");
		EnumSet<Permission> allowedVoicePermissions = EnumSet.of(Permission.VOICE_CONNECT);
		allowedVoicePermissions.add(Permission.VOICE_SPEAK);
		allowedVoicePermissions.add(Permission.VOICE_STREAM);
		allowedVoicePermissions.add(Permission.VOICE_USE_VAD);
		allowedVoicePermissions.add(Permission.VIEW_CHANNEL);
		EnumSet<Permission> deniedVoicePermissions = EnumSet.of(Permission.MANAGE_CHANNEL);
		deniedVoicePermissions.add(Permission.MANAGE_PERMISSIONS);
		deniedVoicePermissions.add(Permission.MANAGE_WEBHOOKS);
		deniedVoicePermissions.add(Permission.VOICE_MOVE_OTHERS);
		deniedVoicePermissions.add(Permission.CREATE_INSTANT_INVITE);
		deniedVoicePermissions.add(Permission.VOICE_DEAF_OTHERS);
		deniedVoicePermissions.add(Permission.VOICE_MUTE_OTHERS);
		deniedVoicePermissions.add(Permission.PRIORITY_SPEAKER);
		newVoiceChannel.addRolePermissionOverride(r.getIdLong(), allowedVoicePermissions, deniedVoicePermissions);
		final EnumSet<Permission> allPerms = allowedVoicePermissions.clone();
		allPerms.add(Permission.MESSAGE_READ); //hides the channel
		allPerms.addAll(deniedVoicePermissions);
		g.getRoles().forEach((role) -> {
			if(role.getIdLong() != r.getIdLong())
				newVoiceChannel.addRolePermissionOverride(role.getIdLong(), EnumSet.of(Permission.NICKNAME_CHANGE), allPerms);
		});
		EnumSet<Permission> captainPerms = EnumSet.of(Permission.VOICE_MUTE_OTHERS);
		captainPerms.add(Permission.VOICE_DEAF_OTHERS);
		captainPerms.add(Permission.PRIORITY_SPEAKER);
		captainPerms.addAll(allowedVoicePermissions);
		deniedVoicePermissions.remove(Permission.PRIORITY_SPEAKER);
		deniedVoicePermissions.remove(Permission.VOICE_DEAF_OTHERS);
		deniedVoicePermissions.remove(Permission.VOICE_MUTE_OTHERS);
		newVoiceChannel.addMemberPermissionOverride(captain.getIdLong(), captainPerms, deniedVoicePermissions);
		newVoiceChannel.queue();
	}
}
