package apache;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HTTPServer {
	
	private static int PORT = 0;
	private final ConcurrentHashMap<String, Map<String, Handler>> handlerHashMap;
	private ExecutorService executorService;
	
	public HTTPServer(int port, int threadPool) {
		this.executorService = Executors.newFixedThreadPool(threadPool);
		handlerHashMap = new ConcurrentHashMap<>();
		PORT = port;
	}
	
	public void run() {
		try (final ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				final Socket socket = serverSocket.accept();
				executorService.submit(() -> prepare(socket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void prepare(Socket socket) {
		try (
			final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
		)
		{
			Request request = Request.parse(in);
			
			var param = request.getQueryParam("last");
			var params = request.getQueryParams();
			
			if (!params.isEmpty()) {
				System.out.println(param);
				System.out.println(params);
			}
			System.out.println(request.getPath());
			
			if (!handlerHashMap.containsKey(request.getMethod())) {
				customResponse(out, 404, "Not found");
				return;
			}
			
			Map<String, Handler> handlerMap = handlerHashMap.get(request.getMethod());
			String pathRequest = preparePath(request.getPath());
			
			if (handlerMap.containsKey(pathRequest)) {
				Handler handler = handlerMap.get(pathRequest);
				handler.handle(request, out);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void customResponse(BufferedOutputStream out, int code, String status) throws IOException {
		out.write((
			"HTTP/1.1 " + code + " " + status + "\r\n" +
				"Content-Length: 0\r\n" +
				"Connection: close\r\n" +
				"\r\n"
		).getBytes());
		out.flush();
	}
	
	public void addHandler(String method, String path, Handler handler) {
		if (!handlerHashMap.containsKey(method)) {
			handlerHashMap.put(method, new HashMap<>());
		}
		handlerHashMap.get(method).put(path, handler);
	}
	
	public String preparePath(String url) {
		int i = url.indexOf("?");
		if (i == -1) {
			return url;
		}
		return url.substring(0, i);
	}
	
}
