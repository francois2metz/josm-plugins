// License: GPL. For details, see LICENSE file.
package gpxfilter;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.layer.GpxLayer;

public class EGpxLayer extends GpxLayer {
    public EGpxLayer(final Bounds b) {
        super(new GpxData(), tr("GPX Data"));
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                GpxGrabber grabber = new GpxGrabber(b);
                while (true) {
                    GpxData newData;
                    try {
                        newData = grabber.parseRawGps();
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    if (newData == null || !newData.hasTrackPoints()) break;
                    synchronized (this) {
                        data.mergeFrom(newData);
                    }

                    invalidate();
                }
                grabber.cancel();
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
