// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;

import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.PluginHandler;
import org.openstreetmap.josm.plugins.PluginInformation;

import livegps.LiveGpsPlugin;

// CHECKSTYLE.OFF: HideUtilityClassConstructorCheck

/**
 * Plugin that uses live gps data and a button panel to add nodes/waypoints etc at the current
 * position.
 *
 * TODO: auto save marker layer and data layer?
 * TODO: in action retrieve buttontype state to set on/off values
 * @author cdaller
 *
 */
public final class SurveyorPlugin {

    private static JFrame surveyorFrame;
    public static final String PREF_KEY_STREET_NAME_FONT_SIZE = "surveyor.way.fontsize";
    
    /**
     * Constructs a new {@code SurveyorPlugin}.
     * @param info plugin information
     */
    public SurveyorPlugin(PluginInformation info) {

        LiveGpsPlugin gpsPlugin = (LiveGpsPlugin) PluginHandler.getPlugin("livegps");
        if (gpsPlugin == null)
            throw new IllegalStateException(tr("SurveyorPlugin needs LiveGpsPlugin, but could not find it!"));

        JMenu m = gpsPlugin.getLgpsMenu();
        m.addSeparator();
        MainMenu.add(m, new SurveyorShowAction(gpsPlugin));

        AutoSaveAction autoSaveAction = new AutoSaveAction();
        JCheckBoxMenuItem autoSaveMenu = new JCheckBoxMenuItem(autoSaveAction);
        m.add(autoSaveMenu);
        autoSaveMenu.setAccelerator(autoSaveAction.getShortcut().getKeyStroke());
    }

    /**
     * @return the surveyorFrame
     */
    public static JFrame getSurveyorFrame() {
        return surveyorFrame;
    }

    /**
     * @param surveyorFrame the surveyorFrame to set
     */
    public static void setSurveyorFrame(JFrame surveyorFrame) {
        SurveyorPlugin.surveyorFrame = surveyorFrame;
    }

    // CHECKSTYLE.ON: HideUtilityClassConstructorCheck
}
