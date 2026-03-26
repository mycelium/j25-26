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

	public enum Method { // HTTP-методы
		GET, POST, PUT, PATCH, DELETE
	}

	private final String host;
	private final int port;
	private final int nThreads;
	private final boolean isVirtual;
	private ServerSocketChannel serverChannel; // серверный канал, который принимает соединения
	private ExecutorService executor; // исполнитель, в который будут отправляться задачи на обработку каждого клиента
	private volatile boolean running; // работает ли сервер
	private final Map<String, Map<Method, Handler>> handlers = new HashMap<>(); // структура для хранения обработчиков

	public Server(String host, int port) {
		this(host, port, 10, false);
	}

	public Server(String host, int port, int nThreads, boolean isVirtual) {
		this.host = host;
		this.port = port;
		this.nThreads = nThreads;
		this.isVirtual = isVirtual;
	}

	public void addHandler(Method method, String path, Handler handler) { // добавление обработчиков
		handlers.computeIfAbsent(path, k -> new HashMap<>()).put(method, handler);
	}

	public void start() throws IOException {
		serverChannel = ServerSocketChannel.open(); // привязывается к адресу и порту
		serverChannel.bind(new InetSocketAddress(host, port));
		serverChannel.configureBlocking(true); // переводит канал в блокирующий режим, чтобы accept() ждал соединения
		running = true;

		executor = isVirtual ? Executors.newVirtualThreadPerTaskExecutor() : Executors.newFixedThreadPool(nThreads);

		Thread acceptor = new Thread(() -> { // отдельный поток, который в цикле принимает входящие соединения
			while (running) {
				try {
					SocketChannel client = serverChannel.accept();
					if (client != null) {
						executor.submit(() -> handleClient(client)); // для каждого клиента он отправляет задачу в
																		// executor вызывая handleClient(client)
					}
				} catch (IOException e) {
					if (running)
						System.err.println("Accept error: " + e.getMessage());
				}
			}
		});
		acceptor.setDaemon(true);
		acceptor.start();
	}

	private void handleClient(SocketChannel client) { // обработка 1 клиента
		try (client) {
			Request request = Request.parse(client.socket().getInputStream());
			Response response = new Response();

			Map<Method, Handler> pathHandlers = handlers.get(request.getPath());
			Handler handler = (pathHandlers != null) ? pathHandlers.get(request.getMethod()) : null;
			if (handler != null) {
				try {
					handler.handle(request, response);
				} catch (Exception e) {
					response.setStatus(500);
					response.setBody("Internal Server Error");
				}
			} else {
				response.setStatus(404);
				response.setBody("Not Found");
			}

			response.send(client.socket().getOutputStream()); // записывает HTTP-ответ в выходной поток клиента
		} catch (Exception e) {
			System.err.println("Client error: " + e.getMessage());
		}
	}

	public void stop() {
		running = false;
		try {
			if (serverChannel != null)
				serverChannel.close();
		} catch (IOException e) {
			// ignore
		}
		if (executor != null) {
			executor.shutdown();
			try {
				executor.awaitTermination(5, TimeUnit.SECONDS); // ожидает завершения всех текущих задач в течение 5
																// секунд
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
		}
	}
}