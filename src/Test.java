
import java.net.URI;
import java.net.http.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    static String get(String key) throws Exception {
        String v = System.getenv(key);
        if (v != null && !v.isBlank()) return v;
        Properties p = new Properties();
        try (var in = new FileInputStream("C:/calendari/cal/config.local.properties")) { p.load(in); }
        return p.getProperty(key);
    }

    // Μικρό helper για να βγάλουμε ένα πεδίο από JSON χωρίς lib (προχειράκι αλλά δουλεύει)
    static String jsonGet(String json, String field) {
        Pattern r = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = r.matcher(json);
        if (m.find()) return m.group(1);
        throw new RuntimeException("Field '" + field + "' not found in JSON");
    }

    public static void main(String[] args) throws Exception {
        String API_KEY = get("AIzaSyDGbZxQ64bf0zwvb50-YzBZMh_rV5AiPCc");
        String PROJECT_ID = get("callendarproject");

        String email = "konstantinakleitsioti@hotmail.com";
        String pass  = "ntina2004";

        // Firebase Auth (Identity Toolkit)
        String loginUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
        String loginPayload = "{\"email\":\""+email+"\",\"password\":\""+pass+"\",\"returnSecureToken\":true}";

        HttpClient http = HttpClient.newHttpClient();
        HttpRequest loginReq = HttpRequest.newBuilder(URI.create(loginUrl))
            .header("Content-Type","application/json")
            .POST(HttpRequest.BodyPublishers.ofString(loginPayload)).build();

        String loginRes = http.send(loginReq, HttpResponse.BodyHandlers.ofString()).body();
        System.out.println("Login response: " + loginRes);

        String idToken = jsonGet(loginRes, "idToken");
        String uid     = jsonGet(loginRes, "localId");

        // ==== Realtime Database ====
        String dbBase = "https://" + PROJECT_ID + ".firebaseio.com";

        // CREATE (push) βιβλίο
        String createUrl = dbBase + "/users/" + uid + "/books.json?auth=" + idToken;
        String body = "{\"title\":\"Δοκιμή\",\"pages\":123}";
        HttpRequest createReq = HttpRequest.newBuilder(URI.create(createUrl))
            .header("Content-Type","application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        var createRes = http.send(createReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("CREATE status=" + createRes.statusCode());
        System.out.println(createRes.body());

        // LIST βιβλία
        String listUrl = dbBase + "/users/" + uid + "/books.json?auth=" + idToken;
        HttpRequest listReq = HttpRequest.newBuilder(URI.create(listUrl))
            .GET().build();

        var listRes = http.send(listReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("LIST status=" + listRes.statusCode());
        System.out.println(listRes.body());
    }
}