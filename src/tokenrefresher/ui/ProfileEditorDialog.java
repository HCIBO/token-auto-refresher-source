package tokenrefresher.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import tokenrefresher.i18n.I18n;
import tokenrefresher.model.ExtractionSource;
import tokenrefresher.model.InjectionTarget;
import tokenrefresher.model.TokenProfile;
import tokenrefresher.model.TokenRule;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ProfileEditorDialog extends JDialog {

    private final TokenProfile profile;
    private final Consumer<TokenProfile> onSave;

    private final JTextField nameField = new JTextField();
    private final HttpRequestEditor requestEditor;

    private final JTextField scopeHostField = new JTextField();
    private final JCheckBox toolProxy = new JCheckBox("Proxy", true);
    private final JCheckBox toolRepeater = new JCheckBox("Repeater", true);
    private final JCheckBox toolIntruder = new JCheckBox("Intruder", true);
    private final JCheckBox toolScanner = new JCheckBox("Scanner", true);
    private final JCheckBox toolExtensions = new JCheckBox("Extensions", true);

    private final JCheckBox enabledBox = new JCheckBox(I18n.t("dlg.enabled"), true);
    private final JCheckBox autoRetryBox = new JCheckBox(I18n.t("dlg.autoRetry"), true);
    private final JTextField authFailurePatternField = new JTextField();
    private final JCheckBox proactiveJwtBox = new JCheckBox(I18n.t("dlg.proactiveJwt"), true);
    private final JCheckBox forceEveryRequestBox = new JCheckBox(I18n.t("dlg.forceEvery"), false);
    private final JSpinner marginSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 3600, 5));
    private final JSpinner ttlSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 86400, 30));

    private final JPanel rulesBox = new JPanel();
    private final List<RuleRow> ruleRows = new ArrayList<>();

    public ProfileEditorDialog(MontoyaApi api, Window owner, TokenProfile profile, Consumer<TokenProfile> onSave) {
        super(owner, I18n.t("dlg.title"), ModalityType.APPLICATION_MODAL);
        this.profile = profile;
        this.onSave = onSave;
        this.requestEditor = api.userInterface().createHttpRequestEditor();

        setLayout(new BorderLayout(8, 8));
        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadFromProfile();

        setSize(980, 800);
        setLocationRelativeTo(owner);
    }

    private JComponent buildForm() {
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        addRow(top, c, row++, I18n.t("dlg.name"), nameField);

        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        top.add(new JLabel(I18n.t("dlg.scope")), c);
        c.gridx = 1; c.gridy = row; c.gridwidth = 2; c.weightx = 1;
        scopeHostField.setToolTipText(I18n.t("dlg.scope.tooltip"));
        top.add(scopeHostField, c);
        row++;

        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        top.add(new JLabel(I18n.t("dlg.tools")), c);
        c.gridx = 1; c.gridy = row; c.gridwidth = 2; c.weightx = 1;
        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolsPanel.add(toolProxy);
        toolsPanel.add(toolRepeater);
        toolsPanel.add(toolIntruder);
        toolsPanel.add(toolScanner);
        toolsPanel.add(toolExtensions);
        top.add(toolsPanel, c);
        row++;

        c.gridx = 0; c.gridy = row; c.gridwidth = 3; c.weightx = 1;
        JPanel rulesHeader = new JPanel(new BorderLayout());
        rulesHeader.setBorder(BorderFactory.createTitledBorder(I18n.t("dlg.rules.title")));
        JButton addRule = new JButton(I18n.t("dlg.rules.add"));
        addRule.addActionListener(e -> addRule(new TokenRule()));
        JPanel addRuleBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addRuleBar.add(addRule);
        rulesBox.setLayout(new BoxLayout(rulesBox, BoxLayout.Y_AXIS));
        JPanel rulesWrap = new JPanel(new BorderLayout());
        rulesWrap.add(rulesHeaderRow(), BorderLayout.NORTH);
        rulesWrap.add(rulesBox, BorderLayout.CENTER);
        rulesWrap.add(addRuleBar, BorderLayout.SOUTH);
        rulesHeader.add(rulesWrap, BorderLayout.CENTER);
        top.add(rulesHeader, c);
        row++;

        c.gridx = 0; c.gridy = row; c.gridwidth = 3; c.weightx = 1;
        top.add(enabledBox, c);
        row++;
        c.gridy = row++;
        top.add(autoRetryBox, c);

        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        top.add(new JLabel(I18n.t("dlg.authPattern.label")), c);
        c.gridx = 1; c.gridy = row; c.gridwidth = 2; c.weightx = 1;
        authFailurePatternField.setToolTipText(I18n.t("dlg.authPattern.tooltip"));
        top.add(authFailurePatternField, c);
        row++;

        c.gridx = 0; c.gridwidth = 3; c.weightx = 1;
        c.gridy = row++;
        top.add(proactiveJwtBox, c);
        c.gridy = row++;
        top.add(forceEveryRequestBox, c);

        c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        top.add(new JLabel(I18n.t("dlg.margin.label")), c);
        c.gridx = 1; c.weightx = 0.3;
        top.add(marginSpinner, c);
        c.gridx = 2; c.weightx = 0;
        top.add(new JLabel(I18n.t("dlg.ttl.label")), c);
        row++;
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        top.add(new JLabel(""), c);
        c.gridx = 1; c.gridy = row; c.weightx = 0.3;
        top.add(ttlSpinner, c);

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.add(new JScrollPane(top, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.NORTH);

        JPanel reqPanel = new JPanel(new BorderLayout());
        reqPanel.setBorder(BorderFactory.createTitledBorder(I18n.t("dlg.loginRequestBorder")));
        reqPanel.add(requestEditor.uiComponent(), BorderLayout.CENTER);
        wrapper.add(reqPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private static final int COL_SOURCE_W = 100;
    private static final int COL_TARGET_W = 100;
    private static final int COL_PREFIX_W = 70;
    private static final int COL_REMOVE_W = 60;

    private static JPanel layoutRuleRow(Component source, Component path, Component target,
                                         Component name, Component prefix, Component remove) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1, 2, 1, 2);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;

        c.gridx = 0; c.weightx = 0;
        source.setPreferredSize(new Dimension(COL_SOURCE_W, source.getPreferredSize().height));
        p.add(source, c);

        c.gridx = 1; c.weightx = 1;
        p.add(path, c);

        c.gridx = 2; c.weightx = 0;
        target.setPreferredSize(new Dimension(COL_TARGET_W, target.getPreferredSize().height));
        p.add(target, c);

        c.gridx = 3; c.weightx = 1;
        p.add(name, c);

        c.gridx = 4; c.weightx = 0;
        prefix.setPreferredSize(new Dimension(COL_PREFIX_W, prefix.getPreferredSize().height));
        p.add(prefix, c);

        c.gridx = 5; c.weightx = 0;
        remove.setPreferredSize(new Dimension(COL_REMOVE_W, remove.getPreferredSize().height));
        p.add(remove, c);

        return p;
    }

    private JPanel rulesHeaderRow() {
        return layoutRuleRow(
                new JLabel(I18n.t("dlg.rules.col.source")),
                new JLabel(I18n.t("dlg.rules.col.path")),
                new JLabel(I18n.t("dlg.rules.col.target")),
                new JLabel(I18n.t("dlg.rules.col.name")),
                new JLabel(I18n.t("dlg.rules.col.prefix")),
                new JLabel(""));
    }

    private void addRule(TokenRule rule) {
        RuleRow row = new RuleRow(rule);
        ruleRows.add(row);
        rulesBox.add(row.panel);
        rulesBox.revalidate();
        rulesBox.repaint();
    }

    private void removeRule(RuleRow row) {
        if (ruleRows.size() <= 1) {
            JOptionPane.showMessageDialog(this, I18n.t("dlg.rules.minWarn"), I18n.t("dlg.warnTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        ruleRows.remove(row);
        rulesBox.remove(row.panel);
        rulesBox.revalidate();
        rulesBox.repaint();
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.gridwidth = 2; c.weightx = 1;
        panel.add(field, c);
    }

    private JComponent buildButtons() {
        JButton save = new JButton(I18n.t("dlg.save"));
        JButton cancel = new JButton(I18n.t("dlg.cancel"));
        save.addActionListener(e -> onSaveClicked());
        cancel.addActionListener(e -> dispose());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(cancel);
        p.add(save);
        return p;
    }

    private void loadFromProfile() {
        nameField.setText(profile.getName());
        if (profile.getLoginRequest() != null) {
            requestEditor.setRequest(profile.getLoginRequest());
        }
        scopeHostField.setText(profile.getScopeHostMatch());

        Set<ToolType> tools = profile.getApplyToTools();
        toolProxy.setSelected(tools.contains(ToolType.PROXY));
        toolRepeater.setSelected(tools.contains(ToolType.REPEATER));
        toolIntruder.setSelected(tools.contains(ToolType.INTRUDER));
        toolScanner.setSelected(tools.contains(ToolType.SCANNER));
        toolExtensions.setSelected(tools.contains(ToolType.EXTENSIONS));

        enabledBox.setSelected(profile.isEnabled());
        autoRetryBox.setSelected(profile.isAutoRetryOn401());
        authFailurePatternField.setText(profile.getAuthFailureBodyPattern());
        proactiveJwtBox.setSelected(profile.isProactiveJwtRefresh());
        forceEveryRequestBox.setSelected(profile.isForceRefreshEveryRequest());
        marginSpinner.setValue(profile.getMarginSeconds());
        ttlSpinner.setValue(profile.getManualTtlSeconds());

        for (TokenRule r : profile.getRules()) {
            addRule(r);
        }
    }

    private void onSaveClicked() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, I18n.t("dlg.err.nameEmpty"), I18n.t("dlg.err.missingInfo"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        HttpRequest req = requestEditor.getRequest();
        if (req == null || req.httpService() == null) {
            JOptionPane.showMessageDialog(this,
                    I18n.t("dlg.err.invalidRequest"),
                    I18n.t("dlg.err.missingInfo"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<TokenRule> rules = new ArrayList<>();
        for (RuleRow row : ruleRows) {
            TokenRule r = row.toRule();
            if (r.getInjectionName() == null || r.getInjectionName().isBlank()) {
                JOptionPane.showMessageDialog(this, I18n.t("dlg.err.ruleNameEmpty"), I18n.t("dlg.err.missingInfo"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            rules.add(r);
        }

        profile.setName(name);
        profile.setLoginRequest(req);
        profile.setRules(rules);
        profile.setScopeHostMatch(scopeHostField.getText().trim());

        Set<ToolType> tools = new LinkedHashSet<>();
        if (toolProxy.isSelected()) tools.add(ToolType.PROXY);
        if (toolRepeater.isSelected()) tools.add(ToolType.REPEATER);
        if (toolIntruder.isSelected()) tools.add(ToolType.INTRUDER);
        if (toolScanner.isSelected()) tools.add(ToolType.SCANNER);
        if (toolExtensions.isSelected()) tools.add(ToolType.EXTENSIONS);
        profile.setApplyToTools(tools);

        profile.setEnabled(enabledBox.isSelected());
        profile.setAutoRetryOn401(autoRetryBox.isSelected());
        profile.setAuthFailureBodyPattern(authFailurePatternField.getText().trim());
        profile.setProactiveJwtRefresh(proactiveJwtBox.isSelected());
        profile.setForceRefreshEveryRequest(forceEveryRequestBox.isSelected());
        profile.setMarginSeconds((Integer) marginSpinner.getValue());
        profile.setManualTtlSeconds((Integer) ttlSpinner.getValue());

        onSave.accept(profile);
        dispose();
    }

    private class RuleRow {
        final JComboBox<ExtractionSource> extractionSourceCombo = new JComboBox<>(ExtractionSource.values());
        final JTextField extractionPathField = new JTextField();
        final JComboBox<InjectionTarget> injectionTargetCombo = new JComboBox<>(InjectionTarget.values());
        final JTextField injectionNameField = new JTextField();
        final JTextField injectionPrefixField = new JTextField();
        final JPanel panel;

        RuleRow(TokenRule rule) {
            extractionSourceCombo.setSelectedItem(rule.getExtractionSource());
            extractionPathField.setText(rule.getExtractionPath());
            extractionPathField.setToolTipText(I18n.t("dlg.rules.path.tooltip"));
            injectionTargetCombo.setSelectedItem(rule.getInjectionTarget());
            injectionNameField.setText(rule.getInjectionName());
            injectionNameField.setToolTipText(I18n.t("dlg.rules.injName.tooltip"));
            injectionPrefixField.setText(rule.getInjectionPrefix());
            injectionPrefixField.setToolTipText(I18n.t("dlg.rules.prefix.tooltip"));

            JButton remove = new JButton(I18n.t("dlg.rules.remove"));
            remove.addActionListener(e -> removeRule(this));

            panel = layoutRuleRow(extractionSourceCombo, extractionPathField, injectionTargetCombo,
                    injectionNameField, injectionPrefixField, remove);
        }

        TokenRule toRule() {
            TokenRule r = new TokenRule();
            r.setExtractionSource((ExtractionSource) extractionSourceCombo.getSelectedItem());
            r.setExtractionPath(extractionPathField.getText().trim());
            r.setInjectionTarget((InjectionTarget) injectionTargetCombo.getSelectedItem());
            r.setInjectionName(injectionNameField.getText().trim());
            r.setInjectionPrefix(injectionPrefixField.getText());
            return r;
        }
    }
}

