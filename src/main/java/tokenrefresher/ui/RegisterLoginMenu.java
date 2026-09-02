package tokenrefresher.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import tokenrefresher.i18n.I18n;
import tokenrefresher.model.TokenProfile;
import tokenrefresher.store.ProfileRegistry;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RegisterLoginMenu implements ContextMenuItemsProvider {

    private final MontoyaApi api;
    private final ProfileRegistry registry;
    private final TokenRefresherPanel panel;

    public RegisterLoginMenu(MontoyaApi api, ProfileRegistry registry, TokenRefresherPanel panel) {
        this.api = api;
        this.registry = registry;
        this.panel = panel;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        HttpRequest req = null;
        if (event.messageEditorRequestResponse().isPresent()) {
            req = event.messageEditorRequestResponse().get().requestResponse().request();
        } else if (!event.selectedRequestResponses().isEmpty()) {
            req = event.selectedRequestResponses().get(0).request();
        }
        if (req == null) return List.of();

        HttpRequest capturedRequest = req;
        JMenuItem item = new JMenuItem(I18n.t("menu.registerAsLogin"));
        item.addActionListener(e -> {
            TokenProfile p = new TokenProfile();
            if (capturedRequest.httpService() != null) {
                p.setName(capturedRequest.httpService().host());
            }
            p.setLoginRequest(capturedRequest);
            ProfileEditorDialog dialog = new ProfileEditorDialog(
                    api, SwingUtilities.getWindowAncestor(panel), p, registry::add);
            dialog.setVisible(true);
        });
        return List.of(item);
    }
}

