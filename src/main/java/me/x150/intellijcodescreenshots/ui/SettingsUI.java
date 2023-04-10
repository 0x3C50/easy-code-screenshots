package me.x150.intellijcodescreenshots.ui;

import com.intellij.ui.ColorChooser;
import me.x150.intellijcodescreenshots.OptionsServiceProvider;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.Color;
import java.util.Locale;

public class SettingsUI {
    private static final double SLIDER_SCALE = 0.25;
    private JPanel myWholePanel;
    private JSlider scaleInp;
    private JLabel dataVis;
    private JCheckBox removeIndent;
    private JSlider innerPaddingInp;
    private JLabel innerPaddingVis;
    private JSlider outerPaddingInp;
    private JLabel outerPaddingVis;
    private JSlider windowRoundnessInp;
    private JLabel roundnessVis;
    private JCheckBox showWindowControls;
    private JFormattedTextField ABB8C3FormattedTextField;
    private JButton editButton;
    private Color initialBgColor = new Color(171, 184, 195);

    public void init() {
        scaleInp.addChangeListener(e -> dataVis.setText(String.format(Locale.ENGLISH, "%.2f", scaleInp.getValue() * SLIDER_SCALE)));
        innerPaddingInp.addChangeListener(e -> innerPaddingVis.setText(String.valueOf(innerPaddingInp.getValue())));
        outerPaddingInp.addChangeListener(e -> outerPaddingVis.setText(String.valueOf(outerPaddingInp.getValue())));
        windowRoundnessInp.addChangeListener(e -> roundnessVis.setText(String.valueOf(windowRoundnessInp.getValue())));
        editButton.addActionListener(e -> {
            initialBgColor = ColorChooser.chooseColor(editButton, "Choose a Color", initialBgColor, true);
            updateJF();
        });
        updateJF();
    }

    private void updateJF() {
        String fmted = String.format(Locale.ENGLISH, "A: %02.0f%%, R: %02.0f%%, G: %02.0f%%, B: %02.0f%%", initialBgColor.getAlpha() / 255f * 100, initialBgColor.getRed() / 255f * 100, initialBgColor.getGreen() / 255f * 100, initialBgColor.getBlue() / 255f * 100);
        ABB8C3FormattedTextField.setText(fmted);
        ABB8C3FormattedTextField.setForeground(new Color(initialBgColor.getRed(), initialBgColor.getGreen(), initialBgColor.getBlue()));
    }

    public OptionsServiceProvider.State toState() {
        OptionsServiceProvider.State s = new OptionsServiceProvider.State();
        s.scale = scaleInp.getValue() * SLIDER_SCALE;
        s.removeIndentation = removeIndent.isSelected();
        s.innerPadding = innerPaddingInp.getValue();
        s.outerPaddingVert = s.outerPaddingHoriz = outerPaddingInp.getValue();
        s.windowRoundness = windowRoundnessInp.getValue();
        s.showWindowControls = showWindowControls.isSelected();
        s.backgroundColor = this.initialBgColor.getRGB();
        return s;
    }

    public JPanel getMyWholePanel() {
        return myWholePanel;
    }

    public void fromState(OptionsServiceProvider.State state) {
        this.scaleInp.setValue((int) Math.round(state.scale / SLIDER_SCALE));
        this.removeIndent.setSelected(state.removeIndentation);
        this.innerPaddingInp.setValue((int) Math.round(state.innerPadding));
        this.outerPaddingInp.setValue((int) Math.round(state.outerPaddingHoriz));
        this.windowRoundnessInp.setValue(state.windowRoundness);
        this.showWindowControls.setSelected(state.showWindowControls);
        this.initialBgColor = state.getBackgroundColor();
        updateJF();
    }
}
