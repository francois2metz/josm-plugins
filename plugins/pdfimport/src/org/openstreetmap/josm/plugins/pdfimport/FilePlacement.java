// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Properties;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;

public class FilePlacement {
    /*
     * provide data and services to place a PDF-File to world coordinates
     * enhanced by FilePlacement18 but kept for compatibilty to existing code
     */
    protected Projection projection = null;
    protected double minX = 0;
    protected double maxX = 1;
    protected double minY = 0;
    protected double maxY = 1;

    protected double minEast = 0;
    protected double maxEast = 10000;
    protected double minNorth = 0;
    protected double maxNorth = 10000;

    private AffineTransform transform;

    public void setPdfBounds(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void setEastNorthBounds(double minEast, double minNorth, double maxEast, double maxNorth) {
        this.minEast = minEast;
        this.maxEast = maxEast;
        this.minNorth = minNorth;
        this.maxNorth = maxNorth;
    }

    protected Properties toProperties() {
        Properties p = new Properties();
        if (projection != null) {
            p.setProperty("Projection", projection.toCode());
        }

        p.setProperty("minX", Double.toString(minX));
        p.setProperty("maxX", Double.toString(maxX));
        p.setProperty("minY", Double.toString(minY));
        p.setProperty("maxY", Double.toString(maxY));
        p.setProperty("minEast", Double.toString(minEast));
        p.setProperty("maxEast", Double.toString(maxEast));
        p.setProperty("minNorth", Double.toString(minNorth));
        p.setProperty("maxNorth", Double.toString(maxNorth));

        return p;
    }

    protected void fromProperties(Properties p) {
        String projectionCode = p.getProperty("Projection", null);
        if (projectionCode != null) {
            projection = ProjectionInfo.getProjectionByCode(projectionCode); // TODO: Handle non-core Projections
        } else {
            projection = null;
        }

        minX = parseProperty(p, "minX", minX);
        maxX = parseProperty(p, "maxX", maxX);
        minY = parseProperty(p, "minY", minY);
        maxY = parseProperty(p, "maxY", maxY);

        minEast = parseProperty(p, "minEast", minEast);
        maxEast = parseProperty(p, "maxEast", maxEast);
        minNorth = parseProperty(p, "minNorth", minNorth);
        maxNorth = parseProperty(p, "maxNorth", maxNorth);
    }

    protected double parseProperty(Properties p, String name, double defaultValue) {
        if (!p.containsKey(name)) {
            return defaultValue;
        }

        String value = p.getProperty(name);

        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    protected String prepareTransform() {
        if (this.minX > this.maxX) {
            return tr("Transform error: Min X must be smaller than max");
        }

        if (this.minY > this.maxY) {
            return tr("Transform error: Min Y must be smaller than max");
        }

        if (Math.abs(this.minY - this.maxY) < 1 &&
                Math.abs(this.minX - this.maxX) < 1) {
            return tr("Transform error: Points too close");
        } else if (Math.abs(this.minX - this.maxX) < 1) {
            //x axis equal, assume same scale in both dimensions
            if (this.minEast == this.maxEast) {
                //no rotation
                this.maxX = this.minX + this.maxY - this.minY;
                this.maxEast = this.minEast + this.maxNorth - this.minNorth;
            } else if (this.minNorth == this.maxNorth) {
                //needs rotated 90 degrees clockwise, or counter
                this.maxX = this.minX + this.maxY - this.minY;
                this.maxNorth = this.minNorth - (this.maxEast - this.minEast);
            } else {
                return tr("Transform error: Unsupported variant.");
            }
        } else if (Math.abs(this.minY - this.maxY) < 1) {
            //Y axis equal, assume same scale in both dimensions
            if (this.minNorth == this.maxNorth) {
                //no rotation
                this.maxY = this.minY + this.maxX - this.minX;
                this.maxNorth = this.minNorth + this.maxEast - this.minEast;
            } else if (this.minEast == this.maxEast) {
                //needs rotated 90 degrees clockwise, or counter
                this.maxY = this.minY + this.maxX - this.minX;
                this.maxEast = this.minEast - (this.maxNorth - this.minNorth);
            } else {
                return tr("Transform error: Unsupported variant.");
            }
        }

        if (this.minEast < this.maxEast && this.minNorth < this.maxNorth) {
            //no rotation
            this.transform = new AffineTransform();
            this.transform.translate(this.minEast, this.minNorth);
            this.transform.scale(
                    (this.maxEast - this.minEast) / (this.maxX - this.minX),
                    (this.maxNorth - this.minNorth) / (this.maxY - this.minY));
            this.transform.translate(-this.minX, -this.minY);
        } else if (this.minEast > this.maxEast && this.minNorth < this.maxNorth) {
            //need to rotate 90 degrees counterclockwise
            this.transform = new AffineTransform();
            //transform to 0..1, 0..1 range
            this.transform.preConcatenate(AffineTransform.getTranslateInstance(-this.minX, -this.minY));
            this.transform.preConcatenate(AffineTransform.getScaleInstance(1/(this.maxX - this.minX), 1/(this.maxY - this.minY)));

            //rotate -90 degs around min
            this.transform.preConcatenate(AffineTransform.getQuadrantRotateInstance(1, 0, 0));

            //transform back to target range
            this.transform.preConcatenate(AffineTransform.getScaleInstance(
                    (this.minEast - this.maxEast),
                    (this.maxNorth - this.minNorth)));
            this.transform.preConcatenate(AffineTransform.getTranslateInstance(this.minEast, this.minNorth));
        } else if (this.minEast < this.maxEast && this.minNorth > this.maxNorth) {
            //need to rotate 90 degrees clockwise
            this.transform = new AffineTransform();
            //transform to 0..1, 0..1 range
            this.transform.preConcatenate(AffineTransform.getTranslateInstance(-this.minX, -this.minY));
            this.transform.preConcatenate(AffineTransform.getScaleInstance(1/(this.maxX - this.minX), 1/(this.maxY - this.minY)));

            //rotate 90 degs around min
            this.transform.preConcatenate(AffineTransform.getQuadrantRotateInstance(-1, 0, 0));

            //transform back to target range
            this.transform.preConcatenate(AffineTransform.getScaleInstance(
                    (this.maxEast - this.minEast),
                    (this.minNorth - this.maxNorth)));
            this.transform.preConcatenate(AffineTransform.getTranslateInstance(this.minEast, this.minNorth));
        } else {
            return tr("Transform error: Unsupported orientation");
        }

        return null;

    }

    EastNorth en = new EastNorth(0, 0);
    Point2D src = new Point2D.Double();

    protected Bounds getWorldBounds(PathOptimizer data) {
        LatLon min = this.tranformCoords(new Point2D.Double(data.bounds.getMinX(), data.bounds.getMinY()));
        LatLon max = this.tranformCoords(new Point2D.Double(data.bounds.getMaxX(), data.bounds.getMaxY()));
        return new Bounds(min, max);
    }

    protected LatLon tranformCoords(Point2D pt) {

        if (this.projection == null) {
            return new LatLon(pt.getY() / 1000, pt.getX() / 1000);
        } else {
            Point2D dest = new Point2D.Double();
            this.transform.transform(pt, dest);
            en = new EastNorth(dest.getX(), dest.getY());
            return this.projection.eastNorth2latlon(en);
        }
    }

    protected EastNorth reverseTransform(LatLon coor) {
        if (this.projection == null) {
            return new EastNorth(coor.lon() * 1000, coor.lat() * 1000);
        } else {
            return null;
        }
    }

}
