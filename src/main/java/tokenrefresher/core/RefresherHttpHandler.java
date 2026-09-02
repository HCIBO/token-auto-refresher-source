package tokenrefresher.core;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import tokenrefresher.i18n.I18n;
import tokenrefresher.model.TokenProfile;
import tokenrefresher.model.TokenRule;
import tokenrefresher.store.ProfileRegistry;

import java.util.regex.Pattern;

public class RefresherHttpHandler implements HttpHandler {

    private static final String RETRIED_NOTE_MARKER = "[token-refresher:retried]";

    private final MontoyaApi api;
    private final ProfileRegistry registry;
    private final ThreadLocal<Boolean> inFlightMacroCall = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private volatile boolean unloaded = false;

    public RefresherHttpHandler(MontoyaApi api, ProfileRegistry registry) {
        this.api = api;
        this.registry = registry;
    }

    public void shutdown() {
        unloaded = true;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        if (unloaded || inFlightMacroCall.get()) {
            
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        HttpRequest current = requestToBeSent;
        int matchCount = 0;
        for (TokenProfile profile : registry.all()) {
            if (!matchesScope(profile, requestToBeSent)) continue;
            matchCount++;
            if (profile.isForceRefreshEveryRequest()) {
                forceRefresh(profile);
            } else {
                ensureFreshToken(profile);
            }
            current = injectAll(profile, current);
            if (requestToBeSent.toolSource().toolType() == burp.api.montoya.core.ToolType.REPEATER) {
                for (TokenRule rule : profile.getRules()) {
                    if (rule.getCurrentValue() == null) continue;
                    api.logging().logToOutput("[TokenRefresher] " + I18n.t("log.inject",
                            profile.getName(), requestToBeSent.method(), requestToBeSent.path(),
                            rule.getInjectionTarget(), rule.getInjectionName(), tokenPreview(rule.getCurrentValue())));
                }
            }
        }
        if (matchCount > 1) {
            api.logging().logToOutput("[TokenRefresher] " + I18n.t("log.multiMatch", matchCount));
        }
        return RequestToBeSentAction.continueWith(current);
    }

    private static String tokenPreview(String token) {
        if (token == null) return "null";
        if (token.length() <= 18) return token;
        return token.substring(0, 12) + "..." + token.substring(token.length() - 6);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        if (unloaded || inFlightMacroCall.get()) {
            return ResponseReceivedAction.continueWith(responseReceived);
        }

        Annotations existing = responseReceived.annotations();
        if (existing != null && existing.hasNotes() && existing.notes() != null && existing.notes().contains(RETRIED_NOTE_MARKER)) {
            return ResponseReceivedAction.continueWith(responseReceived); 
        }

        short status = responseReceived.statusCode();

        for (TokenProfile profile : registry.all()) {
            if (!profile.isAutoRetryOn401()) continue;
            if (!matchesScope(profile, responseReceived.initiatingRequest(), responseReceived.toolSource().toolType())) continue;
            if (!looksLikeAuthFailure(profile, responseReceived, status)) continue;

            api.logging().logToOutput("[TokenRefresher] " + I18n.t("log.authFailureDetected", profile.getName(), status));
            forceRefresh(profile);
            if (!profile.hasAnyValue()) continue;

            HttpRequest retryRequest = injectAll(profile, responseReceived.initiatingRequest());
            inFlightMacroCall.set(true);
            try {
                HttpRequestResponse retried = api.http().sendRequest(retryRequest);
                if (retried.response() != null) {
                    Annotations note = Annotations.annotations(RETRIED_NOTE_MARKER + " " + I18n.t("log.retriedSent", status, profile.getName()));
                    api.logging().logToOutput("[TokenRefresher] " + I18n.t("log.retriedSent", status, profile.getName()));
                    return ResponseReceivedAction.continueWith(retried.response(), note);
                }
            } catch (Exception e) {
                api.logging().logToError("[TokenRefresher] " + I18n.t("log.retryFailed", profile.getName()), e);
            } finally {
                inFlightMacroCall.set(false);
            }
        }

        return ResponseReceivedAction.continueWith(responseReceived);
    }

    private boolean looksLikeAuthFailure(TokenProfile p, HttpResponseReceived resp, short status) {
        if (status == 401 || status == 403) return true;
        String pattern = p.getAuthFailureBodyPattern();
        if (pattern == null || pattern.isBlank()) return false;
        try {
            return Pattern.compile(pattern, Pattern.DOTALL).matcher(resp.bodyToString()).find();
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureFreshToken(TokenProfile p) {
        if (p.hasAllValues() && !isStale(p)) return;
        doRefresh(p);
    }

    private void forceRefresh(TokenProfile p) {
        doRefresh(p);
    }

    private boolean isStale(TokenProfile p) {
        Long exp = p.getTokenExpiresAtEpochMillis();
        if (exp != null) {
            return System.currentTimeMillis() >= exp - (p.getMarginSeconds() * 1000L);
        }
        if (p.getManualTtlSeconds() > 0) {
            return System.currentTimeMillis() >= p.getLastRefreshedAtEpochMillis() + (p.getManualTtlSeconds() * 1000L);
        }
        return false; 
    }

    private synchronized void doRefresh(TokenProfile p) {
        if (unloaded) {
            return;
        }
        HttpRequest loginRequest = p.getLoginRequest();
        if (loginRequest == null) {
            p.setLastError(I18n.t("err.noLoginRequest"));
            return;
        }
        p.setRefreshing(true);
        inFlightMacroCall.set(true);
        try {
            HttpRequestResponse rr = api.http().sendRequest(loginRequest);
            if (rr == null || rr.response() == null) {
                p.setLastError(I18n.t("err.noResponse"));
                return;
            }

            StringBuilder failedRules = new StringBuilder();
            Long earliestExp = null;
            for (int i = 0; i < p.getRules().size(); i++) {
                TokenRule rule = p.getRules().get(i);
                String value = TokenExtractor.extract(rule, rr.response());
                if (value == null || value.isEmpty()) {
                    failedRules.append(failedRules.length() > 0 ? ", " : "").append("#").append(i + 1);
                    continue; 
                }
                rule.setCurrentValue(value);
                if (p.isProactiveJwtRefresh()) {
                    Long exp = JwtUtil.extractExpMillis(value);
                    if (exp != null && (earliestExp == null || exp < earliestExp)) earliestExp = exp;
                }
            }

            p.setLastRefreshedAtEpochMillis(System.currentTimeMillis());
            p.setTokenExpiresAtEpochMillis(earliestExp);

            if (failedRules.length() > 0) {
                p.setLastError(I18n.t("err.ruleNotFound", failedRules.toString()));
            } else {
                p.setLastError(null);
            }
            p.setRefreshCount(p.getRefreshCount() + 1);
            api.logging().logToOutput("[TokenRefresher] " + (failedRules.length() > 0
                    ? I18n.t("log.refreshedWarn", p.getName(), p.getRefreshCount(), failedRules.toString())
                    : I18n.t("log.refreshedOk", p.getName(), p.getRefreshCount())));
        } catch (Exception e) {
            p.setLastError(e.getMessage());
            api.logging().logToError("[TokenRefresher] " + I18n.t("log.refreshError", p.getName()), e);
        } finally {
            inFlightMacroCall.set(false);
            p.setRefreshing(false);
            registry.fireChanged();
        }
    }

    private boolean matchesScope(TokenProfile p, HttpRequestToBeSent req) {
        return matchesScope(p, req, req.toolSource().toolType());
    }

    private boolean matchesScope(TokenProfile p, HttpRequest req, burp.api.montoya.core.ToolType toolType) {
        if (!p.isEnabled()) return false;
        if (!p.getApplyToTools().contains(toolType)) return false;
        if (req.httpService() == null) return false;

        String host = req.httpService().host();
        String scope = p.getScopeHostMatch();

        if (scope == null || scope.isBlank()) {
            if (p.getLoginRequest() == null || p.getLoginRequest().httpService() == null) return false;
            return host.equalsIgnoreCase(p.getLoginRequest().httpService().host());
        }
        if (scope.startsWith("re:")) {
            try {
                return Pattern.compile(scope.substring(3)).matcher(host).find();
            } catch (Exception e) {
                return false;
            }
        }
        return host.toLowerCase().contains(scope.toLowerCase());
    }

    public String refreshNow(TokenProfile p) {
        forceRefresh(p);
        if (p.getLastError() != null) {
            return I18n.t("refreshNow.failed", p.getLastError());
        }
        StringBuilder sb = new StringBuilder(I18n.t("refreshNow.header"));
        for (int i = 0; i < p.getRules().size(); i++) {
            TokenRule r = p.getRules().get(i);
            sb.append("  #").append(i + 1).append(" ").append(r.getInjectionTarget()).append(" '")
                    .append(r.getInjectionName()).append("' = ").append(tokenPreview(r.getCurrentValue())).append("\n");
        }
        if (p.getTokenExpiresAtEpochMillis() != null) {
            sb.append(I18n.t("refreshNow.expiresAt", new java.util.Date(p.getTokenExpiresAtEpochMillis())));
        }
        return sb.toString();
    }

    public static HttpRequest injectAll(TokenProfile p, HttpRequest req) {
        HttpRequest current = req;
        for (TokenRule rule : p.getRules()) {
            if (rule.getCurrentValue() == null) continue;
            current = injectRule(rule, current);
        }
        return current;
    }

    private static HttpRequest injectRule(TokenRule rule, HttpRequest req) {
        String value = rule.getCurrentValue();
        switch (rule.getInjectionTarget()) {
            case HEADER: {
                String prefix = rule.getInjectionPrefix() == null ? "" : rule.getInjectionPrefix();
                String headerValue = prefix + value;
                return req.hasHeader(rule.getInjectionName())
                        ? req.withUpdatedHeader(rule.getInjectionName(), headerValue)
                        : req.withAddedHeader(rule.getInjectionName(), headerValue);
            }
            case COOKIE:
                return req.withParameter(HttpParameter.cookieParameter(rule.getInjectionName(), value));
            case QUERY_PARAM:
                return req.withParameter(HttpParameter.urlParameter(rule.getInjectionName(), value));
            default:
                return req;
        }
    }
}

