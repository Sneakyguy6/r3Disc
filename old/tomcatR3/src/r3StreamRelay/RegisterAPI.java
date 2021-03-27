package r3StreamRelay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Register
 */
@WebServlet("/Streams/RegisterAPI")
public class RegisterAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterAPI() {
        super();
    }

	/**Returns 406 if the ip is not entered in the query
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ip;
		try {
			ip = request.getQueryString().split("=")[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			response.getWriter().append("The query is not formatted properly.\nError: " + e.getMessage());
			response.getWriter().append("Example of a working URI: \"GET https://conwynavision.com:9000/tomcatR3/Register?ip=http://domain.com/\".").flush();
			return;
		} catch (NullPointerException e) {
			response.getWriter().append("No query was found. You must add a query containing the ip\nExample of a working URI: \"GET https://conwynavision.com:9000/tomcatR3/Register?ip=http://domain.com/\". ");
			response.getWriter().append("Example of a working URI: \"GET https://conwynavision.com:9000/tomcatR3/Register?ip=http://domain.com/\".").flush();
			return;
		}
		String out = "";
		try {
			HttpURLConnection con;
			long rn = new Random().nextLong();
			
			switch(ip.split(":")[0])
			{
			case "http":
				con = (HttpURLConnection) new URL(ip).openConnection();
				break;
			case "https":
				con = (HttpsURLConnection) new URL(ip).openConnection();
				break;
			default:
				throw new MalformedURLException("Protocol not supported or not added. Must include either 'http://' or 'https://' at the start");
			}
			con.setRequestMethod("GET");
			con.addRequestProperty("key", Long.toString(rn));
			con.connect();
			if(Integer.toString(con.getResponseCode()).toCharArray()[0] != '2') {
				response.getWriter().append("Your server did not return a 2xx response code. Did an error occur?").flush();
				return;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while((line = br.readLine()) != null)
				out += line;
			br.close();
			if(Long.parseLong(out) != rn) {
				response.getWriter().append("The sent verification code sent by your server is not equal to the code sent. It should have returned: '" + rn + "'. It returned: " + out).flush();
				return;
			}
			response.sendRedirect("/tomcatR3/Streams/Distributor" + "?" + request.getQueryString());
		} catch (MalformedURLException | SocketTimeoutException | UnknownHostException e) {
			response.getWriter().append("An error occured when trying to connect to your server: " + e.getClass().getName() + ": " + e.getMessage()).flush();
		} catch (NumberFormatException e) {
			response.getWriter().append("There is an issue with the response sent by your server. It sent: '" + out + "'. Example of acceptable response: '123456789'").flush();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
