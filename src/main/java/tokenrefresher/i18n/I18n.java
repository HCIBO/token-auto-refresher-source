package tokenrefresher.i18n;

import java.util.HashMap;
import java.util.Map;

public final class I18n {

    public enum Lang {
        TR("Türkçe"), EN("English"), FR("Français");

        public final String displayName;

        Lang(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static volatile Lang current = Lang.TR;

    public static void setLang(Lang l) {
        current = l;
    }

    public static Lang getLang() {
        return current;
    }

    private static final Map<String, String[]> M = new HashMap<>();

    private static void add(String key, String tr, String en, String fr) {
        M.put(key, new String[]{tr, en, fr});
    }

    public static String t(String key) {
        String[] arr = M.get(key);
        if (arr == null) return key;
        int idx = current == Lang.TR ? 0 : current == Lang.EN ? 1 : 2;
        String v = arr[idx];
        return v != null ? v : arr[0];
    }

    public static String t(String key, Object... args) {
        return String.format(t(key), args);
    }

    static {
        
        add("ext.loaded", "Token Auto-Refresher yüklendi. %d profil bulundu.",
                "Token Auto-Refresher loaded. %d profile(s) found.",
                "Token Auto-Refresher chargé. %d profil(s) trouvé(s).");
        add("tab.title", "Token Refresher", "Token Refresher", "Token Refresher");

        add("panel.btnAdd", "Ekle", "Add", "Ajouter");
        add("panel.btnEdit", "Düzenle", "Edit", "Modifier");
        add("panel.btnDelete", "Sil", "Delete", "Supprimer");
        add("panel.btnRefreshNow", "Şimdi yenile", "Refresh now", "Actualiser maintenant");
        add("panel.langLabel", "Dil:", "Language:", "Langue :");
        add("panel.hint",
                "İpucu: Proxy/Repeater geçmişinde bir isteğe sağ tıklayıp \"Token Refresher: Bunu giriş (token) isteği yap\" ile de profil oluşturabilirsin.",
                "Tip: you can also right-click a request in Proxy/Repeater history and choose \"Token Refresher: use as login (token) request\" to create a profile.",
                "Astuce : vous pouvez aussi faire un clic droit sur une requête dans l'historique Proxy/Repeater et choisir « Token Refresher : utiliser comme requête de connexion (jeton) » pour créer un profil.");
        add("panel.confirmDeleteMsg", "'%s' profili silinsin mi?", "Delete profile '%s'?", "Supprimer le profil « %s » ?");
        add("panel.confirmTitle", "Onay", "Confirm", "Confirmation");
        add("panel.refreshPopupTitle", "Şimdi yenile: %s", "Refresh now: %s", "Actualiser maintenant : %s");

        add("panel.col.name", "Ad", "Name", "Nom");
        add("panel.col.host", "Host", "Host", "Hôte");
        add("panel.col.inject", "Enjekte", "Inject", "Injection");
        add("panel.col.status", "Durum", "Status", "État");
        add("panel.col.lastRefresh", "Son yenileme", "Last refresh", "Dernière actualisation");

        add("panel.disabledPrefix", "[kapalı] ", "[disabled] ", "[désactivé] ");
        add("panel.status.refreshing", "yenileniyor...", "refreshing...", "actualisation...");
        add("panel.status.error", "HATA: %s", "ERROR: %s", "ERREUR : %s");
        add("panel.status.noValue", "token henüz alınmadı", "token not fetched yet", "jeton pas encore récupéré");
        add("panel.status.allValues", "tüm değerler var", "all values present", "toutes les valeurs présentes");
        add("panel.status.partialValues", "kısmen alındı", "partially fetched", "partiellement récupéré");
        add("panel.status.refreshCountSuffix", " (#%d yenileme)", " (#%d refresh)", " (#%d actualisation)");
        add("panel.status.expiresIn", ", ~%dsn sonra dolacak", ", expires in ~%ds", ", expire dans ~%ds");
        add("panel.status.expired", ", süresi dolmuş", ", expired", ", expiré");

        add("dlg.title", "Token Profili", "Token Profile", "Profil de jeton");
        add("dlg.name", "Profil adı:", "Profile name:", "Nom du profil :");
        add("dlg.scope", "Kaynak scope (opsiyonel):", "Source scope (optional):", "Portée source (optionnel) :");
        add("dlg.scope.tooltip",
                "Boş = giriş isteğiyle aynı host. Metin = host içinde geçmeli. 're:regex' = regex eşleşmesi.",
                "Empty = same host as the login request. Text = must appear in the host. 're:regex' = regex match.",
                "Vide = même hôte que la requête de connexion. Texte = doit apparaître dans l'hôte. « re:regex » = correspondance regex.");
        add("dlg.tools", "Uygulanacak tool'lar:", "Apply to tools:", "Outils concernés :");

        add("dlg.rules.title",
                "Kurallar — aynı login response'undan çekilecek her ayrı token/değer için bir satır (örn. JWT header + ayrı GUID query param)",
                "Rules — one row per separate token/value extracted from the same login response (e.g. JWT header + separate GUID query param)",
                "Règles — une ligne par jeton/valeur distinct extrait de la même réponse de connexion (ex. en-tête JWT + paramètre GUID séparé)");
        add("dlg.rules.add", "+ Kural ekle", "+ Add rule", "+ Ajouter une règle");
        add("dlg.rules.col.source", "Nereden al", "Extract from", "Extraire de");
        add("dlg.rules.col.path", "Path/isim", "Path/name", "Chemin/nom");
        add("dlg.rules.col.target", "Nereye yaz", "Inject into", "Injecter dans");
        add("dlg.rules.col.name", "Ad", "Name", "Nom");
        add("dlg.rules.col.prefix", "Prefix", "Prefix", "Préfixe");
        add("dlg.rules.remove", "Sil", "Remove", "Supprimer");
        add("dlg.rules.minWarn", "En az bir kural kalmalı.", "At least one rule must remain.", "Il doit rester au moins une règle.");
        add("dlg.warnTitle", "Uyarı", "Warning", "Avertissement");

        add("dlg.enabled", "Aktif", "Enabled", "Activé");
        add("dlg.autoRetry", "Oturum hatası alınca otomatik yenile + isteği tekrar gönder",
                "Auto-refresh + resend the request on an auth failure",
                "Actualiser automatiquement + renvoyer la requête en cas d'échec d'authentification");
        add("dlg.authPattern.label", "  ↳ ek algılama regex (401/403 dışı, body'de ara):",
                "  ↳ extra detection regex (besides 401/403, searched in body):",
                "  ↳ regex de détection supplémentaire (en plus de 401/403, recherché dans le corps) :");
        add("dlg.authPattern.tooltip",
                "Örn: -100006  |  HTTP 200 dönüp gövdede oturum/token hatası yazan API'ler için. Boş = sadece 401/403.",
                "E.g.: -100006  |  For APIs that return HTTP 200 but write a session/token error in the body. Empty = 401/403 only.",
                "Ex. : -100006  |  Pour les API qui renvoient HTTP 200 mais écrivent une erreur de session/jeton dans le corps. Vide = 401/403 seulement.");
        add("dlg.proactiveJwt", "JWT ise exp claim'ini oku, süresi dolmadan önce yenile",
                "If it's a JWT, read the exp claim and refresh before it expires",
                "Si c'est un JWT, lire le claim exp et actualiser avant expiration");
        add("dlg.forceEvery",
                "Her istekte zorla yenile (login isteğini her seferinde çalıştır — yavaş, tek-session sunucularda deneyin)",
                "Force-refresh on every request (runs the login request every time — slow, try on single-session servers)",
                "Forcer l'actualisation à chaque requête (exécute la requête de connexion à chaque fois — lent, à essayer sur les serveurs à session unique)");
        add("dlg.margin.label", "Erken yenileme payı (sn):", "Early refresh margin (sec):", "Marge d'actualisation anticipée (s) :");
        add("dlg.ttl.label", "JWT değilse manuel TTL (sn, 0=kapalı):", "Manual TTL if not JWT (sec, 0=off):", "TTL manuel si non-JWT (s, 0=désactivé) :");
        add("dlg.loginRequestBorder",
                "Giriş / Token üretme isteği (Repeater'daki gibi düzenlenebilir)",
                "Login / token-generating request (editable like in Repeater)",
                "Requête de connexion / génération du jeton (modifiable comme dans Repeater)");
        add("dlg.save", "Kaydet", "Save", "Enregistrer");
        add("dlg.cancel", "İptal", "Cancel", "Annuler");
        add("dlg.err.nameEmpty", "Profil adı boş olamaz.", "Profile name cannot be empty.", "Le nom du profil ne peut pas être vide.");
        add("dlg.err.missingInfo", "Eksik bilgi", "Missing information", "Information manquante");
        add("dlg.err.invalidRequest",
                "Geçerli bir giriş isteği girilmedi (host/port bilgisi eksik olabilir).",
                "No valid login request was entered (host/port info may be missing).",
                "Aucune requête de connexion valide n'a été saisie (les infos hôte/port sont peut-être manquantes).");
        add("dlg.err.ruleNameEmpty", "Bir kuralda enjekte edilecek ad boş.", "One rule has an empty injection name.", "Une règle a un nom d'injection vide.");

        add("menu.registerAsLogin",
                "Token Refresher: Bunu giriş (token) isteği yap",
                "Token Refresher: use as login (token) request",
                "Token Refresher : utiliser comme requête de connexion (jeton)");

        add("enum.extraction.jsonPath", "JSON path", "JSON path", "Chemin JSON");
        add("enum.extraction.regex", "Regex", "Regex", "Regex");
        add("enum.extraction.header", "Header", "Header", "En-tête");
        add("enum.extraction.setCookie", "Set-Cookie", "Set-Cookie", "Set-Cookie");
        add("enum.injection.header", "Header", "Header", "En-tête");
        add("enum.injection.cookie", "Cookie", "Cookie", "Cookie");
        add("enum.injection.queryParam", "Query param", "Query param", "Paramètre URL");

        add("dlg.rules.path.tooltip",
                "JSON path: nokta ile ayrılmış path (örn. data.token) | Regex: body'de 1. grup | Header/Set-Cookie: isim",
                "JSON path: dot-separated path (e.g. data.token) | Regex: group 1 in the body | Header/Set-Cookie: name",
                "Chemin JSON : chemin à points (ex. data.token) | Regex : groupe 1 dans le corps | En-tête/Set-Cookie : nom");
        add("dlg.rules.injName.tooltip",
                "Enjekte edilecek header/cookie/param adı, örn: Authorization",
                "Name of the header/cookie/param to inject, e.g. Authorization",
                "Nom de l'en-tête/cookie/paramètre à injecter, ex. Authorization");
        add("dlg.rules.prefix.tooltip",
                "Header değeri önüne eklenecek metin (örn. 'Bearer '), sadece Header hedefinde kullanılır",
                "Text prepended to the header value (e.g. 'Bearer '), only used for the Header target",
                "Texte ajouté avant la valeur de l'en-tête (ex. « Bearer »), utilisé seulement pour la cible En-tête");

        add("log.inject", "'%s' -> %s %s : %s '%s' = %s", "'%s' -> %s %s : %s '%s' = %s", "« %s » -> %s %s : %s « %s » = %s");
        add("log.multiMatch",
                "Bilgi: bu isteğe %d profil birden eşleşti (aynı host/tool scope). Farklı header/param'lara yazıyorlarsa sorun yok; aynı header/param'a yazıyorlarsa sonuncusu öncekinin üzerine yazar.",
                "Info: %d profiles matched this request (same host/tool scope). Fine if they write to different headers/params; if they write to the same one, the last one wins.",
                "Info : %d profils correspondent à cette requête (même portée hôte/outil). Pas de problème s'ils écrivent dans des en-têtes/paramètres différents ; sinon, le dernier écrase le précédent.");
        add("log.authFailureDetected",
                "'%s' için oturum hatası algılandı (status=%d), tüm kurallar yenileniyor...",
                "Auth failure detected for '%s' (status=%d), refreshing all rules...",
                "Échec d'authentification détecté pour « %s » (statut=%d), actualisation de toutes les règles...");
        add("log.retriedSent",
                "%d alındı, '%s' için token yenilendi ve istek tekrar gönderildi.",
                "Got %d, refreshed the token for '%s' and resent the request.",
                "%d reçu, jeton actualisé pour « %s » et requête renvoyée.");
        add("log.retryFailed", "'%s' profili için tekrar gönderme başarısız", "retry failed for profile '%s'", "échec du renvoi pour le profil « %s »");

        add("err.noLoginRequest", "Giriş (token üretme) isteği tanımlı değil.", "No login (token-generating) request defined.", "Aucune requête de connexion (génération de jeton) définie.");
        add("err.noResponse", "Giriş isteğinden cevap alınamadı.", "No response received from the login request.", "Aucune réponse reçue de la requête de connexion.");
        add("err.ruleNotFound",
                "Kural %s response'ta bulunamadı (extraction ayarlarını kontrol et).",
                "Rule(s) %s not found in the response (check extraction settings).",
                "Règle(s) %s introuvable(s) dans la réponse (vérifiez les paramètres d'extraction).");
        add("log.refreshedOk", "'%s' yenilendi (#%d).", "'%s' refreshed (#%d).", "« %s » actualisé (#%d).");
        add("log.refreshedWarn",
                "'%s' yenilendi (#%d) — UYARI: kural %s çekilemedi",
                "'%s' refreshed (#%d) — WARNING: rule(s) %s could not be fetched",
                "« %s » actualisé (#%d) — AVERTISSEMENT : règle(s) %s non récupérée(s)");
        add("log.refreshError", "'%s' yenileme hatası", "'%s' refresh error", "erreur d'actualisation pour « %s »");

        add("refreshNow.failed", "Yenileme sorunlu: %s", "Refresh had issues: %s", "Actualisation avec problèmes : %s");
        add("refreshNow.header", "Yenilendi:\n", "Refreshed:\n", "Actualisé :\n");
        add("refreshNow.expiresAt", "Bitiş (en erken): %s", "Expires (earliest): %s", "Expiration (la plus proche) : %s");

        add("panel.btnHelp", "Yardım", "Help", "Aide");
        add("help.title", "Nasıl kullanılır", "How to use", "Comment l'utiliser");
        add("help.html",
                "<h2>Nasıl çalışır</h2>"
                        + "<p>Bir \"giriş / token üretme\" isteğini (örn. <code>/api/login</code>) bir kere kaydediyorsun. "
                        + "Extension o isteği arka planda çalıştırıp gelen response'tan token(lar)ı çekiyor ve "
                        + "seçtiğin tool'lardan (Proxy, Repeater, Intruder, Scanner, Extensions) geçen tüm eşleşen "
                        + "isteklere kendisi yazıyor. Token yenilenince tekrar git-bul-değiştir yapmana gerek yok.</p>"
                        + "<h2>Adım adım</h2>"
                        + "<ol>"
                        + "<li><b>Giriş isteğini kaydet:</b> Proxy/Repeater geçmişinde login isteğine sağ tık → "
                        + "\"Token Refresher: Bunu giriş (token) isteği yap\" — ya da bu sekmede <b>Ekle</b>'ye basıp "
                        + "isteği sağdaki editöre kendin yapıştır.</li>"
                        + "<li><b>Kural(lar)ı tanımla:</b> Her kural, response'tan bir değeri (JSON path / regex / "
                        + "header / Set-Cookie ile) çekip bir hedefe (Header / Cookie / URL query param) yazar. "
                        + "Aynı login response'unda birden fazla kimlik bilgisi varsa (örn. JWT header + ayrı bir "
                        + "GUID session token'ı) <b>+ Kural ekle</b> ile ikinci bir satır ekle — ikisi de aynı tek "
                        + "login çağrısından birlikte yenilenir.</li>"
                        + "<li><b>Scope ve tool'ları ayarla:</b> Boş scope = login isteğiyle aynı host. Hangi "
                        + "tool'larda (Proxy/Repeater/Intruder/Scanner/Extensions) uygulanacağını seç.</li>"
                        + "<li><b>Yenileme davranışını ayarla:</b> JWT ise <code>exp</code> claim'i otomatik okunup "
                        + "süresi dolmadan yenilenir. JWT değilse manuel TTL (saniye) verebilirsin. Sunucu oturum "
                        + "hatasını 401/403 yerine <code>HTTP 200</code> + gövdede özel bir hata koduyla veriyorsa "
                        + "(örn. <code>-100006</code>), bunu \"ek algılama regex\" alanına yaz. Tek-session "
                        + "sunucularda (her login öncekini geçersiz kılıyorsa) \"her istekte zorla yenile\" "
                        + "seçeneğini dene.</li>"
                        + "<li><b>Kaydet.</b> Artık token expire olsa bile bir sonraki istekte (ya da oturum hatası "
                        + "algılanınca) extension arka planda kendi kendine tazeleyip isteği tekrar gönderir.</li>"
                        + "</ol>"
                        + "<h2>Sorun giderme</h2>"
                        + "<p><b>Extensions → Token Auto-Refresher → Output/Errors</b> sekmesinde her yenileme ve "
                        + "enjeksiyon için log satırı görürsün — bir şey beklediğin gibi çalışmazsa önce oraya bak.</p>",
                "<h2>How it works</h2>"
                        + "<p>Register a \"login / token-generating\" request (e.g. <code>/api/login</code>) once. "
                        + "The extension runs it in the background, extracts the token(s) from the response, and "
                        + "writes them into every matching outgoing request on the tools you pick (Proxy, Repeater, "
                        + "Intruder, Scanner, Extensions). No more manual find-and-replace once a token expires.</p>"
                        + "<h2>Step by step</h2>"
                        + "<ol>"
                        + "<li><b>Register the login request:</b> right-click a login request in Proxy/Repeater "
                        + "history → \"Token Refresher: use as login (token) request\" — or click <b>Add</b> in this "
                        + "tab and paste the request into the editor on the right yourself.</li>"
                        + "<li><b>Define rule(s):</b> each rule pulls one value out of the response (JSON path / "
                        + "regex / header / Set-Cookie) and writes it into a target (Header / Cookie / URL query "
                        + "param). If the same login response carries more than one credential (e.g. a JWT header "
                        + "plus a separate GUID session token), click <b>+ Add rule</b> for a second row — both are "
                        + "refreshed together from the same single login call.</li>"
                        + "<li><b>Set scope and tools:</b> an empty scope means \"same host as the login request\". "
                        + "Pick which tools (Proxy/Repeater/Intruder/Scanner/Extensions) it applies to.</li>"
                        + "<li><b>Tune refresh behaviour:</b> if it's a JWT, the <code>exp</code> claim is read "
                        + "automatically and it refreshes before expiry. Otherwise set a manual TTL in seconds. If "
                        + "the server signals an expired session with <code>HTTP 200</code> plus an app-level error "
                        + "code instead of 401/403 (e.g. <code>-100006</code>), put that in the \"extra detection "
                        + "regex\" field. On single-session servers (where every new login invalidates the previous "
                        + "one), try \"force-refresh on every request\".</li>"
                        + "<li><b>Save.</b> From now on, even if the token expires, the extension refreshes it and "
                        + "resends the request by itself — on the next request, or the moment an auth failure is "
                        + "detected.</li>"
                        + "</ol>"
                        + "<h2>Troubleshooting</h2>"
                        + "<p>Check <b>Extensions → Token Auto-Refresher → Output/Errors</b> in Burp — every refresh "
                        + "and injection is logged there. Start there if something doesn't behave as expected.</p>",
                "<h2>Fonctionnement</h2>"
                        + "<p>Enregistrez une fois une requête de \"connexion / génération de jeton\" (ex. "
                        + "<code>/api/login</code>). L'extension l'exécute en arrière-plan, extrait le(s) jeton(s) "
                        + "de la réponse, et les écrit dans chaque requête sortante correspondante sur les outils "
                        + "choisis (Proxy, Repeater, Intruder, Scanner, Extensions). Plus besoin de chercher-"
                        + "remplacer à la main quand un jeton expire.</p>"
                        + "<h2>Étape par étape</h2>"
                        + "<ol>"
                        + "<li><b>Enregistrer la requête de connexion :</b> clic droit sur une requête de connexion "
                        + "dans l'historique Proxy/Repeater → « Token Refresher : utiliser comme requête de "
                        + "connexion (jeton) » — ou cliquez sur <b>Ajouter</b> dans cet onglet et collez la requête "
                        + "vous-même dans l'éditeur à droite.</li>"
                        + "<li><b>Définir la/les règle(s) :</b> chaque règle extrait une valeur de la réponse "
                        + "(chemin JSON / regex / en-tête / Set-Cookie) et l'écrit dans une cible (En-tête / Cookie "
                        + "/ paramètre d'URL). Si la même réponse de connexion contient plusieurs identifiants "
                        + "(ex. un en-tête JWT plus un jeton de session GUID séparé), cliquez sur <b>+ Ajouter une "
                        + "règle</b> pour une deuxième ligne — les deux sont actualisés ensemble depuis le même "
                        + "appel de connexion.</li>"
                        + "<li><b>Définir la portée et les outils :</b> une portée vide signifie « même hôte que la "
                        + "requête de connexion ». Choisissez les outils concernés (Proxy/Repeater/Intruder/"
                        + "Scanner/Extensions).</li>"
                        + "<li><b>Régler le comportement d'actualisation :</b> si c'est un JWT, le claim "
                        + "<code>exp</code> est lu automatiquement et actualisé avant expiration. Sinon, définissez "
                        + "un TTL manuel en secondes. Si le serveur signale une session expirée avec "
                        + "<code>HTTP 200</code> plus un code d'erreur applicatif au lieu de 401/403 (ex. "
                        + "<code>-100006</code>), indiquez-le dans le champ « regex de détection supplémentaire ». "
                        + "Sur les serveurs à session unique (où chaque nouvelle connexion invalide la précédente), "
                        + "essayez « forcer l'actualisation à chaque requête ».</li>"
                        + "<li><b>Enregistrer.</b> Désormais, même si le jeton expire, l'extension l'actualise et "
                        + "renvoie la requête elle-même — à la prochaine requête, ou dès qu'un échec "
                        + "d'authentification est détecté.</li>"
                        + "</ol>"
                        + "<h2>Dépannage</h2>"
                        + "<p>Consultez <b>Extensions → Token Auto-Refresher → Output/Errors</b> dans Burp — chaque "
                        + "actualisation et injection y est journalisée. Commencez par là si quelque chose ne se "
                        + "comporte pas comme prévu.</p>");
    }
}

