package tokenrefresher.store;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedObject;
import tokenrefresher.model.TokenProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProfileStore {

    private final PersistedObject profilesRoot;

    public ProfileStore(MontoyaApi api) {
        PersistedObject extData = api.persistence().extensionData();
        PersistedObject profiles = extData.getChildObject("profiles");
        if (profiles == null) {
            profiles = PersistedObject.persistedObject();
            extData.setChildObject("profiles", profiles);
        }
        this.profilesRoot = profiles;
    }

    public List<TokenProfile> loadAll() {
        List<TokenProfile> list = new ArrayList<>();
        for (String id : profilesRoot.childObjectKeys()) {
            PersistedObject po = profilesRoot.getChildObject(id);
            if (po != null) {
                list.add(TokenProfile.fromPersisted(id, po));
            }
        }
        list.sort(Comparator.comparing(TokenProfile::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public void save(TokenProfile profile) {
        PersistedObject po = profilesRoot.getChildObject(profile.getId());
        if (po == null) {
            po = PersistedObject.persistedObject();
        }
        profile.writeTo(po);
        profilesRoot.setChildObject(profile.getId(), po);
    }

    public void delete(String id) {
        profilesRoot.deleteChildObject(id);
    }
}

