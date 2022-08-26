package me.x150.intellijcodescreenshots.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import me.x150.intellijcodescreenshots.OptionsServiceProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConfigProvider implements SearchableConfigurable, Configurable.NoScroll {
    Real panel;
    Project p;

    public ConfigProvider(Project p) {
        this.p = p;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "code-screenshots";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Code Screenshots";
    }

    @Override
    public @Nullable JComponent createComponent() {
        panel = new Real();
        panel.init();
        return panel.getMyWholePanel();
    }

    @Override
    public boolean isModified() {
        OptionsServiceProvider service = p.getService(OptionsServiceProvider.class);
        return panel != null && !service.getState().equals(panel.toState());
    }

    @Override
    public void apply() throws ConfigurationException {
        OptionsServiceProvider service = p.getService(OptionsServiceProvider.class);
        service.loadState(panel.toState());
    }

    @Override
    public void reset() {
        OptionsServiceProvider service = p.getService(OptionsServiceProvider.class);
        panel.fromState(service.getState());
    }

    @Override
    public void disposeUIResources() {
        panel = null;
    }
}
