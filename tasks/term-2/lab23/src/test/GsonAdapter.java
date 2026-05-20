package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lab1.LibMain;

public class GsonAdapter {
    private final boolean useGson;
    private final LibMain ownParser;
    private final Gson gson;

    public GsonAdapter(boolean useGson) {
        this.useGson = useGson;
        this.ownParser = new LibMain();
        this.gson = new GsonBuilder().create();
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        if (useGson) {
            return gson.fromJson(json, clazz);
        } else {
            return ownParser.readValue(json, clazz);
        }
    }

    public String toJson(Object obj) {
        if (useGson) {
            return gson.toJson(obj);
        } else {
            return ownParser.writeValueAsString(obj);
        }
    }
}