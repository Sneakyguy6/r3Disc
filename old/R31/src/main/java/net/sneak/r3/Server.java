package net.sneak.r3;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.sneak.r3.lookingForGroup.LookingForGroup;
import net.sneak.r3.streams.TwitchSubscribeLoop;
import net.sneak.r3.streams.TwitchUserIDHandler;

public class Server {
	private static HttpServer server;
	private static TwitchSubscribeLoop twitchSubscribeLoop;
	public static void startServer() throws IOException {
		twitchSubscribeLoop = new TwitchSubscribeLoop();
		verifications = new HashMap<Long, String[]>();
        server = HttpServer.create(new InetSocketAddress(25564), 0);
        server.createContext("/twitch", new TwitchHandler());
        server.createContext("/lfg", new LookingForGroupHandler());
        server.createContext("/lfgVerify", new LookingForGroupVerification());
        server.createContext("/twitchSubscibeUpdater", new TwitchUserIDHandler(twitchSubscribeLoop));
        server.setExecutor(null); // creates a default executor
        server.start();
        new Thread(twitchSubscribeLoop).start();
    }
	
	public static void close() {
		System.out.println("HTTP shutting down");
		if(server != null)
			server.stop(0);
		twitchSubscribeLoop.close();
	}
	
	private static Map<Long, String[]> verifications;
	static class LookingForGroupVerification implements HttpHandler { //?verifKey=[key]
		@Override
		public void handle(HttpExchange t) throws IOException {
			SimpleDateFormat temp = new SimpleDateFormat("HH:mm:ss");
			temp.getCalendar().setTimeInMillis(System.currentTimeMillis());
			System.out.println("Sent at: " + temp.format(temp.getCalendar().getTime()) + " to: /lfg");
        	t.getRequestHeaders().keySet().forEach((k) -> System.out.println(k + ": " + t.getRequestHeaders().get(k)));
        	System.out.println();
        	System.out.println("METHOD: " + t.getRequestMethod());
        	System.out.println("URI: " + t.getRequestURI());
        	System.out.println("BODY:");
        	String body = "";
        	BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
        	String line;
        	while((line = br.readLine()) != null)
        		body += line;
        	System.out.println(body);
        	System.out.println("Host: " + t.getRemoteAddress().getHostString());
        	System.out.println("_______________________________________________________________________________________________________________________________________________");
        	t.getResponseHeaders().put("Content-type", Arrays.asList(new String[] {"text/plain"}));
        	t.getResponseHeaders().put("Accept-encoding", Arrays.asList(new String[] {"gzip"}));
        	
        	String response;
        	if(t.getRequestHeaders().get("accept").get(0).equals("yes")) {
        		try {
            		String[] properties = verifications.get(Long.parseLong(t.getRequestHeaders().get("verifKey").get(0)));
            		response = LookingForGroup.getInstance().createNewGroup(properties[0], properties[1], properties[2]);
            		verifications.remove(Long.parseLong(t.getRequestHeaders().get("verifKey").get(0)));
            	} catch (NullPointerException e) {
            		//e.printStackTrace();
            		response = "Invalid verification key";
            	}
        	} else {
        		verifications.remove(Long.parseLong(t.getRequestHeaders().get("verifKey").get(0)));
        		response = "Your request has been cancelled";
        	}
        	
        	t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        	
		}
	}
    
	static class LookingForGroupHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			SimpleDateFormat temp = new SimpleDateFormat("HH:mm:ss");
			temp.getCalendar().setTimeInMillis(System.currentTimeMillis());
			System.out.println("Sent at: " + temp.format(temp.getCalendar().getTime()) + " to: /lfg");
        	t.getRequestHeaders().keySet().forEach((k) -> System.out.println(k + ": " + t.getRequestHeaders().get(k)));
        	System.out.println();
        	System.out.println("METHOD: " + t.getRequestMethod());
        	System.out.println("URI: " + t.getRequestURI());
        	System.out.println("BODY:");
        	String body = "";
        	BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
        	String line;
        	while((line = br.readLine()) != null)
        		body += line;
        	System.out.println(body);
        	System.out.println("Host: " + t.getRemoteAddress().getHostString());
        	System.out.println("_______________________________________________________________________________________________________________________________________________");
        	t.getResponseHeaders().put("Content-type", Arrays.asList(new String[] {"text/plain"}));
        	t.getResponseHeaders().put("Accept-encoding", Arrays.asList(new String[] {"gzip"}));
        	//t.getResponseHeaders().add("Connection", "keep-alive");
        	
