// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.photoadjust;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.ImageData;
import org.openstreetmap.josm.data.ImageData.ImageDataUpdateListener;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;

/**
 * Interpolate a part of a geotagged sequence
 */
public class InterpolateImages extends JosmAction implements LayerChangeListener, ImageDataUpdateListener {

    public InterpolateImages() {
        super(tr("Interpolate between the 2 images"),
                null,
                tr("Interpolate images position between the 2 images selected"),
                null, false, false);

        installAdapters();
        updateEnabledState();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!getLayerWithTwoSelectedImages().isPresent()) {
            return;
        }
        GeoImageLayer layer = getLayerWithTwoSelectedImages().get();
        ImageData data = layer.getImageData();

        interpolate(data, MainApplication.getMap().mapView);
    }

    public void interpolate(ImageData data, MapView mapView) {
        List<ImageEntry> images = data.getImages();
        List<ImageEntry> selected = data.getSelectedImages();
        Collections.sort(selected);
        ImageEntry firstPhoto = selected.get(0);
        ImageEntry lastPhoto = selected.get(1);
        final Point2D pos1 = mapView.getPoint2D(firstPhoto.getPos());
        final Point2D pos2 = mapView.getPoint2D(lastPhoto.getPos());
        int nbPhotos = images.indexOf(lastPhoto) - images.indexOf(firstPhoto);
        int firstIndex = images.indexOf(firstPhoto);
        for (int i = 1; i < nbPhotos; i++) {
            ImageEntry photo = images.get(i+ firstIndex);
            LatLon newPos = mapView.getLatLon(
                    (pos2.getX() - pos1.getX()) / nbPhotos * i + pos1.getX(),
                    (pos2.getY() - pos1.getY()) / nbPhotos * i + pos1.getY()
                    );
            photo.setPos(newPos);
            photo.flagNewGpsData();
        }
        data.notifyImageUpdate();
    }

    @Override
    protected void installAdapters() {
        MainApplication.getLayerManager().addLayerChangeListener(this);
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerWithTwoSelectedImages().isPresent());
    }

    private static Optional<GeoImageLayer> getLayerWithTwoSelectedImages() {
        List<GeoImageLayer> list = MainApplication.getLayerManager().getLayersOfType(GeoImageLayer.class);
        return list.stream().filter(l -> l.getImageData().getSelectedImages().size() == 2).findFirst();
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
        Layer layer = e.getAddedLayer();
        if (layer instanceof GeoImageLayer) {
            ((GeoImageLayer) layer).getImageData().addImageDataUpdateListener(this);
        }
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        Layer layer = e.getRemovedLayer();

        if (layer instanceof GeoImageLayer) {
            ((GeoImageLayer) layer).getImageData().removeImageDataUpdateListener(this);
        }
        updateEnabledState();
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {}

    @Override
    public void imageDataUpdated(ImageData data) {}

    @Override
    public void selectedImagesChanged(ImageData data) {
        updateEnabledState();
    }
}
