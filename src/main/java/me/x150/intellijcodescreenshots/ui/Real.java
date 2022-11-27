package me.x150.intellijcodescreenshots.ui;

import me.x150.intellijcodescreenshots.OptionsServiceProvider;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.util.Locale;

public class Real {
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

    public void init() {
        scaleInp.addChangeListener(e -> dataVis.setText(String.format(Locale.ENGLISH, "%.2f", scaleInp.getValue() * SLIDER_SCALE)));
        innerPaddingInp.addChangeListener(e -> innerPaddingVis.setText(innerPaddingInp.getValue() + ""));
        outerPaddingInp.addChangeListener(e -> outerPaddingVis.setText(outerPaddingInp.getValue() + ""));
        windowRoundnessInp.addChangeListener(e -> roundnessVis.setText(windowRoundnessInp.getValue() + ""));
    }

    public OptionsServiceProvider.State toState() {
        OptionsServiceProvider.State s = new OptionsServiceProvider.State();
        s.scale = scaleInp.getValue() * SLIDER_SCALE;
        s.removeIndentation = removeIndent.isSelected();
        s.innerPadding = innerPaddingInp.getValue();
        s.outerPaddingVert = s.outerPaddingHoriz = outerPaddingInp.getValue();
        s.windowRoundness = windowRoundnessInp.getValue();
        s.showWindowControls = showWindowControls.isSelected();
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
    }
}
