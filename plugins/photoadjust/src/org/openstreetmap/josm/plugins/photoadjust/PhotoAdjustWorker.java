// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.photoadjust;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;

import org.openstreetmap.josm.data.ImageData;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.gui.layer.geoimage.ImageViewerDialog;

/**
 * Class that does the actual work.
 */
public class PhotoAdjustWorker {

    private ImageEntry dragPhoto;
    private ImageData dragData;
    // Offset between center of the photo and point where it is
    // clicked.  This must be in pixels to maintain the same offset if
    // the photo is moved very far.
    private Point2D dragOffset;
    private boolean centerViewIsDisabled = false;
    private boolean centerViewNeedsEnable = false;

    /**
     * Reset the worker.
     */
    public void reset() {
        dragPhoto = null;
        dragData = null;
        dragOffset = null;
    }

    /**
     * Disable the "center view" button.  The map is moved instead of the
     * photo if the center view is enabled while a photo is moved.  The method
     * disables the center view to avoid such behavior.  Call
     * restoreCenterView() to restore the original state.
     */
    public synchronized void disableCenterView() {
        if (!centerViewIsDisabled && ImageViewerDialog.isCenterView()) {
            centerViewIsDisabled = true;
            centerViewNeedsEnable = ImageViewerDialog.setCentreEnabled(false);
        }
    }

    /**
     * Restore the center view state that was active before
     * disableCenterView() was called.
     */
    public synchronized void restoreCenterView() {
        if (centerViewIsDisabled) {
            if (centerViewNeedsEnable) {
                centerViewNeedsEnable = false;
                ImageViewerDialog.setCentreEnabled(true);
            }
            centerViewIsDisabled = false;
        }
    }

    /**
     * Mouse click handler.  Control+click changes the image direction if
     * there is a photo selected on the map.  Shift+click positions the photo
     * from the ImageViewerDialog.  Click without shift or control checks if
     * there is a photo under the mouse.
     *
     * @param evt Mouse event from MouseAdapter mousePressed().
     * @param imageLayers List of GeoImageLayers to be considered.
     */
    public void doMousePressed(MouseEvent evt,
            List<GeoImageLayer> imageLayers) {
        reset();

        if (evt.getButton() == MouseEvent.BUTTON1
                && imageLayers != null && !imageLayers.isEmpty()) {
            // Check if modifier key is pressed and change to
            // image viewer photo if it is.
            final boolean isShift = (evt.getModifiers() & InputEvent.SHIFT_MASK) != 0;
            final boolean isCtrl = (evt.getModifiers() & InputEvent.CTRL_MASK) != 0;
            if (isShift || isCtrl) {
                for (GeoImageLayer layer: imageLayers) {
                    if (layer.isVisible()) {
                        final ImageEntry img = layer.getImageData().getSelectedImage();
                        if (img != null) {
                            // Change direction if control is pressed, position
                            // otherwise.  Shift+control changes direction, similar to
                            // rotate in select mode.
                            //
                            // Combinations:
                            // S ... shift pressed
                            // C ... control pressed
                            // pos ... photo has a position set == is displayed on the map
                            // nopos ... photo has no position set
                            //
                            // S + pos: position at mouse
                            // S + nopos: position at mouse
                            // C + pos: change orientation
                            // C + nopos: ignored
                            // S + C + pos: change orientation
                            // S + C + nopos: ignore
                            if (isCtrl) {
                                if (img.getPos() != null) {
                                    changeDirection(img, layer.getImageData(), evt);
                                }
                            } else { // shift pressed
                                movePhoto(img, layer.getImageData(), evt);
                            }
                            dragPhoto = img;
                            dragData = layer.getImageData();
                            break;
                        }
                    }
                }
            } else {
                // Start with the top layer.
                for (GeoImageLayer layer: imageLayers) {
                    if (layer.isVisible()) {
                        dragPhoto = layer.getPhotoUnderMouse(evt);
                        if (dragPhoto != null) {
                            dragData = layer.getImageData();
                            setDragOffset(dragPhoto, evt);
                            disableCenterView();
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Mouse release handler.
     *
     * @param evt Mouse event from MouseAdapter mouseReleased().
     */
    public void doMouseReleased(MouseEvent evt) {
        restoreCenterView();
        //if (dragLayer != null && dragPhoto != null) {
        //    // Re-display the photo to update the OSD.
        //    ImageViewerDialog.showImage(dragLayer, dragPhoto);
        //}
    }

    /**
     * Mouse drag handler.  Changes direction or moves photo.
     *
     * @param evt Mouse event from MouseMotionAdapter mouseDragged().
     */
    public void doMouseDragged(MouseEvent evt) {
        if (dragData != null && dragPhoto != null) {
            if ((evt.getModifiers() & InputEvent.CTRL_MASK) != 0) {
                changeDirection(dragPhoto, dragData, evt);
            } else {
                disableCenterView();
                movePhoto(dragPhoto, dragData, evt);
            }
        }
    }

    /**
     * Set the offset between a photo and the current mouse position.
     *
     * @param photo The photo to move.
     * @param evt Mouse event from one of the mouse adapters.
     */
    private void setDragOffset(ImageEntry photo, MouseEvent evt) {
        final Point2D centerPoint = MainApplication.getMap().mapView.getPoint2D(photo.getPos());
        dragOffset = new Point2D.Double(centerPoint.getX() - evt.getX(),
                                        centerPoint.getY() - evt.getY());
    }

    /**
     * Move the photo to the mouse position.
     *
     * @param photo The photo to move.
     * @param data ImageData of the photo.
     * @param evt Mouse event from one of the mouse adapters.
     */
    private void movePhoto(ImageEntry photo, ImageData data,
            MouseEvent evt) {
        LatLon newPos;
        if (dragOffset != null) {
            newPos = MainApplication.getMap().mapView.getLatLon(
                dragOffset.getX() + evt.getX(),
                dragOffset.getY() + evt.getY());
        } else {
            newPos = MainApplication.getMap().mapView.getLatLon(evt.getX(), evt.getY());
        }
        data.updateImagePosition(photo, newPos);
        // Re-display the photo because the OSD data might change (new
        // coordinates).  Or do that in doMouseReleased().
        //ImageViewerDialog.showImage(layer, photo);
    }

    /**
     * Set the image direction, i.e. let it point to where the mouse is.
     *
     * @param photo The photo to move.
     * @param data ImageData of the photo.
     * @param evt Mouse event from one of the mouse adapters.
     */
    private void changeDirection(ImageEntry photo, ImageData data,
            MouseEvent evt) {
        final LatLon photoLL = photo.getPos();
        if (photoLL == null) {
            // Direction cannot be set if image doesn't have a position.
            return;
        }
        final LatLon mouseLL = MainApplication.getMap().mapView.getLatLon(evt.getX(), evt.getY());
        // The projection doesn't matter here.
        double direction = photoLL.bearing(mouseLL) * 360.0 / 2.0 / Math.PI;
        if (direction < 0.0) {
            direction += 360.0;
        } else if (direction >= 360.0) {
            direction -= 360.0;
        }
        data.updateImageDirection(photo, direction);
        setDragOffset(photo, evt);
    }
}
