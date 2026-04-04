package httpserver;
public interface Handler { // как обрабатывать входящие HTTP-запросы
	void handle(Request request, Response response) throws Exception;
}