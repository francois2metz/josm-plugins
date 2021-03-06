// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.npm;

import java.awt.GraphicsEnvironment;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.io.auth.CredentialsManager;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;

public class NPMPlugin extends Plugin {
    
    public final static String NPMPLUGIN_KEY = "plugins.native-password-manager.";
    
    public NPMPlugin(PluginInformation info) {
        super(info);
        initialize();
    }
    
    private void initialize() {
        String pref = Config.getPref().get(NPMPLUGIN_KEY+"agent", null);
        if ("off".equals(pref)) return;
        NPMType sel = NPMType.fromPrefString(pref);
        if (sel != null) {
            CredentialsManager.registerCredentialsAgentFactory(
                    new NPMCredentialsAgentFactory(sel)
            );
        } else if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                        final InitializationWizard wizard = new InitializationWizard();
                        wizard.showDialog();
                }
            });
        }
    }

    public static void selectAndSave(NPMType type) {
        CredentialsManager.registerCredentialsAgentFactory(
                new NPMCredentialsAgentFactory(type)
        );
        Config.getPref().put(NPMPLUGIN_KEY+"agent", type.toPrefString());
    }
    
    public static void turnOffPermanently() {
        Config.getPref().put(NPMPLUGIN_KEY+"agent", "off");
    }
}
