package net.sneak.discordTournamentBot;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.sneak.discordTournamentBot.listener.Listener;
import net.sneak.discordTournamentBot.listener.OnReady;
import net.sneak.discordTournamentBot.sql.Sql;

public class Main {
	public static final Map<String, Long> IDs = new HashMap<String, Long>();
	public static void main(String[] args) throws LoginException {
		IDs.put("hostRole", /*702947086101905561L*/ 706911282556829726L);
		//IDs.put("discordManager", 0L);
		IDs.put("bot", 706908679160725514L /*706441475998613534L*/);
		//IDs.put("managerChannel", 706466843363246080L);
		
		Sql.init();
		Listener.init();
		ScoreBoard.init();
		JDA bot = JDABuilder
				.create(GUILD_MEMBERS, GUILD_MESSAGES, GUILD_VOICE_STATES, GUILD_PRESENCES, GUILD_EMOJIS)
				.addEventListeners(Listener.getInstance())
				.addEventListeners(new OnReady())
				//.setToken("NzA2NDQxNDc1OTk4NjEzNTM0.XrFVUQ.nGIqTdxfKO9QGf8_Bctnct-DfTs") //live
				.setToken("NzA2OTA4Njc5MTYwNzI1NTE0.XrLpwA.9IZ2GM9zPgoB1UMBMENrQYhTTKs") //test
				.build();
		
		new Thread(new Runnable() {
			private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			@Override
			public void run() {
				try {
					System.out.println("type stop to stop the bot");
					while(!br.readLine().equals("stop"));
					System.out.println("Shutting down the bot.");
					bot.shutdown();
					System.out.println("Shutting down Sql.");
					Sql.getInstance().close();
					System.out.println("Goodbye!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
