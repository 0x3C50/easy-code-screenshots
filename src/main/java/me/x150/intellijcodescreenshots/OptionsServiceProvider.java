package me.x150.intellijcodescreenshots;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.ui.JBColor;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@State(name = "CodeScreenshotsOptions", storages = {@Storage(value = "codeScreenshots.xml")}, category = SettingsCategory.PLUGINS)
public class OptionsServiceProvider implements PersistentStateComponent<OptionsServiceProvider.State> {
	State state = new State();

	public static OptionsServiceProvider getInstance() {
		return ApplicationManager.getApplication().getService(OptionsServiceProvider.class);
	}

	@Override
	public OptionsServiceProvider.State getState() {
		return state;
	}

	@Override
	public void loadState(@NotNull State state) {
		XmlSerializerUtil.copyBean(state, this.state);
	}

	public static class State {
		public double scale = 1.5;
		public boolean removeIndentation = true;
		public double innerPadding = 16;
		public double outerPaddingHoriz = 10;
		public double outerPaddingVert = 10;
		public int windowRoundness = 10;
		public boolean showWindowControls = true;
		public int backgroundColor = 0xffabb8c3;
		public boolean showFileName = true;

		public Color getBackgroundColor() {
			return new JBColor(new Color(backgroundColor, true), new Color(backgroundColor, true));
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			State state = (State) o;

			if (Double.compare(state.scale, scale) != 0) {
				return false;
			}
			if (removeIndentation != state.removeIndentation) {
				return false;
			}
			if (Double.compare(state.innerPadding, innerPadding) != 0) {
				return false;
			}
			if (Double.compare(state.outerPaddingHoriz, outerPaddingHoriz) != 0) {
				return false;
			}
			if (Double.compare(state.outerPaddingVert, outerPaddingVert) != 0) {
				return false;
			}
			if (windowRoundness != state.windowRoundness) {
				return false;
			}
			if (showWindowControls != state.showWindowControls) {
				return false;
			}
			if (showFileName != state.showFileName)
			{
				return false;
			}
			return backgroundColor == state.backgroundColor;
		}

		@Override
		public int hashCode() {
			int result;
			result = Double.hashCode(scale);
			result = 31 * result + (removeIndentation ? 1 : 0);
			result = 31 * result + Double.hashCode(innerPadding);
			result = 31 * result + Double.hashCode(outerPaddingHoriz);
			result = 31 * result + Double.hashCode(outerPaddingVert);
			result = 31 * result + windowRoundness;
			result = 31 * result + (showWindowControls ? 1 : 0);
			result = 31 * result + (showFileName ? 1 : 0);
			result = 31 * result + backgroundColor;
			return result;
		}
	}
}
