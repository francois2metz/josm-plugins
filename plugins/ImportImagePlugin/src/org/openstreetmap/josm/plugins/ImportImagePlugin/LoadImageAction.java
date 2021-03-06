// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.ImportImagePlugin;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.ImportImagePlugin.ImageLayer.LayerCreationCanceledException;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * Class extends JosmAction and creates a new image layer.
 *
 * @author Christoph Beekmans, Fabian Kowitz, Anna Robaszkiewicz, Oliver Kuhn, Martin Ulitzny
 *
 */
public class LoadImageAction extends JosmAction {

    private Logger logger = Logger.getLogger(LoadImageAction.class);

    /**
     * Constructor...
     */
    public LoadImageAction() {
        super(tr("Import image"), (String) null, tr("Import georeferenced image"), null, true, "importimage/loadimage", true);
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        // Choose a file
        JFileChooser fc = new JFileChooser(Config.getPref().get("plugins.importimage.importpath", null));
        fc.setAcceptAllFileFilterUsed(false);
        int result = fc.showOpenDialog(MainApplication.getMainFrame());

        ImageLayer layer = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            Config.getPref().put("plugins.importimage.importpath", fc.getCurrentDirectory().getAbsolutePath());
            logger.info("File chosen:" + fc.getSelectedFile());
            try {
                layer = new ImageLayer(fc.getSelectedFile());
            } catch (LayerCreationCanceledException e) {
                // if user decides that layer should not be created just return.
                return;
            } catch (Exception e) {
                logger.error("Error while creating image layer: \n" + e.getMessage());
                JOptionPane.showMessageDialog(null, marktr("Error while creating image layer: " + e.getCause()));
                return;
            }

            // Add layer:
            MainApplication.getLayerManager().addLayer(layer);
            BoundingXYVisitor boundingXYVisitor = new BoundingXYVisitor();
            layer.visitBoundingBox(boundingXYVisitor);
            MainApplication.getMap().mapView.zoomTo(boundingXYVisitor);
        }
    }
}
