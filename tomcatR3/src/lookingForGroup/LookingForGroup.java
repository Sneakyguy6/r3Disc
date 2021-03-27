package lookingForGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LookingForGroup
 */
@WebServlet("/lfg")
public class LookingForGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LookingForGroup() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Called from: " + request.getRemoteAddr());
		HttpURLConnection c = (HttpURLConnection) new URL("http://192.168.1.20:25564/lfg").openConnection();
		c.addRequestProperty("Discord-username", request.getParameter("username"));
		c.addRequestProperty("Discord-game", request.getParameter("game"));
		c.addRequestProperty("Discord-description", request.getParameter("description"));
		c.connect();
		//System.out.println(c.getResponseCode());
		String out = "";
		if(Integer.toString(c.getResponseCode()).toCharArray()[0] == '2') {
			BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
			String line;
			while((line = br.readLine()) != null)
				out += line;
		} else
			out = "Something went wrong: Error=" + c.getResponseCode();
		response.getWriter().append(out);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
