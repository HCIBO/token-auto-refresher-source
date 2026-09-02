package tokenrefresher.model;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.persistence.PersistedObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TokenProfile {

    private final String id;
    private String name = "Yeni Profil";
    private HttpRequest loginRequest;

    private List<TokenRule> rules = new ArrayList<>(List.of(defaultRule()));

    private String scopeHostMatch = "";
    private Set<ToolType> applyToTools = new LinkedHashSet<>(Arrays.asList(
            ToolType.PROXY, ToolType.REPEATER, ToolType.INTRUDER, ToolType.SCANNER, ToolType.EXTENSIONS));

    private boolean enabled = true;
    private boolean autoRetryOn401 = true;
    private String authFailureBodyPattern = "";
    private boolean proactiveJwtRefresh = true;
    private boolean forceRefreshEveryRequest = false;
    private int marginSeconds = 30;
    private int manualTtlSeconds = 0;

    private transient volatile long lastRefreshedAtEpochMillis = 0L;
    private transient volatile Long tokenExpiresAtEpochMillis;
    private transient volatile String lastError;
    private transient volatile int refreshCount = 0;
    private transient volatile boolean refreshing = false;

    public TokenProfile() {
        this.id = UUID.randomUUID().toString();
    }

    private TokenProfile(String id) {
        this.id = id;
    }

    private static TokenRule defaultRule() {
        TokenRule r = new TokenRule();
        r.setExtractionSource(ExtractionSource.JSON_PATH);
        r.setInjectionTarget(InjectionTarget.HEADER);
        r.setInjectionName("Authorization");
        r.setInjectionPrefix("Bearer ");
        return r;
    }

    public static TokenProfile fromPersisted(String id, PersistedObject po) {
        TokenProfile p = new TokenProfile(id);
        p.name = orDefault(po.getString("name"), "Profil");
        p.loginRequest = po.getHttpRequest("loginRequest");
        p.scopeHostMatch = orDefault(po.getString("scopeHostMatch"), "");

        Integer ruleCount = po.getInteger("ruleCount");
        if (ruleCount != null && ruleCount > 0) {
            List<TokenRule> loaded = new ArrayList<>();
            for (int i = 0; i < ruleCount; i++) {
                PersistedObject ruleObj = po.getChildObject("rule_" + i);
                if (ruleObj != null) loaded.add(TokenRule.fromPersisted(ruleObj));
            }
            if (!loaded.isEmpty()) p.rules = loaded;
        } else if (po.getString("extractionSource") != null) {
            
            TokenRule legacy = new TokenRule();
            legacy.setExtractionSource(safeEnum(ExtractionSource.class, po.getString("extractionSource"), ExtractionSource.JSON_PATH));
            legacy.setExtractionPath(orDefault(po.getString("extractionPath"), ""));
            legacy.setInjectionTarget(safeEnum(InjectionTarget.class, po.getString("injectionTarget"), InjectionTarget.HEADER));
            legacy.setInjectionName(orDefault(po.getString("injectionName"), "Authorization"));
            legacy.setInjectionPrefix(orDefault(po.getString("injectionPrefix"), ""));
            p.rules = new ArrayList<>(List.of(legacy));
        }

        String toolsCsv = po.getString("applyToolsCsv");
        if (toolsCsv != null && !toolsCsv.isBlank()) {
            Set<ToolType> set = new LinkedHashSet<>();
            for (String t : toolsCsv.split(",")) {
                try {
                    set.add(ToolType.valueOf(t.trim()));
                } catch (Exception ignored) {
                }
            }
            if (!set.isEmpty()) p.applyToTools = set;
        }

        Boolean en = po.getBoolean("enabled");
        p.enabled = en == null || en;
        Boolean ar = po.getBoolean("autoRetryOn401");
        p.autoRetryOn401 = ar == null || ar;
        p.authFailureBodyPattern = orDefault(po.getString("authFailureBodyPattern"), "");
        Boolean pj = po.getBoolean("proactiveJwtRefresh");
        p.proactiveJwtRefresh = pj == null || pj;
        Boolean fr = po.getBoolean("forceRefreshEveryRequest");
        p.forceRefreshEveryRequest = fr != null && fr;
        Integer ms = po.getInteger("marginSeconds");
        p.marginSeconds = ms == null ? 30 : ms;
        Integer tt = po.getInteger("manualTtlSeconds");
        p.manualTtlSeconds = tt == null ? 0 : tt;
        return p;
    }

    public void writeTo(PersistedObject po) {
        po.setString("name", name == null ? "" : name);
        if (loginRequest != null) po.setHttpRequest("loginRequest", loginRequest);
        po.setString("scopeHostMatch", scopeHostMatch == null ? "" : scopeHostMatch);
        po.setString("applyToolsCsv", applyToTools.stream().map(Enum::name).collect(Collectors.joining(",")));
        po.setBoolean("enabled", enabled);
        po.setBoolean("autoRetryOn401", autoRetryOn401);
        po.setString("authFailureBodyPattern", authFailureBodyPattern == null ? "" : authFailureBodyPattern);
        po.setBoolean("proactiveJwtRefresh", proactiveJwtRefresh);
        po.setBoolean("forceRefreshEveryRequest", forceRefreshEveryRequest);
        po.setInteger("marginSeconds", marginSeconds);
        po.setInteger("manualTtlSeconds", manualTtlSeconds);

        po.setInteger("ruleCount", rules.size());
        for (int i = 0; i < rules.size(); i++) {
            PersistedObject ruleObj = PersistedObject.persistedObject();
            rules.get(i).writeTo(ruleObj);
            po.setChildObject("rule_" + i, ruleObj);
        }
    }

    private static String orDefault(String v, String def) {
        return (v == null || v.isEmpty()) ? def : v;
    }

    private static <E extends Enum<E>> E safeEnum(Class<E> cls, String v, E def) {
        if (v == null) return def;
        try {
            return Enum.valueOf(cls, v);
        } catch (Exception e) {
            return def;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HttpRequest getLoginRequest() {
        return loginRequest;
    }

    public void setLoginRequest(HttpRequest loginRequest) {
        this.loginRequest = loginRequest;
    }

    public List<TokenRule> getRules() {
        return rules;
    }

    public void setRules(List<TokenRule> rules) {
        this.rules = (rules == null || rules.isEmpty()) ? new ArrayList<>(List.of(defaultRule())) : rules;
    }

    public String getScopeHostMatch() {
        return scopeHostMatch;
    }

    public void setScopeHostMatch(String scopeHostMatch) {
        this.scopeHostMatch = scopeHostMatch;
    }

    public Set<ToolType> getApplyToTools() {
        return applyToTools;
    }

    public void setApplyToTools(Set<ToolType> applyToTools) {
        this.applyToTools = applyToTools;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAutoRetryOn401() {
        return autoRetryOn401;
    }

    public void setAutoRetryOn401(boolean autoRetryOn401) {
        this.autoRetryOn401 = autoRetryOn401;
    }

    public String getAuthFailureBodyPattern() {
        return authFailureBodyPattern;
    }

    public void setAuthFailureBodyPattern(String authFailureBodyPattern) {
        this.authFailureBodyPattern = authFailureBodyPattern;
    }

    public boolean isProactiveJwtRefresh() {
        return proactiveJwtRefresh;
    }

    public void setProactiveJwtRefresh(boolean proactiveJwtRefresh) {
        this.proactiveJwtRefresh = proactiveJwtRefresh;
    }

    public boolean isForceRefreshEveryRequest() {
        return forceRefreshEveryRequest;
    }

    public void setForceRefreshEveryRequest(boolean forceRefreshEveryRequest) {
        this.forceRefreshEveryRequest = forceRefreshEveryRequest;
    }

    public int getMarginSeconds() {
        return marginSeconds;
    }

    public void setMarginSeconds(int marginSeconds) {
        this.marginSeconds = marginSeconds;
    }

    public int getManualTtlSeconds() {
        return manualTtlSeconds;
    }

    public void setManualTtlSeconds(int manualTtlSeconds) {
        this.manualTtlSeconds = manualTtlSeconds;
    }

    public boolean hasAllValues() {
        for (TokenRule r : rules) {
            if (r.getCurrentValue() == null) return false;
        }
        return !rules.isEmpty();
    }

    public boolean hasAnyValue() {
        for (TokenRule r : rules) {
            if (r.getCurrentValue() != null) return true;
        }
        return false;
    }

    public long getLastRefreshedAtEpochMillis() {
        return lastRefreshedAtEpochMillis;
    }

    public void setLastRefreshedAtEpochMillis(long lastRefreshedAtEpochMillis) {
        this.lastRefreshedAtEpochMillis = lastRefreshedAtEpochMillis;
    }

    public Long getTokenExpiresAtEpochMillis() {
        return tokenExpiresAtEpochMillis;
    }

    public void setTokenExpiresAtEpochMillis(Long tokenExpiresAtEpochMillis) {
        this.tokenExpiresAtEpochMillis = tokenExpiresAtEpochMillis;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public int getRefreshCount() {
        return refreshCount;
    }

    public void setRefreshCount(int refreshCount) {
        this.refreshCount = refreshCount;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }
}

