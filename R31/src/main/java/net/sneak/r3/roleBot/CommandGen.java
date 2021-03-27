package net.sneak.r3.roleBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.sneak.r3.commands.Command;

public class CommandGen extends Command {

	public CommandGen() {
		super("gen");
	}

	@Override
	public String description() {
		
		return null;
	}

	@Override
	public boolean run(GuildMessageReceivedEvent e) {
		try {
			System.out.println(e.getMessage().getContentRaw());
			Role r = Roles.getInstance().assignGenRole(e.getMember(), Integer.parseInt(e.getMessage().getContentRaw().split(" ")[2]));
			e.getChannel().sendMessage(new EmbedBuilder().setDescription("You are now in the '" + r.getName() + "' group").setColor(r.getColor()).build()).queue();
		} catch (NumberFormatException ex) {
			e.getChannel().sendMessage("Your generation must be a number").queue();
			ex.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException ex) {
			e.getChannel().sendMessage("Not enough arguments").queue();
			ex.printStackTrace();
		}
		return true;
	}
}
