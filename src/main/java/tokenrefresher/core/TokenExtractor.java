package tokenrefresher.core;

import burp.api.montoya.http.message.responses.HttpResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import tokenrefresher.model.TokenRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TokenExtractor {

    private TokenExtractor() {
    }

    public static String extract(TokenRule rule, HttpResponse response) {
        if (response == null) return null;
        switch (rule.getExtractionSource()) {
            case JSON_PATH:
                return extractJsonPath(response.bodyToString(), rule.getExtractionPath());
            case REGEX:
                return extractRegex(response.bodyToString(), rule.getExtractionPath());
            case HEADER:
                return response.headerValue(rule.getExtractionPath());
            case SET_COOKIE:
                return response.cookieValue(rule.getExtractionPath());
            default:
                return null;
        }
    }

    private static String extractJsonPath(String body, String path) {
        if (body == null || path == null || path.isBlank()) return null;
        try {
            JsonElement el = JsonParser.parseString(body);
            for (String key : path.split("\\.")) {
                if (key.isBlank()) continue;
                if (el == null || !el.isJsonObject()) return null;
                el = el.getAsJsonObject().get(key);
            }
            if (el == null) return null;
            return el.isJsonPrimitive() ? el.getAsString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractRegex(String body, String pattern) {
        if (body == null || pattern == null || pattern.isBlank()) return null;
        try {
            Matcher m = Pattern.compile(pattern, Pattern.DOTALL).matcher(body);
            if (m.find()) {
                return m.groupCount() >= 1 ? m.group(1) : m.group();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}

