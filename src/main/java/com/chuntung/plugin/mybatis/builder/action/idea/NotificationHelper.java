/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.BuildNumber;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class NotificationHelper {
    public static final String NOTIFICATION_GROUP = "MyBatisBuilder.NotificationGroup";
    private static NotificationHelper instance;

    private NotificationGroup notificationGroup;

    private NotificationHelper() {
        try {
            // 203.3645.34 above,  NotificationGroup getNotificationGroup(@NotNull String groupId);
            // NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP);
            if (BuildNumber.fromString("203.3645.34").compareTo(ApplicationInfo.getInstance().getBuild()) <= 0) {

                Class<?> managerClass = Class.forName("com.intellij.notification.NotificationGroupManager");
                Method instanceMethod = managerClass.getMethod("getInstance");
                Object manager = instanceMethod.invoke(null);
                Method groupMethod = managerClass.getMethod("getNotificationGroup", String.class);
                this.notificationGroup = (NotificationGroup) groupMethod.invoke(manager, NOTIFICATION_GROUP);
            } else {
                // before 203
                // public NotificationGroup(@NotNull String displayId, @NotNull NotificationDisplayType defaultDisplayType, boolean logByDefault) {
                Method balloonGroup = NotificationGroup.class.getMethod("balloonGroup", String.class);
                notificationGroup = (NotificationGroup) balloonGroup.invoke(null, NOTIFICATION_GROUP);
            }
        } catch (Exception e) {
            // NOOP
            e.printStackTrace();
        }
    }

    public static NotificationHelper getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (NOTIFICATION_GROUP) {
            if (instance == null) {
                instance = new NotificationHelper();
            }
        }
        return instance;
    }

    public void notifyInfo(String info, Project project) {
        Notification notification = notificationGroup.createNotification(info, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, project);
    }

    public void notifyWarn(String warn, Project project) {
        Notification notification = notificationGroup.createNotification(warn, NotificationType.WARNING);
        Notifications.Bus.notify(notification, project);
    }

    public void notifyError(String error, Project project) {
        Notification notification = notificationGroup.createNotification(error, NotificationType.ERROR);
        Notifications.Bus.notify(notification, project);
    }
}
