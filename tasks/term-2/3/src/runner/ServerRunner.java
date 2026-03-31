package runner;

import server.Main;

public class ServerRunner {

    static final boolean IS_VIRTUAL    = true; // true = виртуальные потоки
    static final boolean USE_OWN_PARSER = false; // true = мой парсер, false = Gson

    public static void main(String[] args) throws Exception {
        System.out.println("Запуск сервера...");
        System.out.println("isVirtual=" + IS_VIRTUAL + ", useOwnParser=" + USE_OWN_PARSER);
        Main.startServer("localhost", 8080, 10, IS_VIRTUAL, USE_OWN_PARSER);
    }
}
