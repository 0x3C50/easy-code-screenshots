package me.x150.intellijcodescreenshots.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import me.x150.intellijcodescreenshots.OptionsServiceProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConfigProvider implements SearchableConfigurable, Configurable.NoScroll {
	SettingsUI panel;

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
		panel = new SettingsUI();
		panel.init();
		return panel.getMyWholePanel();
	}

	@Override
	public boolean isModified() {
		OptionsServiceProvider service = OptionsServiceProvider.getInstance();
		return panel != null && !service.getState().equals(panel.toState());
	}

	@Override
	public void apply() {
		OptionsServiceProvider service = OptionsServiceProvider.getInstance();
		service.loadState(panel.toState());
	}

	@Override
	public void reset() {
		OptionsServiceProvider service = OptionsServiceProvider.getInstance();
		panel.fromState(service.getState());
	}

	@Override
	public void disposeUIResources() {
		panel = null;
	}
}
