package me.x150.intellijcodescreenshots;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class Plugin {
    public static void showError(Project project, String msg) {
        NotificationGroupManager.getInstance().getNotificationGroup("Code Screenshots").createNotification(msg, NotificationType.ERROR).notify(project);
    }
}
