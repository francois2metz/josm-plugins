package org.openstreetmap.josm.plugins.photoadjust;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.data.ImageData;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerAction;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;

public class SetDirectionToASequence extends AbstractAction implements LayerAction {

    public SetDirectionToASequence() {
        super(tr("Set direction to all images in the sequence"), null);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        GeoImageLayer layer = getSelectedLayer();

        List<ImageEntry> images = layer.getImageData().getImages();
        for (int i = 0; i < images.size(); i++) {
            ImageEntry entry = images.get(i);
            if (i + 1 < images.size()) {
                changeDirection(entry, images.get(i + 1), layer.getImageData());
            }
        }
    }

    private static GeoImageLayer getSelectedLayer() {
        return (GeoImageLayer)LayerListDialog.getInstance().getModel()
                .getSelectedLayers().get(0);
    }

    private void changeDirection(ImageEntry photo, ImageEntry next, ImageData data) {
        final LatLon photoLL = photo.getPos();
        if (photoLL == null) {
            // Direction cannot be set if image doesn't have a position.
            return;
        }
        final LatLon mouseLL = next.getPos();
        // The projection doesn't matter here.
        double direction = photoLL.bearing(mouseLL) * 360.0 / 2.0 / Math.PI;
        if (direction < 0.0) {
            direction += 360.0;
        } else if (direction >= 360.0) {
            direction -= 360.0;
        }
        data.updateImageDirection(photo, direction);
    }

    @Override
    public boolean supportLayers(List<Layer> layers) {
        return layers.size() == 1 && layers.get(0) instanceof GeoImageLayer;
    }

    @Override
    public Component createMenuComponent() {
        return new JMenuItem(this);
    }
}
