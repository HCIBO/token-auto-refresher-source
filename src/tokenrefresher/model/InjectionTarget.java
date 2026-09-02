package tokenrefresher.model;

import tokenrefresher.i18n.I18n;

public enum InjectionTarget {
    HEADER("enum.injection.header"),
    COOKIE("enum.injection.cookie"),
    QUERY_PARAM("enum.injection.queryParam");

    private final String i18nKey;

    InjectionTarget(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    @Override
    public String toString() {
        return I18n.t(i18nKey);
    }
}

