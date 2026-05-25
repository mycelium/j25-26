import jsonlib.Json;
import java.util.*;

public class Main {

    // ── Domain model ──────────────────────────────────────────────────────────

    public static class Studio {
        private String  name;
        private String  country;
        private int     founded;

        public Studio() {}

        public String getName()          { return name; }
        public void   setName(String v)  { this.name = v; }
        public String getCountry()       { return country; }
        public void   setCountry(String v) { this.country = v; }
        public int    getFounded()       { return founded; }
        public void   setFounded(int v)  { this.founded = v; }

        @Override
        public String toString() {
            return "Studio{name='" + name + "', country='" + country + "', founded=" + founded + "}";
        }
    }

    public static class Game {
        private String       title;
        private int          releaseYear;
        private double       score;
        private boolean      multiplayer;
        private List<String> platforms;
        private Studio       studio;
        private String[]     tags;

        public Game() {}

        public String       getTitle()                    { return title; }
        public void         setTitle(String v)            { this.title = v; }
        public int          getReleaseYear()              { return releaseYear; }
        public void         setReleaseYear(int v)         { this.releaseYear = v; }
        public double       getScore()                    { return score; }
        public void         setScore(double v)            { this.score = v; }
        public boolean      isMultiplayer()               { return multiplayer; }
        public void         setMultiplayer(boolean v)     { this.multiplayer = v; }
        public List<String> getPlatforms()                { return platforms; }
        public void         setPlatforms(List<String> v)  { this.platforms = v; }
        public Studio       getStudio()                   { return studio; }
        public void         setStudio(Studio v)           { this.studio = v; }
        public String[]     getTags()                     { return tags; }
        public void         setTags(String[] v)           { this.tags = v; }

        @Override
        public String toString() {
            return "Game{"
                    + "title='"      + title       + '\''
                    + ", releaseYear=" + releaseYear
                    + ", score="     + score
                    + ", multiplayer=" + multiplayer
                    + ", platforms=" + platforms
                    + ", studio="    + studio
                    + ", tags="      + Arrays.toString(tags)
                    + '}';
        }
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        demoDeserializeGame();
        demoRoundTrip();
        demoMapParsing();
        demoCollections();
    }

    // ── Demos ─────────────────────────────────────────────────────────────────

    private static void demoDeserializeGame() {
        System.out.println("─── Deserialize Game ───────────────────────────");
        String json = """
            {
                "title":       "Hollow Knight",
                "releaseYear": 2017,
                "score":       9.1,
                "multiplayer": false,
                "platforms":   ["PC", "Switch", "PS4", "Xbox"],
                "studio": {
                    "name":    "Team Cherry",
                    "country": "Australia",
                    "founded": 2014
                },
                "tags": ["metroidvania", "indie", "platformer"]
            }
            """;

        Game g = Json.fromJson(json, Game.class);
        System.out.println("Parsed : " + g);
    }

    private static void demoRoundTrip() {
        System.out.println("\n─── Round-trip (serialize → deserialize) ───────");
        Studio s = new Studio();
        s.setName("FromSoftware");
        s.setCountry("Japan");
        s.setFounded(1986);

        Game g = new Game();
        g.setTitle("Elden Ring");
        g.setReleaseYear(2022);
        g.setScore(9.5);
        g.setMultiplayer(true);
        g.setPlatforms(List.of("PC", "PS5", "Xbox Series"));
        g.setStudio(s);
        g.setTags(new String[]{"open-world", "action-rpg", "soulslike"});

        String json = Json.toJson(g);
        System.out.println("JSON   : " + json);

        Game parsed = Json.fromJson(json, Game.class);
        System.out.println("Back   : " + parsed);
    }

    private static void demoMapParsing() {
        System.out.println("\n─── Parse to Map ───────────────────────────────");
        String json = """
            {
                "status":  "ok",
                "version": 2,
                "flags":   { "debug": false, "beta": true }
            }
            """;

        Map<String, Object> map = Json.fromJsonToMap(json);
        System.out.println("Map    : " + map);
    }

    private static void demoCollections() {
        System.out.println("\n─── Serialize collections & primitives ─────────");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("null_field", null);
        payload.put("integer",    100);
        payload.put("decimal",    3.14159);
        payload.put("boolean",    true);
        payload.put("array",      new int[]{1, 2, 3});
        payload.put("list",       List.of("x", "y", "z"));
        payload.put("nested",     Map.of("a", 1, "b", 2));

        System.out.println("JSON   : " + Json.toJson(payload));
    }
}
