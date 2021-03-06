// License: WTFPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imagery_offset_db;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;

import org.openstreetmap.josm.data.Version;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.preferences.ToolbarPreferences;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * A plugin to request and store imagery offsets in the centralized database.
 *
 * @author Zverik
 * @license WTFPL
 */
public class ImageryOffsetPlugin extends Plugin {
    private GetImageryOffsetAction getAction;
    private StoreImageryOffsetAction storeAction;

    /**
     * Add both actions to their own menu. This creates
     * "Offset" menu, because "Imagery" is constantly rebuilt,
     * losing all changes, and other menus are either too long already,
     * or completely unsuitable for imagery offset actions.
     * @param info Plugin information
     */
    public ImageryOffsetPlugin(PluginInformation info) {
        super(info);

        getAction = new GetImageryOffsetAction();
        storeAction = new StoreImageryOffsetAction();

        // before 5803 imagery menu was constantly regenerated, erasing extra items
        // before 5729 it was regenerated only when the imagery list was modified (also bad)
        int version = Version.getInstance().getVersion();
        JMenu offsetMenu = version < 5803
                ? MainApplication.getMenu().addMenu("Offset", tr("Offset"), KeyEvent.VK_O, 6, "help")
                        : MainApplication.getMenu().imageryMenu;
                offsetMenu.add(getAction);
                offsetMenu.add(storeAction);

                // an ugly hack to add this plugin to the toolbar
                if (Config.getPref().getBoolean("iodb.modify.toolbar", true)) {
                    List<String> toolbar = new LinkedList<>(ToolbarPreferences.getToolString());
                    if (!toolbar.contains("getoffset")) {
                        toolbar.add("getoffset");
                        Config.getPref().putList("toolbar", toolbar);
                        MainApplication.getToolbar().refreshToolbarControl();
                    }
                    Config.getPref().putBoolean("iodb.modify.toolbar", false);
                }
    }
}
