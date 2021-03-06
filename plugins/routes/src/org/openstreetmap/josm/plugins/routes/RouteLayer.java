// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.routes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.routes.paint.NarrowLinePainter;
import org.openstreetmap.josm.plugins.routes.paint.PathPainter;
import org.openstreetmap.josm.plugins.routes.paint.WideLinePainter;
import org.openstreetmap.josm.plugins.routes.xml.RoutesXMLLayer;
import org.openstreetmap.josm.plugins.routes.xml.RoutesXMLRoute;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.ImageProvider;

public class RouteLayer extends Layer implements DataSetListenerAdapter.Listener {

    private final PathPainter pathPainter;
    private final PathBuilder pathBuilder = new PathBuilder();
    private final List<RouteDefinition> routes = new ArrayList<>();
    private volatile boolean datasetChanged = true;

    public RouteLayer(RoutesXMLLayer xmlLayer) {
        super(xmlLayer.getName());

        int index = 0;
        for (RoutesXMLRoute route:xmlLayer.getRoute()) {
            if (route.isEnabled()) {
                Color color = ColorHelper.html2color(route.getColor());
                if (color == null) {
                    color = Color.RED;
                    System.err.printf("Routes plugin - unable to convert color (%s)\n", route.getColor());
                }
                routes.add(new RouteDefinition(index++, color, route.getPattern()));
            }
        }

        if ("wide".equals(Config.getPref().get("routes.painter"))) {
            pathPainter = new WideLinePainter(this);
        } else {
            pathPainter = new NarrowLinePainter(this);
        }

        DatasetEventManager.getInstance().addDatasetListener(new DataSetListenerAdapter(this), FireMode.IMMEDIATELY);
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("layer", "osmdata_small");
    }

    @Override
    public Object getInfoComponent() {
        return null;
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[0];
    }

    @Override
    public String getToolTipText() {
        return "Hiking routes";
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
        // Merging is not supported
    }

    private void addRelation(Relation relation, RouteDefinition route) {
        for (RelationMember member:relation.getMembers()) {
            if (member.getMember() instanceof Way) {
                Way way = (Way) member.getMember();
                pathBuilder.addWay(way, route);
            }
        }
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {

        DataSet dataset = MainApplication.getLayerManager().getEditDataSet();

        if (dataset == null) {
            return;
        }

        if (datasetChanged) {
            datasetChanged = false;
            pathBuilder.clear();

            for (Relation relation:dataset.getRelations()) {
                for (RouteDefinition route:routes) {
                    if (route.matches(relation)) {
                        addRelation(relation, route);
                    }
                }
            }

            for (Way way:dataset.getWays()) {
                for (RouteDefinition route:routes) {
                    if (route.matches(way)) {
                        pathBuilder.addWay(way, route);
                    }
                }
            }
        }

        Stroke stroke = g.getStroke();
        Color color = g.getColor();
        for (ConvertedWay way:pathBuilder.getConvertedWays()) {
            pathPainter.drawWay(way, mv, g);
        }
        g.setStroke(stroke);
        g.setColor(color);

    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {

    }

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    @Override
    public void processDatasetEvent(AbstractDatasetChangedEvent event) {
        datasetChanged = true;
    }

    @Override
    public synchronized void destroy() {
        /* layer is reused, don't destroy it at all */
    }
}
