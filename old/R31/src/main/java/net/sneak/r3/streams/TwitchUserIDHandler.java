package net.sneak.r3.streams;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TwitchUserIDHandler implements HttpHandler {
private TwitchSubscribeLoop loopInstance;
	
	public TwitchUserIDHandler(TwitchSubscribeLoop loopInstance) {
		this.loopInstance = loopInstance;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		String userId = t.getRequestHeaders().get("userId").get(0);
		if(t.getRequestHeaders().get("function").get(0).equals("add")) {
			if(!this.loopInstance.getUserIds().contains(userId))
				this.loopInstance.getUserIds().add(userId);
			System.out.println("Added '" + userId + "' to twitch subscribe loop");
		} else {
			this.loopInstance.getUserIds().remove(userId);
			System.out.println("Removed '" + userId + "' to twitch subscribe loop");
		}
		
		t.sendResponseHeaders(200, "Done".length());
        OutputStream os = t.getResponseBody();
        os.write("Done".getBytes());
        t.close();
	}
}
