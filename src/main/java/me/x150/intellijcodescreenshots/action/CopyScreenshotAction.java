package me.x150.intellijcodescreenshots.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


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
					.setTitle("Code Screenshots")
					.addAction(NotificationAction.create("Save to File", anActionEvent -> saveToFileDialog(image, p)))
					.addAction(NotificationAction.create("Post to Image Host", anActionEvent -> {
						try {
							NotificationGroupManager.getInstance().getNotificationGroup("Code Screenshots")
									.createNotification("Posting to host...", NotificationType.INFORMATION)
									.setTitle("Code Screenshots")
									.notify(p);

							postToHost(":)", image, p);
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						} catch (JSONException ex) {
							throw new RuntimeException(ex);
						}
					}))
					.notify(p);
		}
	}

	private void postToHost(String apiKey, BufferedImage image, Project p) throws IOException, JSONException {

		System.out.println(image.getWidth() + " " + image.getHeight());

		// Validate the API key
		if (!apiKey.contains("_")) {
			System.out.println("INVALID API KEY -> " + apiKey.substring(0, 5));
			return;
		}

		System.out.println("Posting to host");
		URL url = new URL("https://api.e-z.host/files");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.addRequestProperty("User-Agent", "CodeScreenshots/1.0");
		connection.addRequestProperty("Sec-Ch-Ua-Platform", "Windows");

		// Set the API key in the Authorization header
		connection.setRequestProperty("key", apiKey);
		System.out.println("Created connection");

		// Create a boundary for the multipart/form-data
		String boundary = "Boundary-" + System.currentTimeMillis();
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		connection.setDoOutput(true);

		try (OutputStream os = connection.getOutputStream();
			 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

			// Add the file part
			writer.append("--" + boundary).append("\r\n");
			writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"image.png\"").append("\r\n");
			writer.append("Content-Type: image/png").append("\r\n");
			writer.append("\r\n");
			writer.flush();

			// Write the image data
			ImageIO.write(image, "png", os);
			os.flush();

			// End of file part
			writer.append("\r\n");

			// End of multipart request
			writer.append("--" + boundary + "--");
			writer.flush();
		}
		System.out.println("Sent file data");

		connection.connect();
		System.out.println(connection.getResponseMessage());

		// Get response from the API
		int responseCode = connection.getResponseCode();
		System.out.println("Got response code: " + responseCode);
		String imageUrl;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			JSONObject jsonResponse = new JSONObject(response.toString());
			imageUrl = jsonResponse.getString("imageUrl");

			connection.disconnect();

			if (responseCode == 200) {
				NotificationGroupManager.getInstance()
						.getNotificationGroup("Code Screenshots")
						.createNotification("Image uploaded", NotificationType.INFORMATION)
						.setTitle("Code Screenshots")
						.addAction(NotificationAction.create("Copy to clipboard", action -> {
							// Copy url to clipboard
							Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
							StringSelection selection = new StringSelection(imageUrl);
							cp.setContents(selection, null);

						}))
						.notify(p);
			}
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
