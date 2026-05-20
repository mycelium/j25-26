package ru.task3;

import ru.task2.http.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestServer {

    private final HttpServer server;

    public TestServer(
            boolean isVirtual,
            JsonAdapter json
    ) {

        server = new HttpServer(
                "localhost",
                8080,
                8,
                isVirtual
        );

        configure(json);
    }

    private void configure(JsonAdapter json) {

        server.addHandler(
                HttpMethod.POST,
                "/request1",
                request -> {

                    RequestData data =
                            json.fromJson(
                                    request.getBody(),
                                    RequestData.class
                            );

                    try {

                        Files.writeString(
                                Path.of("data/storage.txt"),
                                data.name + ":" + data.value
                        );

                        String stored =
                                Files.readString(
                                        Path.of("data/storage.txt")
                                );

                        return new HttpResponse()
                                .body(stored);

                    } catch (IOException e) {

                        return new HttpResponse()
                                .status(500)
                                .body("IO Error");
                    }
                }
        );

        server.addHandler(
                HttpMethod.POST,
                "/request2",
                request -> {

                    RequestData data =
                            json.fromJson(
                                    request.getBody(),
                                    RequestData.class
                            );

                    int result = 0;

                    for (int i = 0; i < 10000; i++) {
                        result += data.value * i;
                    }

                    RequestData response =
                            new RequestData(
                                    data.name,
                                    result
                            );

                    return new HttpResponse()
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .body(
                                    json.toJson(response)
                            );
                }
        );
    }

    public void start() throws IOException {
        server.start();
    }
}