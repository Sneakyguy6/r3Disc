package r3StreamRelay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonParser;

/**
 * Servlet implementation class TwitchSubscribe
 */
@WebServlet("/Streams/TwitchSubscribe")
public class TwitchSubscribe extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static final String twitchAuthToken = "Bearer jd8ardu70qppjwjasksn8204c59tgl";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TwitchSubscribe() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String requestBody = 
				"{" +
				"\"hub.topic\": \"https://api.twitch.tv/helix/streams?user_id=[PLACEHOLDER]\", " + 
				"\"hub.mode\": \"subscribe\", " + 
				"\"hub.callback\": \"https://conwynavision.com:9000/tomcatR3/Streams\", " + 
				"\"hub.lease_seconds\": 864000" +
				"}";
		System.out.println("Requesting twitch user ID for: " + request.getParameter("username"));
		HttpsURLConnection getUserId = (HttpsURLConnection) new URL("https://api.twitch.tv/helix/users?login=" + request.getParameter("username")).openConnection();
		getUserId.setRequestProperty("Client-ID", "e81p55jfcl1ppc6q0xek81vjajjq8f");
		getUserId.setRequestProperty("Authorization", twitchAuthToken);
		getUserId.connect();
		String userId;
		if(Integer.toString(getUserId.getResponseCode()).toCharArray()[0] == '2') {
			BufferedReader br = new BufferedReader(new InputStreamReader(getUserId.getInputStream()));
			String temp = "";
			String line;
			while((line = br.readLine()) != null)
				temp += line;
			try {
				userId = JsonParser.parseString(temp).getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
			} catch (IndexOutOfBoundsException e) {
				response.getWriter().append("Twitch could not find anyone with that name. Was it entered correctly?");
				getUserId.disconnect();
				return;
			}
			requestBody = requestBody.replace("[PLACEHOLDER]", userId);
			System.out.println("Found twitch user ID successfully");
		} else {
			response.getWriter().append("Something went wrong: " + getUserId.getResponseCode());
			getUserId.disconnect();
			return;
		}
		getUserId.disconnect();
		System.out.println("Going to send post to twitch: " + requestBody);
		HttpsURLConnection postSubscribe = (HttpsURLConnection) new URL("https://api.twitch.tv/helix/webhooks/hub").openConnection();
		postSubscribe.setRequestProperty("Client-ID", "e81p55jfcl1ppc6q0xek81vjajjq8f");
		postSubscribe.setRequestProperty("Authorization", twitchAuthToken);
		postSubscribe.setRequestProperty("Content-Type", "application/json");
		postSubscribe.setDoInput(true);
		postSubscribe.setDoOutput(true);
		postSubscribe.setRequestMethod("POST");
		postSubscribe.connect();
		postSubscribe.getOutputStream().write(requestBody.getBytes());
		if(Integer.toString(postSubscribe.getResponseCode()).toCharArray()[0] == '2')
			response.getWriter().append("The server will now post notifications when you start streaming");
		else {
			BufferedReader br = new BufferedReader(new InputStreamReader(postSubscribe.getErrorStream()));
			String temp = "";
			String line;
			while((line = br.readLine()) != null)
				temp += line;
			response.getWriter().append("Something went wrong when posting the request: " + postSubscribe.getResponseCode() + ": " + temp);
		}
		postSubscribe.disconnect();
		
		HttpURLConnection botServer = (HttpURLConnection) new URL("http://192.168.1.20:25564/twitchSubscibeUpdater").openConnection();
		botServer.setRequestProperty("userId", userId);
		botServer.setRequestProperty("function", "add");
		botServer.connect();
		if(Integer.toString(botServer.getResponseCode()).toCharArray()[0] != '2') {
			BufferedReader br = new BufferedReader(new InputStreamReader(botServer.getErrorStream()));
			String temp = "";
			String line;
			while((line = br.readLine()) != null)
				temp += line;
			response.getWriter().append("Something went wrong when posting the request: " + botServer.getResponseCode() + ": " + temp);
		}
		botServer.disconnect();
	}
}
