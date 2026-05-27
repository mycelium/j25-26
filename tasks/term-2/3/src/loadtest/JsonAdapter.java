package loadtest;

import jsonlib.Json;

/**
 * Abstraction over JSON parsers so the load test can swap
 * between the custom library (Lab-1) and Gson at runtime.
 */
public interface JsonAdapter {

    String toJson(Object obj);
    <T> T fromJson(String json, Class<T> clazz);

    // -------------------------------------------------------------------------
    // Built-in implementations
    // -------------------------------------------------------------------------

    /** Uses the custom Lab-1 JSON library. */
    JsonAdapter OWN = new JsonAdapter() {
        @Override
        public String toJson(Object obj) {
            return Json.toJson(obj);
        }
        @Override
        public <T> T fromJson(String json, Class<T> clazz) {
            return Json.toObject(json, clazz);
        }
        @Override
        public String toString() { return "OwnParser"; }
    };

    /**
     * Uses Gson via reflection so the project compiles even without Gson on
     * the classpath — it will fail at runtime with a clear message if absent.
     */
    JsonAdapter GSON = new JsonAdapter() {
        private Object gson;

        private Object gson() {
            if (gson == null) {
                try {
                    Class<?> gsonClass = Class.forName("com.google.gson.Gson");
                    gson = gsonClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Gson not found on classpath. Add gson.jar to /lib. " + e.getMessage(), e);
                }
            }
            return gson;
        }

        @Override
        public String toJson(Object obj) {
            try {
                return (String) gson().getClass()
                    .getMethod("toJson", Object.class)
                    .invoke(gson(), obj);
            } catch (Exception e) { throw new RuntimeException(e); }
        }

        @Override
        public <T> T fromJson(String json, Class<T> clazz) {
            try {
                @SuppressWarnings("unchecked")
                T result = (T) gson().getClass()
                    .getMethod("fromJson", String.class, Class.class)
                    .invoke(gson(), json, clazz);
                return result;
            } catch (Exception e) { throw new RuntimeException(e); }
        }

        @Override
        public String toString() { return "Gson"; }
    };
}
