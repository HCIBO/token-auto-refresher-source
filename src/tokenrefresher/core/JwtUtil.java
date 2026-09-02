package tokenrefresher.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class JwtUtil {

    private JwtUtil() {
    }

    public static Long extractExpMillis(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        if (parts.length < 2) return null;
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(padBase64Url(parts[1]));
            String json = new String(decoded, StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("exp") && obj.get("exp").isJsonPrimitive()) {
                return obj.get("exp").getAsLong() * 1000L;
            }
        } catch (Exception ignored) {
            
        }
        return null;
    }

    private static String padBase64Url(String s) {
        int mod = s.length() % 4;
        if (mod == 0) return s;
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < 4 - mod; i++) sb.append('=');
        return sb.toString();
    }
}

