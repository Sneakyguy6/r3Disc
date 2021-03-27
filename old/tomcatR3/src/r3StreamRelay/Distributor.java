package r3StreamRelay;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Distributor. This is responsible for sending the post request data to all the linked APIs.
 */
@WebServlet("/Streams/Distributor")
public class Distributor extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private List<URL> linkedAPIs;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Distributor() {
        super();
        this.linkedAPIs = new ArrayList<URL>();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		URL link = new URL(request.getQueryString().split("=")[1]);
		new Thread(() -> {
			try {
				if(this.linkedAPIs.contains(link)) {
					System.out.println("Refusing to register '" + link + "' as it already has been registered");
					return;
				}
				this.linkedAPIs.add(link);
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(((File) super.getServletContext().getAttribute(ServletContext.TEMPDIR)).getAbsolutePath() + "/APILinks.txt"), true));
				System.out.println("Saving links to file");
				bw.append(link + "\n");
				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
		response.getWriter().append("Done").flush();		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String body = request.getReader().lines().collect(Collectors.joining());
		request.getReader().close();
		System.out.println("APIs: " + this.linkedAPIs);
		new Thread(() -> {
			try {
				for(URL i : this.linkedAPIs) {
					try {
						HttpURLConnection con;
						if(i.getProtocol().equals("https"))
							con = (HttpsURLConnection) i.openConnection();
						else
							con = (HttpURLConnection) i.openConnection();
						con.setRequestMethod("POST");
						con.setRequestProperty("Content-Type", "application/json; utf-8");
						con.setRequestProperty("Accept", "application/json");
						con.setDoOutput(true);
						con.setDoInput(true);
						BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
						bw.append(body);
						bw.flush();
						bw.close();
						con.connect();
						if(Integer.toString(con.getResponseCode()).toCharArray()[0] != '2')
							System.out.println("Server returned " + con.getResponseCode() + ". Something went wrong: " + con.getResponseMessage());
						System.out.println("Sending '" + body + "' to '" + i + "'");
						con.disconnect();
					} catch (SocketTimeoutException e) {
						System.out.println("Unable to connect to: " + i.getHost() + ". Assuming it is temporarily down and skipping.");
					}
				}
			} catch (Exception e) {
				try {
					e.printStackTrace();
					response.sendError(500, e.getMessage());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}).start();
	}
	
	@Override
	public void init() {
		try {
			File f = new File(((File) super.getServletContext().getAttribute(ServletContext.TEMPDIR)).getAbsolutePath() + "/APILinks.txt");
			if(!f.exists())
				f.createNewFile();
			BufferedReader br = new BufferedReader(new FileReader(f));
			System.out.println("Loading links from file");
			String line;
			while((line = br.readLine()) != null)
				this.linkedAPIs.add(new URL(line));
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
