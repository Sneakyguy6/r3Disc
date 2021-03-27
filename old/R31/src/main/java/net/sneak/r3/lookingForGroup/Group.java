package net.sneak.r3.lookingForGroup;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.sneak.r3.Main;

public class Group {
	private Member owner;
	private Message post;
	private int lastActivity;
	private Category category;
	private Role role;
	
	/** The constructor assumes the group is being created (therefore knows who the owner is etc)
	 */
	public Group(String ownerName, String game, String description, List<Group> listToAddTo) throws LFGException {
		Guild g = Main.getBot().getGuildById(Main.IDs.get("R3"));
		try {
			this.owner = g.getMemberByTag(ownerName);
		} catch (IllegalArgumentException e) {
			List<Member> effectiveNameMembers = g.getMembersByEffectiveName(ownerName, false);
			if(effectiveNameMembers.size() == 0)
				throw new LFGException("No member was found with that name on the server. Please check the discord username entered", 2);
			else if(effectiveNameMembers.size() > 1)
				throw new LFGException("It seems more than 1 member on the server has that name. Please use your discord tag (e.g. username#0000)", 1);
			else
				this.owner = effectiveNameMembers.get(0);
		}
		this.lastActivity = 0;//Instant.now().plusSeconds(86400l);
		new Thread(() -> {
			String[] temp = game.split(" ");
			for(int i = 0; i < temp.length; i++) {
				StringBuilder j = new StringBuilder(temp[i]);
				j.setCharAt(0, (temp[i].charAt(0) + "").toUpperCase().charAt(0));
				temp[i] = j.toString();
			}
			String gameFormatted = "";
			for(String i : temp)
				gameFormatted += i;
			this.post = g.getTextChannelById(Main.IDs.get("R3#LookingForGroup")).sendMessage(new EmbedBuilder()
					.setTitle(gameFormatted)
					.setColor(Color.YELLOW)
					.setDescription("Setup by: " + this.owner.getEffectiveName())
					.addField(new Field("Description", description, false))
					.build()).complete();
			this.post.addReaction("U+2705").complete();
			this.role = g.createRole().setName(this.owner.getEffectiveName() + "'s group").complete();
			this.category = g.createCategory(this.owner.getEffectiveName() + "'s group")
					.addPermissionOverride(this.role, EnumSet.of(Permission.VIEW_CHANNEL), null)
					.addPermissionOverride(g.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL)).complete();
			this.category.createTextChannel("general").complete();
			this.category.createVoiceChannel("general").complete();
			this.category.getTextChannels().get(0).sendMessage(new EmbedBuilder().setDescription("NOTE: The group will be deleted if there is no activity for 1 day").setColor(Color.RED).build()).complete();
			g.addRoleToMember(this.owner, this.role).complete();
			listToAddTo.add(this);
		}).start();
	}
	
	public void resetInactivityCounter() {
		this.lastActivity = 0;
	}
	
	public void incrementInactivityCounter() {
		this.lastActivity += 1;
	}
	
	public Member getOwner() {
		return this.owner;
	}

	public Message getPost() {
		return this.post;
	}

	public int getInactivityTime() {
		return this.lastActivity;
	}
	
	public Category getCategory() {
		return this.category;
	}
	
	public Role getRole() {
		return this.role;
	}
	public void close() {
		for(GuildChannel i : this.category.getChannels())
			i.delete().queue();
		this.category.delete().queue();
		this.role.delete().queue();
		this.post.delete().queue();
		this.owner.getUser().openPrivateChannel().queue((c) ->
				c.sendMessage(new EmbedBuilder().setDescription("Your group was disbanded due to inactivity").setColor(Color.RED).build()).queue());
	}
}
class LFGException extends Exception {
	private static final long serialVersionUID = 1L;
	private int errorCode;
	/**List of Codes:
	 * 0: Unknown/unexpected error (probably a glitch or instability)
	 * 1: There is more than 1 member with the specified effective name
	 * 2: No member was found with the specified name
	 * @param message message
	 * @param errorCode code
	 */
	public LFGException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public LFGException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return this.errorCode;
	}
}
