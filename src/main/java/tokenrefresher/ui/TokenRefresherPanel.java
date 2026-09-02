package tokenrefresher.ui;

import burp.api.montoya.MontoyaApi;
import tokenrefresher.core.RefresherHttpHandler;
import tokenrefresher.i18n.I18n;
import tokenrefresher.model.TokenProfile;
import tokenrefresher.model.TokenRule;
import tokenrefresher.store.ProfileRegistry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TokenRefresherPanel extends JPanel {

    private final MontoyaApi api;
    private final ProfileRegistry registry;
    private final RefresherHttpHandler handler;
    private final ProfileTableModel tableModel;
    private final JTable table;
    private final JLabel hint = new JLabel();

    public TokenRefresherPanel(MontoyaApi api, ProfileRegistry registry, RefresherHttpHandler handler) {
        this.api = api;
        this.registry = registry;
        this.handler = handler;

        String savedLang = api.persistence().extensionData().getString("uiLanguage");
        if (savedLang != null) {
            try {
                I18n.setLang(I18n.Lang.valueOf(savedLang));
            } catch (Exception ignored) {
            }
        }

        this.tableModel = new ProfileTableModel();
        this.table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(320);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.NORTH);

        hint.setForeground(Color.GRAY);
        add(hint, BorderLayout.SOUTH);

        applyLanguageTexts();
    }

    private JComponent buildToolbar() {
        JButton add = new JButton();
        JButton edit = new JButton();
        JButton delete = new JButton();
        JButton refreshNow = new JButton();
        JButton help = new JButton();
        JLabel langLabel = new JLabel();
        JComboBox<I18n.Lang> langCombo = new JComboBox<>(I18n.Lang.values());
        langCombo.setSelectedItem(I18n.getLang());
        langCombo.addActionListener(e -> {
            I18n.Lang chosen = (I18n.Lang) langCombo.getSelectedItem();
            I18n.setLang(chosen);
            api.persistence().extensionData().setString("uiLanguage", chosen.name());
            applyLanguageTexts();
            add.setText(I18n.t("panel.btnAdd"));
            edit.setText(I18n.t("panel.btnEdit"));
            delete.setText(I18n.t("panel.btnDelete"));
            refreshNow.setText(I18n.t("panel.btnRefreshNow"));
            help.setText(I18n.t("panel.btnHelp"));
            langLabel.setText(I18n.t("panel.langLabel"));
        });

        add.setText(I18n.t("panel.btnAdd"));
        edit.setText(I18n.t("panel.btnEdit"));
        delete.setText(I18n.t("panel.btnDelete"));
        refreshNow.setText(I18n.t("panel.btnRefreshNow"));
        help.setText(I18n.t("panel.btnHelp"));
        langLabel.setText(I18n.t("panel.langLabel"));

        add.addActionListener(e -> openEditor(new TokenProfile(), true));
        edit.addActionListener(e -> selected().ifPresent(p -> openEditor(p, false)));
        delete.addActionListener(e -> selected().ifPresent(this::confirmDelete));
        refreshNow.addActionListener(e -> selected().ifPresent(p -> {
            new Thread(() -> {
                String result = handler.refreshNow(p);
                registry.fireChanged();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(api.userInterface().swingUtils().suiteFrame(), result, I18n.t("panel.refreshPopupTitle", p.getName()),
                                p.getLastError() != null ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE));
            }, "token-refresher-manual-refresh").start();
        }));
        help.addActionListener(e -> showHelp());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(add);
        bar.add(edit);
        bar.add(delete);
        bar.add(refreshNow);
        bar.add(help);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(langLabel);
        bar.add(langCombo);
        return bar;
    }

    private void showHelp() {
        JEditorPane pane = new JEditorPane("text/html", "<html><body style='font-family:sans-serif;'>" + I18n.t("help.html") + "</body></html>");
        pane.setEditable(false);
        pane.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(pane);
        scroll.setPreferredSize(new Dimension(640, 520));
        JOptionPane.showMessageDialog(api.userInterface().swingUtils().suiteFrame(), scroll, I18n.t("help.title"), JOptionPane.PLAIN_MESSAGE);
    }

    private void applyLanguageTexts() {
        hint.setText(I18n.t("panel.hint"));
        tableModel.fireTableStructureChanged();
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(320);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
    }

    private java.util.Optional<TokenProfile> selected() {
        int row = table.getSelectedRow();
        if (row < 0) return java.util.Optional.empty();
        int modelRow = table.convertRowIndexToModel(row);
        return java.util.Optional.of(registry.all().get(modelRow));
    }

    private void confirmDelete(TokenProfile p) {
        int result = JOptionPane.showConfirmDialog(api.userInterface().swingUtils().suiteFrame(),
                I18n.t("panel.confirmDeleteMsg", p.getName()), I18n.t("panel.confirmTitle"), JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            registry.remove(p);
        }
    }

    public void openEditor(TokenProfile profile, boolean isNew) {
        ProfileEditorDialog dialog = new ProfileEditorDialog(api, api.userInterface().swingUtils().suiteFrame(), profile, saved -> {
            if (isNew) {
                registry.add(saved);
            } else {
                registry.update(saved);
            }
        });
        dialog.setVisible(true);
    }

    public void refresh() {
        tableModel.fireTableDataChanged();
    }

    private class ProfileTableModel extends AbstractTableModel {
        private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");

        @Override
        public int getRowCount() {
            return registry.all().size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0: return I18n.t("panel.col.name");
                case 1: return I18n.t("panel.col.host");
                case 2: return I18n.t("panel.col.inject");
                case 3: return I18n.t("panel.col.status");
                case 4: return I18n.t("panel.col.lastRefresh");
                default: return "";
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            List<TokenProfile> profiles = registry.all();
            if (rowIndex >= profiles.size()) return "";
            TokenProfile p = profiles.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return (p.isEnabled() ? "" : I18n.t("panel.disabledPrefix")) + p.getName();
                case 1:
                    return p.getLoginRequest() != null && p.getLoginRequest().httpService() != null
                            ? p.getLoginRequest().httpService().host() : "-";
                case 2:
                    return injectSummary(p);
                case 3:
                    return status(p);
                case 4:
                    return p.getLastRefreshedAtEpochMillis() == 0 ? "-" : fmt.format(new Date(p.getLastRefreshedAtEpochMillis()));
                default:
                    return "";
            }
        }

        private String injectSummary(TokenProfile p) {
            List<TokenRule> rules = p.getRules();
            if (rules.size() == 1) {
                TokenRule r = rules.get(0);
                return r.getInjectionTarget() + ": " + r.getInjectionName();
            }
            StringBuilder sb = new StringBuilder();
            for (TokenRule r : rules) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(r.getInjectionTarget()).append(":").append(r.getInjectionName());
            }
            return sb.toString();
        }

        private String status(TokenProfile p) {
            if (p.isRefreshing()) return I18n.t("panel.status.refreshing");
            if (p.getLastError() != null) return I18n.t("panel.status.error", p.getLastError());
            if (!p.hasAnyValue()) return I18n.t("panel.status.noValue");
            String s = (p.hasAllValues() ? I18n.t("panel.status.allValues") : I18n.t("panel.status.partialValues"))
                    + I18n.t("panel.status.refreshCountSuffix", p.getRefreshCount());
            Long exp = p.getTokenExpiresAtEpochMillis();
            if (exp != null) {
                long remain = (exp - System.currentTimeMillis()) / 1000;
                s += remain > 0 ? I18n.t("panel.status.expiresIn", remain) : I18n.t("panel.status.expired");
            }
            return s;
        }
    }
}

