package net.sneak.discordTournamentBot.listener;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.sneak.discordTournamentBot.Main;
import net.sneak.discordTournamentBot.commands.Commands;

public class Listener extends ListenerAdapter {
	private static Listener instance;
	private List<CustomCallback> customCallbacks;
	private List<CustomCallback> customCallbacksBeforeRegisteredCommands;
	
	public static Listener init() {
		instance = new Listener();
		return instance;
	}
	
	public static Listener getInstance() {
		return instance;
	}
	
	private Listener() {
		this.customCallbacks = new ArrayList<CustomCallback>();
		this.customCallbacksBeforeRegisteredCommands = new ArrayList<CustomCallback>();
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		this.displayMessageToConsole(e);
		try {
			if(e.getMessage().getMentions(MentionType.USER).get(0).getIdLong() != Main.IDs.get("bot"))
				return;
		} catch (IndexOutOfBoundsException ex) {
			return;
		}
		
		String[] parts = e.getMessage().getContentRaw().split(" ");
		System.out.println("BOT ACCEPTED");
		boolean commandHandled = false;
		try {
			for(int i = 0; i < this.customCallbacksBeforeRegisteredCommands.size(); i++) {
				boolean[] r = this.customCallbacksBeforeRegisteredCommands.get(i).run(e);
				if(r[1])
					this.customCallbacksBeforeRegisteredCommands.remove(i);
				if(r[0])
					commandHandled = true;
			}
			if(parts.length < 2 && this.customCallbacks.size() == 0)
				e.getChannel().sendMessage(e.getMember().getAsMention() + " " + Commands.HELP.getExecutor().execute(e)).queue();
			else {
				String returnString = Commands.getCommandFromString(parts[1]).getExecutor().execute(e);
				if(returnString != null)
					e.getChannel().sendMessage(e.getMember().getAsMention() + " " + returnString).queue();
				else {
					for(int i = 0; i < this.customCallbacks.size(); i++) {
						boolean[] r = this.customCallbacks.get(i).run(e);
						if(r[1])
							this.customCallbacks.remove(i);
						if(r[0])
							commandHandled = true;
					}
				}
			}
		} catch(NullPointerException ex) {
			if(!commandHandled)
				e.getChannel().sendMessage(e.getMember().getAsMention() + " Unrecognised command!").queue();
		} catch (IllegalArgumentException ex) {
		}
	}
	
	private void displayMessageToConsole(GuildMessageReceivedEvent e) {
		System.out.println("Message recieved: " + e.getMessage().getContentDisplay());
		System.out.println("Message: " + e.getMessage().getContentRaw());
		System.out.println("ChannelID: " + e.getChannel().getIdLong());
		System.out.println("MemberID: " + e.getMember().getIdLong());
		e.getMember().getRoles().forEach((r) -> {
			System.out.print("Roles: " + r.getName() + ":" + r.getIdLong() + "\t");
		});
		System.out.println();
		e.getMessage().getMentions(MentionType.values()).forEach((mention) -> {
			System.out.print("Mentioned: " + mention.getIdLong() + " String: " + mention.getAsMention());
		});
		System.out.println();
		System.out.println();
	}
	
	public void addCustomCallback(CustomCallback callback, boolean runBefore) {
		if(runBefore)
			this.customCallbacksBeforeRegisteredCommands.add(callback);
		else
			this.customCallbacks.add(callback);
	}
}

/*if(!parts[0].equals("<@!706441475998613534>""<@&706909379764682804>")) {
	//e.getMessage().delete().queue();
	return;
}*/

/*new Thread(new Runnable() {
@Override
public void run() {
	String[] parts = e.getMessage().getContentRaw().split(" ");
	String out = "";
	for(int i = 1; i < parts.length; i++)
		out += parts[i];
	for(Member host : e.getGuild().getMembersWithRoles(e.getGuild().getRoleById(Main.IDs.get("hostRole"))))
		host.getUser().openPrivateChannel().complete().sendMessage(out + " -> " + ((e.getMember().getNickname() == null)? e.getMember().getEffectiveName():e.getMember().getNickname())).submit();
	for(Member host : e.getGuild().getMembersWithRoles(e.getGuild().getRoleById(Main.IDs.get("discordManagerRole"))))
		host.getUser().openPrivateChannel().complete().sendMessage(out + " -> " + ((e.getMember().getNickname() == null)? e.getMember().getEffectiveName():e.getMember().getNickname())).submit();
	e.getChannel().sendMessage(e.getMember().getAsMention() + " your message has been sent.").queue();
}
}).start();*/
