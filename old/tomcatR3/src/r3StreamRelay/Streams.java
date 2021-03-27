package r3StreamRelay;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Streams
 */
@WebServlet("/Streams")
public class Streams extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Streams() {
        super();
    }

	/**This is used to respond to verification requests sent by twitch
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String query = request.getQueryString();
		response.getWriter().append(query == null ? "No key due to no query" : query.split("&")[0].split("=")[1]);
		response.setStatus(200);
		this.outputToConsole(request);
	}

	/**This is used to relay data sent from twitch to the r3 bot web service. The body will be handled there
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//String line;
		//while((line = request.getReader().readLine()) != null)
		//	System.out.println(line);
		request.getRequestDispatcher("/Streams/Distributor").forward(request, response);
	}
	
	private void outputToConsole(HttpServletRequest request) throws IOException {
		SimpleDateFormat temp = new SimpleDateFormat("HH:mm:ss");
		temp.getCalendar().setTimeInMillis(System.currentTimeMillis());
		System.out.println("Sent at: " + temp.format(temp.getCalendar().getTime()) + " to: /Streams");
		Collections.list(request.getHeaderNames()).forEach((h) -> System.out.println(h + ": " + request.getHeader(h)));
		System.out.println("METHOD: " + request.getMethod());
		System.out.println("URI: " + request.getRequestURI());
		System.out.println("QUERY: " + request.getQueryString());
		System.out.println("BODY:");
		String line;
		while((line = request.getReader().readLine()) != null)
			System.out.println(line);
		System.out.println("_______________________________________________________________________________________________________________________________________________");
	}
}
