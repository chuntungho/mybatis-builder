/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.util;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ViewUtil {
    public static Icon getIcon(String path) {
        return IconLoader.getIcon(path, ViewUtil.class);
    }

    /**
     * Focus component in tabbed pane.
     *
     * @param component
     */
    public static void focusTab(JComponent component) {
        JTabbedPane tabbedPane = null;
        // find out tabbed pane & the tab
        Component focusTab = component;
        while (true) {
            Container parent = focusTab.getParent();
            if (parent instanceof JTabbedPane) {
                tabbedPane = (JTabbedPane) parent;
                break;
            }
            if (parent == null) {
                break;
            }
            focusTab = parent;
        }

        if (tabbedPane != null && tabbedPane.getSelectedComponent() != focusTab) {
            tabbedPane.setSelectedComponent(focusTab);
            component.requestFocus();
        }
    }


    /**
     * Make the component in panel enabled or not.
     *
     * @param panel
     * @param b
     */
    public static void makeAvailable(JPanel panel, boolean b) {
        if (panel.isEnabled() != b) {
            panel.setEnabled(b);
            for (int i = 0; i < panel.getComponentCount(); i++) {
                Component component = panel.getComponent(i);
                if (component instanceof JPanel) {
                    makeAvailable((JPanel) component, b);
                } else if (component instanceof JComponent) {
                    component.setEnabled(b);
                }
            }
        }
    }

    // set checkboxes' cursor to hand
    public static void setCheckboxCursor(JPanel panel) {
        Cursor hand = new Cursor(Cursor.HAND_CURSOR);
        for (int i = 0; i < panel.getComponentCount(); i++) {
            Component component = panel.getComponent(i);
            if (component instanceof JPanel) {
                setCheckboxCursor((JPanel) component);
            } else if (component instanceof JCheckBox) {
                component.setCursor(hand);
            }
        }
    }

    /**
     * Init the checkbox group panel.
     *
     * @param panel
     * @param allCheckBox
     */
    public static void initCheckBoxPanel(JPanel panel, JCheckBox allCheckBox) {
        setCheckboxCursor(panel);
        allCheckBox.addActionListener(createCheckAllAction(panel, allCheckBox));

        for (int i = 0; i < panel.getComponentCount(); i++) {
            Component component = panel.getComponent(i);
            if (component instanceof JCheckBox) {
                if (component != allCheckBox) {
                    ((JCheckBox) component).addActionListener(createRenderAllCheckBoxAction(panel, allCheckBox));
                }
            }
        }
    }

    private static ActionListener createRenderAllCheckBoxAction(JPanel panel, JCheckBox allCheckBox) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                renderAllCheckBox(panel, allCheckBox);
            }
        };
    }

    /**
     * Render all check box based on the panel.
     *
     * @param panel
     * @param allCheckBox
     */
    public static void renderAllCheckBox(JPanel panel, JCheckBox allCheckBox) {
        boolean allChecked = true;
        for (int i = 0; i < panel.getComponentCount(); i++) {
            Component component = panel.getComponent(i);
            if (component instanceof JCheckBox) {
                if (component != allCheckBox) {
                    allChecked = allChecked && ((JCheckBox) component).isSelected();
                    if (!allChecked) {
                        break;
                    }
                }
            }
        }

        allCheckBox.setSelected(allChecked);
    }

    private static ActionListener createCheckAllAction(JPanel panel, JCheckBox allCheckBox) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                Boolean checked = checkBox.isSelected();
                for (int i = 0; i < panel.getComponentCount(); i++) {
                    Component component = panel.getComponent(i);
                    if (component instanceof JCheckBox) {
                        if (component != allCheckBox) {
                            ((JCheckBox) component).setSelected(checked);
                        }
                    }
                }
            }
        };
    }
}