        	String[] properties = {
        			t.getRequestHeaders().get("Discord-username").get(0),
        			t.getRequestHeaders().get("Discord-game").get(0),
        			t.getRequestHeaders().get("Discord-description").get(0)};
        	Guild g = Main.getBot().getGuildById(Main.IDs.get("R3"));
        	Member m = null;
        	String response;
        	try {
    			m = g.getMemberByTag(properties[0]);
    			response = "A verification has been sent. Please follow the instructions to verify that the discord name entered really is you";
    		} catch (IllegalArgumentException e) {
    			List<Member> effectiveNameMembers = g.getMembersByEffectiveName(properties[0], false);
    			if(effectiveNameMembers.size() == 0)
    				response = "No member was found with that name on the server. Please check the discord username entered";
    			else if(effectiveNameMembers.size() > 1)
    				response = "It seems more than 1 member on the server has that name. Please use your discord tag (e.g. username#0000)";
    			else {
    				m = effectiveNameMembers.get(0);
    				response = "A verification has been sent. Please follow the instructions to verify that the discord name entered really is you";
    			}
    		}
        	if(m != null) {
        		long r = new Random().nextLong();
            	verifications.put(r, properties);
            	m.getUser().openPrivateChannel().queue((c) -> 
            			c.sendMessage(new EmbedBuilder()
            					.setColor(Color.GREEN)
            					.setDescription("A looking for group post was created under your name. If this post was made by you, please click the 'Accept' link. "
            							+ "If not please click the 'Deny' link."
            							+ "\n[Accept](https://conwynavision.com:9000/tomcatR3/LookingForGroup/confirmed.html?verifKey=" + r + "&accept=yes)"
            							+ "\n[Deny](https://conwynavision.com:9000/tomcatR3/LookingForGroup/confirmed.html?verifKey=" + r + "&accept=no)")
            					.build()).queue());
        	}
        	
        	t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            //t.close();
		}
	}
    static class TwitchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        	SimpleDateFormat temp = new SimpleDateFormat("HH:mm:ss");
			temp.getCalendar().setTimeInMillis(System.currentTimeMillis());
			System.out.println("Sent at: " + temp.format(temp.getCalendar().getTime()) + " to: /twitch");
        	t.getRequestHeaders().keySet().forEach((k) -> System.out.println(k + ": " + t.getRequestHeaders().get(k)));
        	System.out.println();
        	System.out.println("METHOD: " + t.getRequestMethod());
        	System.out.println("URI: " + t.getRequestURI());
        	System.out.println("BODY:");
        	String body = "";
        	BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
        	String line;
        	while((line = br.readLine()) != null)
        		body += line;
        	System.out.println(body);
        	System.out.println("_______________________________________________________________________________________________________________________________________________");
        	if(t.getRequestMethod().equals("GET")) {
        		String out = t.getRequestHeaders().get("key").get(0);
        		t.sendResponseHeaders(200, out.length());
                OutputStream os = t.getResponseBody();
                os.write(out.getBytes());
                os.close();
                return;
        	}
        	t.sendResponseHeaders(200, "Accepted".length());
            OutputStream os = t.getResponseBody();
            os.write("Accepted".getBytes());
            os.close();
            final String jsonString = body;
            new Thread(() -> {
            	JsonArray data = JsonParser.parseString(jsonString).getAsJsonObject().get("data").getAsJsonArray();
            	for(JsonElement i : data) {
            		JsonObject o = i.getAsJsonObject();
            		if(!o.get("type").getAsString().equals("live"))
                		continue;
                	Main.getBot().getTextChannelById(Main.IDs.get("R3#Announcements")).sendMessage(new EmbedBuilder()
                			.setColor((135 * 65536) + 211)
                			.setTitle(o.get("user_name").getAsString() + " has started streaming!")
                			.addField(new Field("Title", o.get("title").getAsString(), true))
                			.addField(new Field("Started at (GMT)", o.get("started_at").getAsString().split("T")[1].replace('Z', ' '), true))
                			.setTimestamp(Instant.now())
                			.setDescription("https://www.twitch.tv/" + o.get("user_name").getAsString())
                			.setImage(o.get("thumbnail_url").getAsString().replace("{width}", "1920").replace("{height}", "1080"))
                			.build()).queue();
            	}
            }).start();
        }
    }
}
