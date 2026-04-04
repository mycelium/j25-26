package httpserver;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

	public enum Method {
		GET, POST, PUT, PATCH, DELETE
	}

	private final String host;
	private final int port;
	private final int nThreads;
	private final boolean isVirtual;
	private ServerSocketChannel serverChannel;
	private ExecutorService executor;
	private volatile boolean running;
	private final Map<String, Map<Method, Handler>> handlers = new HashMap<>();

	public Server(String host, int port) {
		this(host, port, 10, false);
	}

	public Server(String host, int port, int nThreads, boolean isVirtual) {
		this.host = host;
		this.port = port;
		this.nThreads = nThreads;
		this.isVirtual = isVirtual;
	}

	public void addHandler(Method method, String path, Handler handler) {
		handlers.computeIfAbsent(path, k -> new HashMap<>()).put(method, handler);
	}

	public void start() throws IOException {
		serverChannel = ServerSocketChannel.open();
		serverChannel.bind(new InetSocketAddress(host, port));
		serverChannel.configureBlocking(true);
		running = true;

		executor = isVirtual ? Executors.newVirtualThreadPerTaskExecutor() : Executors.newFixedThreadPool(nThreads);

		Thread acceptor = new Thread(() -> {
			while (running) {
				try {
					SocketChannel client = serverChannel.accept();
					if (client != null) {
						executor.submit(() -> handleClient(client));
					}
				} catch (IOException e) {
					if (running) {
						e.printStackTrace(); // добавлено
						System.err.println("Accept error: " + e.getMessage());
					}
				}
			}
		});
		acceptor.setDaemon(true);
		acceptor.start();
	}

	private void handleClient(SocketChannel client) {
	    Response response = new Response();

	    try {
	        System.out.println("HANDLE CLIENT START");

	        Request request = Request.parse(client.socket().getInputStream());

	        Map<Method, Handler> pathHandlers = handlers.get(request.getPath());
	        Handler handler = (pathHandlers != null) ? pathHandlers.get(request.getMethod()) : null;

	        if (handler != null) {
	            handler.handle(request, response);
	        } else {
	            response.setStatus(404);
	            response.setBody("Not Found");
	        }

	    } catch (Exception e) {
	        System.err.println("Client error FULL:");
	        e.printStackTrace();

	        response.setStatus(500);
	        response.setBody("Internal Server Error: " + e.getMessage());
	    }

	    try {
	        response.send(client.socket().getOutputStream());

	        // даём времени сети отправить данные
	        client.socket().getOutputStream().flush();

	        // небольшая задержка
	        Thread.sleep(5);

	        client.close();

	    } catch (Exception e) {
	        System.err.println("Failed to send response: " + e.getMessage());
	    }
	}
	public void stop() {
		running = false;
		try {
			if (serverChannel != null)
				serverChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (executor != null) {
			executor.shutdown();
			try {
				executor.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace(); 
				executor.shutdownNow();
			}
		}
	}
}