package net.sneak.r3.lookingForGroup;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.sneak.r3.Main;

public class LookingForGroup {
	private List<Group> groups;
	private boolean doExpirationLoop;
	
	private LookingForGroup() {
		this.groups = new ArrayList<Group>();
		this.doExpirationLoop = true;
		this.expirationLoop();
	}
	
	public void sendInstructionsPost(Guild g) {
		g.getTextChannelById(Main.IDs.get("R3#LookingForGroup")).sendMessage(new EmbedBuilder()
				.setDescription("To create a new group, [click here](https://conwynavision.com:9000/tomcatR3/LookingForGroup/createPost.jsp)")
				.setColor(Color.ORANGE)
				.build()).queue();
	}
	
	private void expirationLoop() {
		new Thread(() -> {
			while(this.doExpirationLoop) {
				for(int i = 0; i < this.groups.size(); i++) {
					Group g = this.groups.get(i);
					if(g.getInactivityTime() == 20) {
						g.close();
						this.groups.remove(i);
					} else {
						boolean activityCurrentlyInVoice = false;
						for(VoiceChannel j : g.getCategory().getVoiceChannels()) {
							if(j.getMembers().size() != 0) {
								activityCurrentlyInVoice = true;
								break;
							}
						}
						if(activityCurrentlyInVoice)
							g.resetInactivityCounter();
						else
							g.incrementInactivityCounter();
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public String createNewGroup(String ownerName, String game, String description) {
		try {
			//this.groups.add(new Group(ownerName, game, description));
			new Group(ownerName, game, description, this.groups);
			return "Your group has been created";
		} catch (LFGException e) {
			return e.getMessage();
		}
	}
	
	public Group getGroupByOwner(Member owner) {
		for(Group i : this.groups)
			if(i.getOwner().equals(owner))
				return i;
		return null;
	}
	
	/**@deprecated This method is kind of useless since one user can be in multiple groups
	 */
	@Deprecated
	public Group getGroupByRole(Role role) {
		for(Group i : this.groups)
			if(i.getRole().equals(role))
				return i;
		return null;
	}
	
	public Group getGroupByCategory(Category c) {
		for(Group i : this.groups)
			if(i.getCategory().equals(c))
				return i;
		return null;
	}
	
	public List<Group> getGroups() {
		return Collections.unmodifiableList(this.groups);
	}
	
	public void close() {
		this.doExpirationLoop = false;
	}
	
	private static LookingForGroup instance;
	
	public static void init() {
		instance = new LookingForGroup();
	}
	
	public static LookingForGroup getInstance() {
		return instance;
	}
}
