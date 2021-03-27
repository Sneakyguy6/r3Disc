package net.sneak.r3;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import com.google.common.collect.ImmutableMap;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.sneak.r3.commands.Listener;
import net.sneak.r3.commands.TerminalListener;
import net.sneak.r3.lookingForGroup.EventListener;
import net.sneak.r3.lookingForGroup.LookingForGroup;
import net.sneak.r3.roleBot.Roles;

public class Main {
	public static final ImmutableMap<String, Long> IDs = ImmutableMap.<String, Long>builder()
			.put("R3#Announcements", 732958640276766761L)
			.put("R3#LookingForGroup", 734764188131655762L)
			.put("R3#streamSubscribe", 0l)
			.put("R3", 731547378280300564L)
			.build();
	private static JDA BOT;
	
	public static JDA getBot() {
		return BOT;
	}
	
	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException, LoginException, InstantiationException, IllegalAccessException {
		Server.startServer();
		Listener.init();
		TerminalListener.init();
		LookingForGroup.init();
		Listener.getInstance().initCommands();
		Roles.init();
		BOT = JDABuilder
				.create(GUILD_MEMBERS, GUILD_MESSAGES, GUILD_VOICE_STATES, GUILD_PRESENCES, GUILD_EMOJIS)
				.addEventListeners(Listener.getInstance(), Roles.getInstance(), new EventListener())
				.setToken("NzMxNTQ5MTQxMDQ5NTQwNjc5.Xwnq5A.hiPmNnLYOw8qo0zQPE7F9WDnQQc")
				.build();
		while(BOT.getStatus() != Status.CONNECTED)
			Thread.sleep(100);
		Roles.getInstance().initRoles(BOT.getGuildById(IDs.get("R3")));
		LookingForGroup.getInstance().sendInstructionsPost(BOT.getGuildById(IDs.get("R3")));
	}
	
	public static void close() {
		try {
			System.out.println("Stopping services");
			Roles.getInstance().close();
			LookingForGroup.getInstance().close();
			Server.close();
			BOT.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*public static void test() throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder()
				.url("https://api.twitch.tv/helix/streams?first=20&after=eyJiIjpudWxsLCJhIjp7Ik9mZnNldCI6MjB9fQ==")
				.method("GET", null)
				.addHeader("Client-ID", "uo6dggojyb8d6soh92zknwmi5ej1q2")
				.addHeader("Authorization", "Bearer 2gbdx6oar67tqtcmt49t3wpcgycthx").build();
		Response response = client.newCall(request).execute();
		System.out.println(response.body().string());
	}*/
}


/* Twitch IDs:
 * R3memberThis: 156359753
 * Sneakyguy6: 125482807
 * 
 * token: kq9qpfacu0egsvxsimykbss8k5rx2l
 * 8kul0s3rduyjue8wy7qlzugv7drk7n
 */