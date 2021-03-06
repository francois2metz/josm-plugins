// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.MenuActionCancelGrab;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.MenuActionLoadFromCache;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.MenuActionRefineGeoRef;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.MenuActionSaveRasterAs;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.mapmode.WMSAdjustAction;
import org.openstreetmap.josm.plugins.fr.cadastre.preferences.CadastrePreferenceSetting;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

/**
 * This is a layer that grabs the current screen from the French cadastre WMS
 * server. The data fetched this way is tiled and managed to the disc to reduce
 * server load.
 */
public class WMSLayer extends Layer implements ImageObserver {

    private int lambertZone = -1;

    CadastreGrabber grabber = new CadastreGrabber();

    protected static final Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(
            CadastrePlugin.class.getResource("/images/cadastre_small.png")));

    private Vector<GeorefImage> images = new Vector<>();

    Lock imagesLock = new ReentrantLock();

    /**
     * v1 to v2 = not supported
     * v2 to v3 = add 4 more EastNorth coordinates in GeorefImages
     * v3 to v4 = add original raster image width and height
     */
    protected final int serializeFormatVersion = 4;

    static int currentFormat;

    private ArrayList<EastNorthBound> dividedBbox = new ArrayList<>();

    private String location = "";

    private String departement = "";

    private String codeCommune = "";

    EastNorthBound communeBBox = new EastNorthBound(new EastNorth(0, 0), new EastNorth(0, 0));

    private boolean isRaster;
    private boolean isAlreadyGeoreferenced;
    double X0, Y0, angle, fX, fY;

    // bbox of the georeferenced raster image (the nice horizontal and vertical box)
    private EastNorth rasterMin;
    private EastNorth rasterMax;
    private double rasterRatio;

    // offset for vector images temporarily shifted (correcting Cadastre artifacts), in pixels
    double deltaEast;
    double deltaNorth;

    private Action saveAsPng;

    private Action cancelGrab;

    private Action refineGeoRef;

    class ResetOffsetActionMenu extends JosmAction {
        ResetOffsetActionMenu() {
            super(tr("Reset offset"), null, tr("Reset offset (only vector images)"), null, false);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            deltaEast = 0;
            deltaNorth = 0;
            invalidate();
        }
    }

    public boolean adjustModeEnabled;

    public GrabThread grabThread;

    /**
     * Constructs a new {@code WMSLayer}.
     */
    public WMSLayer() {
        this(tr("Blank Layer"), "", -1);
    }

    public WMSLayer(String location, String codeCommune, int lambertZone) {
        super(buildName(location, codeCommune));
        this.location = location;
        this.codeCommune = codeCommune;
        this.lambertZone = lambertZone;
        grabThread = new GrabThread(this);
        grabThread.start();
        // enable auto-sourcing option
        CadastrePlugin.pluginUsed = true;
    }

    @Override
    public synchronized void destroy() {
        // if the layer is currently saving the images in the cache, wait until it's finished
        if (grabThread != null)
                grabThread.cancel();
        grabThread = null;
        super.destroy();
        images = null;
        dividedBbox = null;
        Logging.info("Layer "+location+" destroyed");
    }

    private static String buildName(String location, String codeCommune) {
        String ret = location.toUpperCase(Locale.FRANCE);
        if (codeCommune != null && !codeCommune.isEmpty())
            ret += "(" + codeCommune + ")";
        return ret;
    }

    private String rebuildName() {
        return buildName(this.location.toUpperCase(Locale.FRANCE), this.codeCommune);
    }

    public void grab(Bounds b) throws IOException {
        grabThread.setCanceled(false);
        grabThread.setGrabber(grabber);
        // if it is the first layer, use the communeBBox as grab bbox (and not divided)
        if (MainApplication.getLayerManager().getLayers().size() == 1) {
            final Bounds bounds = this.getCommuneBBox().toBounds();
            GuiHelper.runInEDTAndWait(() -> MainApplication.getMap().mapView.zoomTo(bounds));
            divideBbox(bounds, 1);
        } else {
            if (isRaster) {
                divideBbox(new Bounds(ProjectionRegistry.getProjection().eastNorth2latlon(rasterMin),
                        ProjectionRegistry.getProjection().eastNorth2latlon(rasterMax)),
                        Integer.parseInt(Config.getPref().get("cadastrewms.rasterDivider", CadastrePreferenceSetting.DEFAULT_RASTER_DIVIDER)));
            } else
                divideBbox(b,
                        Integer.parseInt(Config.getPref().get("cadastrewms.scale", CadastrePreferenceSetting.DEFAULT_GRAB_MULTIPLIER)));
        }
        grabThread.addImages(dividedBbox);
    }

    /**
     * Divides the bounding box in smaller squares. Their size (and quantity) is configurable in Preferences.
     *
     * @param b      the original bbox, usually the current bbox on screen
     * @param factor 1 = source bbox 1:1
     *               2 = source bbox divided by 2x2 smaller boxes
     *               3 = source bbox divided by 3x3 smaller boxes
     *               4 = configurable size from preferences (100 meters per default) rounded
     *                   allowing grabbing of next contiguous zone
     */
    private void divideBbox(Bounds b, int factor) {
        EastNorth lambertMin = ProjectionRegistry.getProjection().latlon2eastNorth(b.getMin());
        EastNorth lambertMax = ProjectionRegistry.getProjection().latlon2eastNorth(b.getMax());
        double minEast = lambertMin.east()+deltaEast;
        double minNorth = lambertMin.north()+deltaNorth;
        double dEast = (lambertMax.east() - minEast) / factor;
        double dNorth = (lambertMax.north() - minNorth) / factor;
        dividedBbox.clear();
        if (factor < 4 || isRaster) {
            for (int xEast = 0; xEast < factor; xEast++) {
                for (int xNorth = 0; xNorth < factor; xNorth++) {
                    dividedBbox.add(new EastNorthBound(new EastNorth(minEast + xEast * dEast, minNorth + xNorth * dNorth),
                                new EastNorth(minEast + (xEast + 1) * dEast, minNorth + (xNorth + 1) * dNorth)));
                }
            }
        } else {
            // divide to fixed size squares
            // grab all square in a spiral starting from the center (usually the most interesting place)
            int c = Integer.parseInt(Config.getPref().get("cadastrewms.squareSize",
                    String.valueOf(CadastrePreferenceSetting.DEFAULT_SQUARE_SIZE)));
            lambertMin = lambertMin.add(-minEast % c, -minNorth % c);
            lambertMax = lambertMax.add(c - lambertMax.east() % c, c - lambertMax.north() % c);
            EastNorth mid = lambertMax.getCenter(lambertMin);
            mid = mid.add(-1, 1); // in case the boxes side is a pair, select the one one top,left to follow the rotation
            mid = mid.add(-mid.east() % c, -mid.north() % c);
            int x = (int) (lambertMax.east() -lambertMin.east())/c;
            int y = (int) (lambertMax.north() -lambertMin.north())/c;
            int[] dx = {+1, 0, -1, 0};
            int[] dy = {0, -1, 0, +1};
            int currDir = -1, lDir = 1, i = 1, j = 0, k = -1;
            if (x == 1)
                currDir = 0;
            dividedBbox.add(new EastNorthBound(mid, new EastNorth(mid.east()+c, mid.north()+c)));
            while (i < (x*y)) {
                i++;
                j++;
                if (j >= lDir) {
                    k++;
                    if (k > 1) {
                        lDir++;
                        k = 0;
                    }
                    j = 0;
                    currDir = (currDir+1) % 4;
                } else if (currDir >= 0 && j >= (currDir == 0 || currDir == 2 ? (x-1) : (y-1))) {
                    // the overall is a rectangle, not a square. Jump to the other side to grab next square.
                    k++;
                    if (k > 1) {
                        lDir++;
                        k = 0;
                    }
                    j = lDir-1;
                    currDir = (currDir+1) % 4;
                    mid = new EastNorth(mid.east() + dx[currDir]*c*(lDir-1), mid.north() + dy[currDir]*c*(lDir-1));
                }
                mid = new EastNorth(mid.east() + dx[currDir]*c, mid.north() + dy[currDir]*c);
                dividedBbox.add(new EastNorthBound(mid, new EastNorth(mid.east()+c, mid.north()+c)));
            }
        }
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getToolTipText() {
        String str = tr("WMS layer ({0}), {1} tile(s) loaded", getName(), images.size());
        if (isRaster) {
            str += "\n"+tr("Is not vectorized.");
            str += "\n"+tr("Bounding box: {0}", communeBBox);
            if (!images.isEmpty())
                str += "\n"+tr("Image size (px): {0}/{1}", images.get(0).image.getWidth(), images.get(0).image.getHeight());
        } else {
            str += "\n"+tr("Is vectorized.");
            str += "\n"+tr("Commune bbox: {0}", communeBBox);
        }
        return str;
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
        // Do nothing
    }

    @Override
    public void paint(Graphics2D g, final MapView mv, Bounds bounds) {
        synchronized (this) {
            Object savedInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            if (savedInterpolation == null) savedInterpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
            String interpolation = Config.getPref().get("cadastrewms.imageInterpolation", "standard");
            if (interpolation.equals("bilinear"))
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            else if (interpolation.equals("bicubic"))
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            else
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            imagesLock.lock();
            try {
                for (GeorefImage img : images) {
                    img.paint(g, mv, CadastrePlugin.backgroundTransparent,
                            CadastrePlugin.transparency, CadastrePlugin.drawBoundaries);
                }
            } finally {
                imagesLock.unlock();
            }
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, savedInterpolation);
        }
        if (this.isRaster) {
            paintCrosspieces(g, mv);
        }
        grabThread.paintBoxesToGrab(g, mv);
        if (this.adjustModeEnabled) {
            WMSAdjustAction.paintAdjustFrames(g, mv);
        }
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        for (GeorefImage img : images) {
            v.visit(img.min);
            v.visit(img.max);
        }
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Action[] getMenuEntries() {
        saveAsPng = new MenuActionSaveRasterAs(this);
        saveAsPng.setEnabled(isRaster);
        cancelGrab = new MenuActionCancelGrab(this);
        cancelGrab.setEnabled(!isRaster && grabThread.getImagesToGrabSize() > 0);
        refineGeoRef = new MenuActionRefineGeoRef(this);
        refineGeoRef.setEnabled(isRaster && grabThread.getImagesToGrabSize() == 0);
        Action resetOffset = new ResetOffsetActionMenu();
        resetOffset.setEnabled(!isRaster && !images.isEmpty() && (deltaEast != 0.0 || deltaNorth != 0.0));
        return new Action[] {
                LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(),
                new MenuActionLoadFromCache(),
                saveAsPng,
                cancelGrab,
                refineGeoRef,
                resetOffset,
                new LayerListPopup.InfoAction(this),
        };
    }

    public GeorefImage findImage(EastNorth eastNorth) {
        // Iterate in reverse, so we return the image which is painted last.
        // (i.e. the topmost one)
        for (int i = images.size() - 1; i >= 0; i--) {
            if (images.get(i).contains(eastNorth)) {
                return images.get(i);
            }
        }
        return null;
    }

    public boolean isOverlapping(Bounds bounds) {
        GeorefImage georefImage =
            new GeorefImage(null,
            ProjectionRegistry.getProjection().latlon2eastNorth(bounds.getMin()),
            ProjectionRegistry.getProjection().latlon2eastNorth(bounds.getMax()), this);
        for (GeorefImage img : images) {
            if (img.overlap(georefImage))
                return true;
        }
        return false;
    }

    /**
     * Convert the eastNorth input coordinates to raster coordinates.
     * The original raster size is [0,0,12286,8730] where 0,0 is the upper left corner and
     * 12286,8730 is the approx. raster max size.
     * @param min min east/north
     * @param max max east/north
     * @return the raster coordinates for the wms server request URL (minX,minY,maxX,maxY)
     */
    public String eastNorth2raster(EastNorth min, EastNorth max) {
        double minX = (min.east() - rasterMin.east()) / rasterRatio;
        double minY = (min.north() - rasterMin.north()) / rasterRatio;
        double maxX = (max.east() - rasterMin.east()) / rasterRatio;
        double maxY = (max.north() - rasterMin.north()) / rasterRatio;
        return minX+","+minY+","+maxX+","+maxY;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        setName(rebuildName());
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getCodeCommune() {
        return codeCommune;
    }

    public void setCodeCommune(String codeCommune) {
        this.codeCommune = codeCommune;
        setName(rebuildName());
    }

    public boolean isRaster() {
        return isRaster;
    }

    public void setRaster(boolean isRaster) {
        this.isRaster = isRaster;
        if (saveAsPng != null)
            saveAsPng.setEnabled(isRaster);
    }

    public boolean isAlreadyGeoreferenced() {
        return isAlreadyGeoreferenced;
    }

    public void setAlreadyGeoreferenced(boolean isAlreadyGeoreferenced) {
        this.isAlreadyGeoreferenced = isAlreadyGeoreferenced;
    }

    /**
     * Set raster positions used for grabbing and georeferencing.
     * rasterMin is the Eaast North of bottom left corner raster image on the screen when image is grabbed.
     * The bounds width and height are the raster width and height. The image width matches the current view
     * and the image height is adapted.
     * Required: the communeBBox must be set (normally it is catched by CadastreInterface and saved by DownloadWMSPlanImage)
     * @param bounds the current main map view boundaries
     */
    public void setRasterBounds(Bounds bounds) {
        EastNorth rasterCenter = ProjectionRegistry.getProjection().latlon2eastNorth(bounds.getCenter());
        EastNorth eaMin = ProjectionRegistry.getProjection().latlon2eastNorth(bounds.getMin());
        EastNorth eaMax = ProjectionRegistry.getProjection().latlon2eastNorth(bounds.getMax());
        double rasterSizeX = communeBBox.max.getX() - communeBBox.min.getX();
        double rasterSizeY = communeBBox.max.getY() - communeBBox.min.getY();
        double ratio = rasterSizeY/rasterSizeX;
        // keep same ratio on screen as WMS bbox (stored in communeBBox)
        rasterMin = new EastNorth(eaMin.getX(), rasterCenter.getY()-(eaMax.getX()-eaMin.getX())*ratio/2);
        rasterMax = new EastNorth(eaMax.getX(), rasterCenter.getY()+(eaMax.getX()-eaMin.getX())*ratio/2);
        rasterRatio = (rasterMax.getX()-rasterMin.getX())/rasterSizeX;
    }

    /**
     * Called by CacheControl when a new cache file is created on disk.
     * Save only primitives to keep cache independent of software changes.
     * @param oos output stream
     * @throws IOException if any I/O error occurs
     */
    public void write(ObjectOutputStream oos) throws IOException {
        currentFormat = this.serializeFormatVersion;
        oos.writeInt(this.serializeFormatVersion);
        oos.writeObject(this.location);    // String
        oos.writeObject(this.codeCommune); // String
        oos.writeInt(this.lambertZone);
        oos.writeBoolean(this.isRaster);
        oos.writeBoolean(false); // previously buildingsOnly
        if (this.isRaster) {
            oos.writeDouble(this.rasterMin.getX());
            oos.writeDouble(this.rasterMin.getY());
            oos.writeDouble(this.rasterMax.getX());
            oos.writeDouble(this.rasterMax.getY());
            oos.writeDouble(this.rasterRatio);
        }
        oos.writeDouble(this.communeBBox.min.getX());
        oos.writeDouble(this.communeBBox.min.getY());
        oos.writeDouble(this.communeBBox.max.getX());
        oos.writeDouble(this.communeBBox.max.getY());
    }

    /**
     * Called by CacheControl when a cache file is read from disk.
     * Cache uses only primitives to stay independent of software changes.
     * @param ois input stream
     * @param currentLambertZone current Lambert zone
     * @return {@code true} for success
     * @throws IOException if any I/O error occurs
     * @throws ClassNotFoundException if class of a serialized object cannot be found
     */
    public boolean read(ObjectInputStream ois, int currentLambertZone) throws IOException, ClassNotFoundException {
        currentFormat = ois.readInt();
        if (currentFormat < 2) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("Unsupported cache file version; found {0}, expected {1}\nCreate a new one.",
                    currentFormat, this.serializeFormatVersion), tr("Cache Format Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        this.setLocation((String) ois.readObject());
        this.setCodeCommune((String) ois.readObject());
        this.lambertZone = ois.readInt();
        this.setRaster(ois.readBoolean());
        if (currentFormat >= 4)
            ois.readBoolean();
        if (this.isRaster) {
            double X = ois.readDouble();
            double Y = ois.readDouble();
            this.rasterMin = new EastNorth(X, Y);
            X = ois.readDouble();
            Y = ois.readDouble();
            this.rasterMax = new EastNorth(X, Y);
            this.rasterRatio = ois.readDouble();
        }
        double minX = ois.readDouble();
        double minY = ois.readDouble();
        double maxX = ois.readDouble();
        double maxY = ois.readDouble();
        this.communeBBox = new EastNorthBound(new EastNorth(minX, minY), new EastNorth(maxX, maxY));
        if (this.lambertZone != currentLambertZone && currentLambertZone != -1) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Lambert zone {0} in cache "+
                    "incompatible with current Lambert zone {1}",
                    this.lambertZone+1, currentLambertZone), tr("Cache Lambert Zone Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        synchronized (this) {
            boolean EOF = false;
            try {
                while (!EOF) {
                    GeorefImage newImage = (GeorefImage) ois.readObject();
                    for (GeorefImage img : this.images) {
                        if (CadastrePlugin.backgroundTransparent) {
                            if (img.overlap(newImage))
                                // mask overlapping zone in already grabbed image
                                img.withdraw(newImage);
                            else
                                // mask overlapping zone in new image only when
                                // new image covers completely the existing image
                                newImage.withdraw(img);
                        }
                    }
                    newImage.wmsLayer = this;
                    this.images.add(newImage);
                }
            } catch (EOFException ex) {
                // expected exception when all images are read
                Logging.trace(ex);
            }
        }
        Logging.info("Cache loaded for location "+location+" with "+images.size()+" images");
        return true;
    }

    /**
     * Join the grabbed images into one single.
     */
    public void joinBufferedImages() {
        if (images.size() > 1) {
            EastNorth min = images.get(0).min;
            EastNorth max = images.get(images.size()-1).max;
            int oldImgWidth = images.get(0).image.getWidth();
            int oldImgHeight = images.get(0).image.getHeight();
            HashSet<Double> lx = new HashSet<>();
            HashSet<Double> ly = new HashSet<>();
            for (GeorefImage img : images) {
                lx.add(img.min.east());
                ly.add(img.min.north());
            }
            int newWidth = oldImgWidth*lx.size();
            int newHeight = oldImgHeight*ly.size();
            BufferedImage newImg = new BufferedImage(newWidth, newHeight, images.get(0).image.getType()/*BufferedImage.TYPE_INT_ARGB*/);
            Graphics g = newImg.getGraphics();
            // Coordinate (0,0) is on top,left corner where images are grabbed from bottom left
            int rasterDivider = (int) Math.sqrt(images.size());
            for (int h = 0; h < lx.size(); h++) {
                for (int v = 0; v < ly.size(); v++) {
                    int newx = h*oldImgWidth;
                    int newy = newHeight - oldImgHeight - (v*oldImgHeight);
                    int j = h*rasterDivider + v;
                    g.drawImage(images.get(j).image, newx, newy, this);
                }
            }
            synchronized (this) {
                images.clear();
                images.add(new GeorefImage(newImg, min, max, this));
            }
        }
    }

    /**
     * Image cropping based on two EN coordinates pointing to two corners in diagonal
     * Because it's coming from user mouse clics, we have to sort de positions first.
     * Works only for raster image layer (only one image in collection).
     * Updates layer georeferences.
     * @param en1 first east/north
     * @param en2 second east/north
     */
    public void cropImage(EastNorth en1, EastNorth en2) {
        // adj1 is corner bottom, left
        EastNorth adj1 = new EastNorth(en1.east() <= en2.east() ? en1.east() : en2.east(),
                en1.north() <= en2.north() ? en1.north() : en2.north());
        // adj2 is corner top, right
        EastNorth adj2 = new EastNorth(en1.east() > en2.east() ? en1.east() : en2.east(),
                en1.north() > en2.north() ? en1.north() : en2.north());
        images.get(0).crop(adj1, adj2);
        // update the layer georefs
        rasterMin = adj1;
        rasterMax = adj2;
        setCommuneBBox(new EastNorthBound(
                new EastNorth(0, 0),
                new EastNorth(images.get(0).image.getWidth()-1, images.get(0).image.getHeight()-1)));
        rasterRatio = (rasterMax.getX()-rasterMin.getX())/(communeBBox.max.getX() - communeBBox.min.getX());
    }

    public EastNorthBound getCommuneBBox() {
        return communeBBox;
    }

    public EastNorthBound getFirstViewFromCacheBBox() {
        if (isRaster) {
            return communeBBox;
        }
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (GeorefImage image:images) {
            minX = image.min.east() < minX ? image.min.east() : minX;
            maxX = image.max.east() > maxX ? image.max.east() : maxX;
            minY = image.min.north() < minY ? image.min.north() : minY;
            maxY = image.max.north() > maxY ? image.max.north() : maxY;
        }
        return new EastNorthBound(new EastNorth(minX, minY), new EastNorth(maxX, maxY));
    }

    public void setCommuneBBox(EastNorthBound entireCommune) {
        this.communeBBox = entireCommune;
    }

    /**
     * Method required by ImageObserver when drawing an image
     */
    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return false;
    }

    public int getLambertZone() {
        return lambertZone;
    }

    public EastNorth getRasterCenter() {
        return new EastNorth((images.get(0).max.east()+images.get(0).min.east())/2,
                (images.get(0).max.north()+images.get(0).min.north())/2);
    }

    public void displace(double dx, double dy) {
        if (isRaster) {
            this.rasterMin = new EastNorth(rasterMin.east() + dx, rasterMin.north() + dy);
            this.rasterMax = new EastNorth(rasterMax.east() + dx, rasterMax.north() + dy);
            images.get(0).shear(dx, dy);
        } else {
            deltaEast += dx;
            deltaNorth += dy;
        }
    }

    public void resize(EastNorth rasterCenter, double proportion) {
        this.rasterMin = rasterMin.interpolate(rasterCenter, proportion);
        this.rasterMax = rasterMax.interpolate(rasterCenter, proportion);
        images.get(0).scale(rasterCenter, proportion);
    }

    public void rotate(EastNorth rasterCenter, double angle) {
        this.rasterMin = rasterMin.rotate(rasterCenter, angle);
        this.rasterMax = rasterMax.rotate(rasterCenter, angle);
        images.get(0).rotate(rasterCenter, angle);
        this.angle += angle;
    }

    private void paintCrosspieces(Graphics g, MapView mv) {
        String crosspieces = Config.getPref().get("cadastrewms.crosspieces", "0");
        if (!crosspieces.equals("0")) {
            int modulo = 25;
            if (crosspieces.equals("2")) modulo = 50;
            if (crosspieces.equals("3")) modulo = 100;
            EastNorthBound currentView = new EastNorthBound(mv.getEastNorth(0, mv.getHeight()),
                    mv.getEastNorth(mv.getWidth(), 0));
            int minX = ((int) currentView.min.east()/modulo+1)*modulo;
            int minY = ((int) currentView.min.north()/modulo+1)*modulo;
            int maxX = ((int) currentView.max.east()/modulo)*modulo;
            int maxY = ((int) currentView.max.north()/modulo)*modulo;
            int size = (maxX-minX)/modulo;
            if (size < 20) {
                int px = size > 10 ? 2 : Math.abs(12-size);
                g.setColor(Color.green);
                for (int x = minX; x <= maxX; x += modulo) {
                    for (int y = minY; y <= maxY; y += modulo) {
                        Point p = mv.getPoint(new EastNorth(x, y));
                        g.drawLine(p.x-px, p.y, p.x+px, p.y);
                        g.drawLine(p.x, p.y-px, p.x, p.y+px);
                    }
                }
            }
        }
    }

    public GeorefImage getImage(int index) {
        imagesLock.lock();
        try {
            return images.get(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            Logging.error(e);
            return null;
        } finally {
            imagesLock.unlock();
        }
    }

    public Vector<GeorefImage> getImages() {
        return images;
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    public void addImage(GeorefImage img) {
        imagesLock.lock();
        try {
            images.add(img);
        } finally {
            imagesLock.unlock();
        }
    }

    public void setImages(Vector<GeorefImage> images) {
        imagesLock.lock();
        try {
            this.images = images;
        } finally {
            imagesLock.unlock();
        }
    }

    public void clearImages() {
        imagesLock.lock();
        try {
            images.clear();
        } finally {
            imagesLock.unlock();
        }
    }
}
