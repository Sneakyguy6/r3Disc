

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Rest
 */
@WebServlet("/Rest")
public class Rest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Rest() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//String query = request.getQueryString();
		//response.getWriter().append(query == null ? "No key due to no query" : query.split("&")[0].split("=")[1]);
		this.outputToConsole(request);
		String key = request.getHeader("key");
		if(key != null)
			response.getWriter().append(key).flush();
		else
			response.getWriter().append("No header 'key' found.");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Accepted post");
		response.setStatus(200);
		this.outputToConsole(request);
	}
	
	private void outputToConsole(HttpServletRequest request) throws IOException {
		SimpleDateFormat temp = new SimpleDateFormat("HH:mm:ss");
		temp.getCalendar().setTimeInMillis(System.currentTimeMillis());
		System.out.println("Sent at: " + temp.format(temp.getCalendar().getTime()) + " to: /Rest");
		Collections.list(request.getHeaderNames()).forEach((h) -> {
			System.out.println(h + ": " + request.getHeader(h));
		});
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
