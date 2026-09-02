package tokenrefresher.model;

import tokenrefresher.i18n.I18n;

public enum ExtractionSource {
    JSON_PATH("enum.extraction.jsonPath"),
    REGEX("enum.extraction.regex"),
    HEADER("enum.extraction.header"),
    SET_COOKIE("enum.extraction.setCookie");

    private final String i18nKey;

    ExtractionSource(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    @Override
    public String toString() {
        return I18n.t(i18nKey);
    }
}

