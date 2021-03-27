package net.sneak.r3.lookingForGroup;

import java.awt.Color;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.r3.commands.Command;

public class CommandLfgManager extends Command {

	public CommandLfgManager() {
		super("lfg");
	}
	
	@Override
	public boolean run(GuildMessageReceivedEvent e) {
		String[] parts = e.getMessage().getContentRaw().split(" ");
		try {
			Group g = LookingForGroup.getInstance().getGroupByCategory(e.getChannel().getParent());
			if(g == null || !e.getMember().getRoles().contains(g.getRole())) {
				e.getChannel().sendMessage(new EmbedBuilder().setDescription("You can only use these commands in your group's private channels").setColor(Color.RED).build()).queue();
				return true;
			}
			switch(parts[2].toLowerCase())
			{
			case "kick":
				Member m = this.getMemberByName(parts[3], e.getChannel());
				if(m == null)
					return true;
				if(!g.getOwner().equals(m)) {
					e.getChannel().sendMessage(new EmbedBuilder().setDescription("This command can only be executed by the group owner").setColor(Color.RED).build()).queue();
					return true;
				}
				if(!m.getRoles().contains(g.getRole())) {
					e.getChannel().sendMessage(new EmbedBuilder().setDescription("This user is not in your group").setColor(Color.RED).build()).queue();
					return true;
				}
				e.getGuild().removeRoleFromMember(m, g.getRole());
				m.getUser().openPrivateChannel().queue((c) ->
					c.sendMessage(new EmbedBuilder().setDescription("You have been kicked from " + g.getCategory().getName()).setColor(Color.RED).build()).queue());
				e.getChannel().sendMessage(new EmbedBuilder().setDescription("Removed player from group.").setColor(Color.GREEN).build()).queue();
				break;
			/*case "invite": This method is not needed and makes things more complex and cba
				m = this.getMemberByName(parts[3], e.getChannel());
				if(m == null)
					return true;
				if(!g.getOwner().equals(m)) {
					e.getChannel().sendMessage(new EmbedBuilder().setDescription("This command can only be executed by the group owner").setColor(Color.RED).build()).queue();
					return true;
				}
				if(m.getRoles().contains(g.getRole())) {
					e.getChannel().sendMessage(new EmbedBuilder().setDescription("This user is already in your group").setColor(Color.YELLOW).build()).queue();
					return true;
				} else {
					e.getChannel().sendMessage(new EmbedBuilder().setDescription("Invite sent").setColor(Color.GREEN).build()).queue();
					m.getUser().openPrivateChannel().queue((c) -> 
							c.sendMessage(new EmbedBuilder().setDescription("You have been invited to join " + g.getCategory().getName()).setColor(Color.GREEN).build()).queue());
				}
				break;*/
			case "leave":
				if(e.getMember().getRoles().contains(g.getRole())) {
					e.getGuild().removeRoleFromMember(e.getMember(), g.getRole()).queue();
					e.getChannel().sendMessage(new EmbedBuilder().setDescription(e.getMember().getEffectiveName() + " has left the group").setColor(Color.YELLOW).build()).queue();
				} else 
					e.getChannel().sendMessage(new EmbedBuilder().setDescription("You are not in this group").setColor(Color.RED).build()).queue();
			}
		} catch (IndexOutOfBoundsException ex) {
			e.getChannel().sendMessage(new EmbedBuilder().setDescription("Not enough arguments!").setColor(Color.RED).build()).queue();
		}
		return true;
	}
	
	private Member getMemberByName(String name, TextChannel channel) {
		Member m;
		try {
			m = channel.getGuild().getMemberByTag(name);
		} catch (IllegalArgumentException ex) {
			List<Member> ms = channel.getGuild().getMembersByEffectiveName(name, false);
			if(ms.size() == 0) {
				channel.sendMessage(new EmbedBuilder().setDescription("There is no player with that name").setColor(Color.RED).build()).queue();
				return null;
			} else if(ms.size() > 1) {
				channel.sendMessage(new EmbedBuilder().setDescription("There seems to be more than 1 person with that name.\nPlease use the person's tag instead.").setColor(Color.RED).build()).queue();
				return null;
			}
			m = ms.get(0);
		}
		return m;
	}
}