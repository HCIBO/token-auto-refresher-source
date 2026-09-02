package tokenrefresher.model;

import burp.api.montoya.persistence.PersistedObject;

public class TokenRule {

    private ExtractionSource extractionSource = ExtractionSource.JSON_PATH;
    private String extractionPath = "";

    private InjectionTarget injectionTarget = InjectionTarget.HEADER;
    private String injectionName = "Authorization";
    private String injectionPrefix = "Bearer ";

    private transient volatile String currentValue;

    public static TokenRule fromPersisted(PersistedObject po) {
        TokenRule r = new TokenRule();
        r.extractionSource = safeEnum(ExtractionSource.class, po.getString("extractionSource"), ExtractionSource.JSON_PATH);
        r.extractionPath = orDefault(po.getString("extractionPath"), "");
        r.injectionTarget = safeEnum(InjectionTarget.class, po.getString("injectionTarget"), InjectionTarget.HEADER);
        r.injectionName = orDefault(po.getString("injectionName"), "Authorization");
        r.injectionPrefix = orDefault(po.getString("injectionPrefix"), "");
        return r;
    }

    public void writeTo(PersistedObject po) {
        po.setString("extractionSource", extractionSource.name());
        po.setString("extractionPath", extractionPath == null ? "" : extractionPath);
        po.setString("injectionTarget", injectionTarget.name());
        po.setString("injectionName", injectionName == null ? "" : injectionName);
        po.setString("injectionPrefix", injectionPrefix == null ? "" : injectionPrefix);
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

    public ExtractionSource getExtractionSource() {
        return extractionSource;
    }

    public void setExtractionSource(ExtractionSource extractionSource) {
        this.extractionSource = extractionSource;
    }

    public String getExtractionPath() {
        return extractionPath;
    }

    public void setExtractionPath(String extractionPath) {
        this.extractionPath = extractionPath;
    }

    public InjectionTarget getInjectionTarget() {
        return injectionTarget;
    }

    public void setInjectionTarget(InjectionTarget injectionTarget) {
        this.injectionTarget = injectionTarget;
    }

    public String getInjectionName() {
        return injectionName;
    }

    public void setInjectionName(String injectionName) {
        this.injectionName = injectionName;
    }

    public String getInjectionPrefix() {
        return injectionPrefix;
    }

    public void setInjectionPrefix(String injectionPrefix) {
        this.injectionPrefix = injectionPrefix;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }
}

