package apache;

import java.io.IOException;

public class Main {
	
	private static final int PORT = 9999;
	private static final int THREAD_POOL = 64;
	
	public static void main(String[] args) {
		
		HTTPServer server = new HTTPServer(PORT, THREAD_POOL);
		
		server.addHandler("GET", "/messages", (request, responseStream) -> {
			responseStream.write((
				"""
					HTTP/1.1 200 OK\r
					Content-Length: 0\r
					Connection: close\r
					\r
					"""
			).getBytes());
			responseStream.flush();
		});
		server.addHandler("POST", "/messages", (request, responseStream) -> {
			try {
				server.customResponse(responseStream, 401, "Not found");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		server.run();
	}
	
}
