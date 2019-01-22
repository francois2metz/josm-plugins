package org.openstreetmap.josm.plugins.photoadjust;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;

import org.junit.Test;
import org.openstreetmap.josm.data.ImageData;
import org.openstreetmap.josm.data.coor.CachedLatLon;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;

public class InterpolateImagesTest {

    @Test
    public void testInterpolate(@Mocked(stubOutClassInitialization=true) MapView view) {
        InterpolateImages interpolate = new InterpolateImages();
        ArrayList<ImageEntry> list = new ArrayList<>();
        ImageEntry first = new ImageEntry(new File("test1"));
        ImageEntry last = new ImageEntry(new File("test3"));
        ImageEntry middle = new ImageEntry(new File("test2"));
        list.add(first);
        list.add(middle);
        list.add(last);

        ImageData data = new ImageData(list);
        new Expectations(first) {{
            first.getPos(); result = new CachedLatLon(0, 0);
        }};
        new Expectations(last) {{
            last.getPos(); result = new CachedLatLon(1, 1);
        }};
        new Expectations(middle) {{
            middle.setPos(new LatLon(0, 0));
            middle.flagNewGpsData();
        }};
        new Expectations(view) {{
            view.getPoint2D((LatLon) any); result = new Delegate() {
                Point2D aDelegateMethod(LatLon pos) {
                    return pos.getX() == 0 ? new Point2D.Double(0, 0) : new Point2D.Double(10, 5);
                }
            };
            view.getLatLon(5.0, 2.5); result = new LatLon(0, 0);
        }};
        new Expectations(data) {{
            data.notifyImageUpdate();
        }};

        data.addImageToSelection(first);
        data.addImageToSelection(last);
        interpolate.interpolate(data, view);
    }

}
