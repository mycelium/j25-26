import papka_HTTP.AllMethods;
import papka_HTTP.Server_HTTP;

public class Main {
    public static void main(String[] args) throws Exception {
        Server_HTTP server = new Server_HTTP(8081, "localhost", 10, false);

        server.addHandler("/", AllMethods.GET, (req, res) -> {
            res.setStutus(200);
            res.setBody("Hi");
        });

        server.addHandler("/users", AllMethods.GET, (req, res) -> {
            res.setStutus(200);
            res.setBody("AAA");
        });

        server.addHandler("/submit", AllMethods.POST, (req, res) -> {
            String body = req.getBody().toString();
            System.out.println("Форма отправлена: " + body);
            res.setStutus(200);
            res.setBody("Форма получена");
        });

        server.addHandler("/users/update", AllMethods.PUT, (req, res) -> {
            String body = req.getBody().toString();
            System.out.println("Новые данные: " + body);
            res.setStutus(200);
            res.setBody("Обновление прошло");
        });

        server.addHandler("/users/delete", AllMethods.DELETE, (req, res) -> {
            System.out.println("Пользователь удален");
            res.setStutus(200);
            res.setBody("Пользователь удален");
        });

        server.startServer();
    }
}
