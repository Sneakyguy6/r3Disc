package net.sneak.r3.streams;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import net.dv8tion.jda.api.exceptions.HttpException;

public class TwitchSubscribeLoop implements Runnable {
	private List<String> userIds;
	private boolean doLoop;
	
	public TwitchSubscribeLoop() throws IOException {
		this.userIds = new ArrayList<String>();
		
		File f = new File(System.getProperty("user.dir") + "/twitchUserIds.txt");
		if(f.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			while((line = br.readLine()) != null)
				this.userIds.add(line);
			br.close();
		} else
			f.createNewFile();
		this.doLoop = true;
	}
	
	@Override
	public void run() {
		while(this.doLoop) {
			for(String i : this.userIds) {
				try {
					HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.twitch.tv/helix/webhooks/hub").openConnection();
					con.setRequestProperty("Client-ID", "e81p55jfcl1ppc6q0xek81vjajjq8f");
					con.setRequestProperty("Authorization", "Bearer jd8ardu70qppjwjasksn8204c59tgl");
					con.setRequestProperty("Content-Type", "application/json");
					con.setDoInput(true);
					con.setDoOutput(true);
					con.setRequestMethod("POST");
					con.connect();
					con.getOutputStream().write(("{" +
							"\"hub.topic\": \"https://api.twitch.tv/helix/streams?user_id=" + i + "\", " + 
							"\"hub.mode\": \"subscribe\", " + 
							"\"hub.callback\": \"https://conwynavision.com:9000/tomcatR3/Streams\", " + 
							"\"hub.lease_seconds\": 864000" +
							"}").getBytes());
					if(Integer.toString(con.getResponseCode()).toCharArray()[0] != '2') {
						BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
						String temp = "";
						String line;
						while((line = br.readLine()) != null)
							temp += line;
						throw new HttpException("Something went wong trying to update '" + i + "'" + "Error: " + con.getResponseCode() + ": " + temp);
					} else
						System.out.println("Sent POST to update twitch subscribers for user '" + i + "'");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for(int i = 0; i < 432000 && this.doLoop; i++) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			File f = new File(System.getProperty("user.dir") + "/twitchUserIds.txt");
			f.delete();
			f.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			for(String i : this.userIds)
				bw.append(i + "\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		this.doLoop = false;
	}
	
	public List<String> getUserIds() {
		return this.userIds;
	}
}