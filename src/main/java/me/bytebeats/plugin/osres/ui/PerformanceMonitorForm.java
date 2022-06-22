package me.bytebeats.plugin.osres.ui;

import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.registry.RegistryValue;

import javax.swing.*;

public class PerformanceMonitorForm {
    private JPanel root;

    private JTextField maxAttemptsField;
    private JTextField attemptIntervalField;
    private JTextField samplingIntervalField;
    private JButton resetBtn;
    private com.intellij.openapi.ui.ex.MultiLineLabel label;
    private JLabel samplingIntervalLabel;
    private JLabel samplingIntervalExplainLabel;
    private JLabel maxAttemptLabel;
    private JLabel attemptIntervalLabel;
    private JLabel attemptIntervalExplainLabel;
    private JLabel maxAttemptsExplainLabel;
    private JLabel providerLabel;


    private final RegistryValue ATTEMPTS = Registry.get("performance.watcher.unresponsive.max.attempts.before.log");
    private final RegistryValue UNRESPONSIVE_INTERVAL_MS = Registry.get("performance.watcher.unresponsive.interval.ms");
    private final RegistryValue SAMPLING_INTERVAL_MS = Registry.get("performance.watcher.sampling.interval.ms");


    public PerformanceMonitorForm() {
        label.setText("For experts only:\nconfiguration for Performance monitor (IDE's bundled automatic thread dumper for frozen UI)\n - you can configure it to dump even for shorter freezes than is the default 5s.");

        init();

        resetBtn.addActionListener(e -> {
            ATTEMPTS.resetToDefault();
            UNRESPONSIVE_INTERVAL_MS.resetToDefault();
            SAMPLING_INTERVAL_MS.resetToDefault();
            init();

        });
    }

    private void init() {
        int attempts = ATTEMPTS.asInteger();
        int unresponsiveInterval = UNRESPONSIVE_INTERVAL_MS.asInteger();
        int samplingInterval = SAMPLING_INTERVAL_MS.asInteger();

        maxAttemptsField.setText(String.valueOf(attempts));
        attemptIntervalField.setText(String.valueOf(unresponsiveInterval));
        samplingIntervalField.setText(String.valueOf(samplingInterval));
    }

    public JPanel getRoot() {
        return root;
    }

    public boolean isModified() {
        int attempts = ATTEMPTS.asInteger();
        int unresponsiveInterval = UNRESPONSIVE_INTERVAL_MS.asInteger();
        int samplingInterval = SAMPLING_INTERVAL_MS.asInteger();

        boolean modified = false;
        try {
            modified = attempts != Integer.parseInt(maxAttemptsField.getText()) ||
                    unresponsiveInterval != Integer.parseInt(attemptIntervalField.getText()) ||
                    samplingInterval != Integer.parseInt(samplingIntervalField.getText());
        } catch (NumberFormatException ignored) {
        }
        return modified;
    }

    public void apply() {
        int attempts = Integer.parseInt(maxAttemptsField.getText());
        int unresponsiveInterval = Integer.parseInt(attemptIntervalField.getText());
        int samplingInterval = Integer.parseInt(samplingIntervalField.getText());

        if (attempts < 0 || unresponsiveInterval < 0 || samplingInterval < 0) {
            throw new RuntimeException("Invalid values");
        }

        ATTEMPTS.setValue(attempts);
        UNRESPONSIVE_INTERVAL_MS.setValue(unresponsiveInterval);
        SAMPLING_INTERVAL_MS.setValue(samplingInterval);
    }
}
