package tokenrefresher.store;

import burp.api.montoya.MontoyaApi;
import tokenrefresher.model.TokenProfile;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProfileRegistry {

    private final ProfileStore store;
    private final List<TokenProfile> profiles = new CopyOnWriteArrayList<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public ProfileRegistry(MontoyaApi api) {
        this.store = new ProfileStore(api);
        this.profiles.addAll(store.loadAll());
    }

    public List<TokenProfile> all() {
        return profiles;
    }

    public void add(TokenProfile profile) {
        profiles.add(profile);
        store.save(profile);
        fireChanged();
    }

    public void update(TokenProfile profile) {
        store.save(profile);
        fireChanged();
    }

    public void remove(TokenProfile profile) {
        profiles.remove(profile);
        store.delete(profile.getId());
        fireChanged();
    }

    public void addListener(Runnable r) {
        listeners.add(r);
    }

    public void fireChanged() {
        for (Runnable r : listeners) {
            SwingUtilities.invokeLater(r);
        }
    }
}

