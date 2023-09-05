package me.x150.intellijcodescreenshots.action;

import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import me.x150.intellijcodescreenshots.Plugin;
import me.x150.intellijcodescreenshots.util.ScreenshotBuilder;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

// Action to copy the selected code snippet
public class CopyScreenshotAction extends DumbAwareAction {

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project p = e.getProject();
		if (p == null) {
			return;
		}
		DataContext context = e.getDataContext();
		Editor editor = PlatformDataKeys.EDITOR.getData(context);
		if (editor == null) {
			Plugin.showError(p, "Screenshotting code is only available in an editor");
			return;
		}
		if (!editor.getSelectionModel().hasSelection()) {
			// No selection, tell user
			Plugin.showError(p, "Select code to screenshot first");
			return;
		}
		ScreenshotBuilder sb = new ScreenshotBuilder(editor);
		BufferedImage image = sb.createImage();
		if (image != null) {
			Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
			cp.setContents(new Transferable() {
				@Override
				public DataFlavor[] getTransferDataFlavors() {
					return new DataFlavor[]{DataFlavor.imageFlavor};
				}

				@Override
				public boolean isDataFlavorSupported(DataFlavor flavor) {
					return flavor == DataFlavor.imageFlavor;
				}

				@NotNull
				@Override
				public Object getTransferData(DataFlavor flavor) {
					return image;
				}
			}, (clipboard, contents) -> {
			});
			NotificationGroupManager.getInstance()
					.getNotificationGroup("Code Screenshots")
					.createNotification("Image copied", NotificationType.INFORMATION)
					.setTitle("Code screenshots")
					.addAction(NotificationAction.create("Save to File", anActionEvent -> saveToFileDialog(image, p)))
					.notify(p);
		}
	}

	void saveToFileDialog(BufferedImage image, Project p) {
		FileSaverDescriptor fsd = new FileSaverDescriptor("Choose Image Location", "Select a location to save the screenshot to", "png");
		FileSaverDialog saveFileDialog = FileChooserFactory.getInstance().createSaveFileDialog(fsd, p);
		VirtualFileWrapper save = saveFileDialog.save("screenshot.png");
		if (save == null) return;
		File file = save.getFile();
		try (FileOutputStream fos = new FileOutputStream(file)) {
			ImageIO.write(image, "png", fos);
		} catch (Throwable t) {
			NotificationGroupManager.getInstance()
					.getNotificationGroup("Code Screenshots")
					.createNotification("Failed to write file: " + t.getClass().getSimpleName(), NotificationType.ERROR)
					.setTitle("Code screenshots")
					.setImportant(true)
					.notify(p);
			t.printStackTrace();
		}
	}

	// Dictates whether the "Screenshot Selected Code" action should be enabled, in the right click menu or the keybinding
	@Override
	public void update(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			return;
		}
		DataContext context = e.getDataContext();
		Editor editor = PlatformDataKeys.EDITOR.getData(context);
		e.getPresentation().setEnabled(editor != null && editor.getSelectionModel().hasSelection());

	}
}
