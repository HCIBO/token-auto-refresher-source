package tokenrefresher;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import tokenrefresher.core.RefresherHttpHandler;
import tokenrefresher.i18n.I18n;
import tokenrefresher.store.ProfileRegistry;
import tokenrefresher.ui.RegisterLoginMenu;
import tokenrefresher.ui.TokenRefresherPanel;

public class TokenRefresherExtension implements BurpExtension {

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Token Auto-Refresher");

        String savedLang = api.persistence().extensionData().getString("uiLanguage");
        if (savedLang != null) {
            try {
                I18n.setLang(I18n.Lang.valueOf(savedLang));
            } catch (Exception ignored) {
            }
        }

        ProfileRegistry registry = new ProfileRegistry(api);
        RefresherHttpHandler handler = new RefresherHttpHandler(api, registry);
        api.http().registerHttpHandler(handler);

        TokenRefresherPanel panel = new TokenRefresherPanel(api, registry, handler);
        registry.addListener(panel::refresh);

        api.userInterface().registerSuiteTab(I18n.t("tab.title"), panel);
        api.userInterface().registerContextMenuItemsProvider(new RegisterLoginMenu(api, registry, panel));

        api.extension().registerUnloadingHandler(() -> {
            handler.shutdown();
            api.logging().logToOutput(I18n.t("ext.unloaded"));
        });

        api.logging().logToOutput(I18n.t("ext.loaded", registry.all().size()));
    }
}

