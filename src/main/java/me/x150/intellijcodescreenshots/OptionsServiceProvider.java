package me.x150.intellijcodescreenshots;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "CodeScreenshotsOptions", storages = { @Storage(StoragePathMacros.CACHE_FILE) })
public class OptionsServiceProvider implements PersistentStateComponent<OptionsServiceProvider.State> {
    State state = new State();

    public static OptionsServiceProvider getInstance(Project project) {
        return project.getService(OptionsServiceProvider.class);
    }

    @Override
    public @Nullable OptionsServiceProvider.State getState() {
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
            return showWindowControls == state.showWindowControls;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(scale);
            result = (int) (temp ^ (temp >>> 32));
            result = 31 * result + (removeIndentation ? 1 : 0);
            temp = Double.doubleToLongBits(innerPadding);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(outerPaddingHoriz);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(outerPaddingVert);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + windowRoundness;
            result = 31 * result + (showWindowControls ? 1 : 0);
            return result;
        }
    }
}
