// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.Vector;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.actions.StopImporterAction;
import org.openstreetmap.josm.plugins.public_transport.models.WaypointTableModel;

public class WaypointsDisableCommand extends Command {
    private Vector<Integer> workingLines = null;

    private Vector<Node> nodesForUndo = null;

    private WaypointTableModel waypointTM = null;

    public WaypointsDisableCommand(StopImporterAction controller) {
        super(MainApplication.getLayerManager().getEditDataSet());
        waypointTM = controller.getWaypointTableModel();
        workingLines = new Vector<>();
        nodesForUndo = new Vector<>();

        // use either selected lines or all lines if no line is selected
        int[] selectedLines = controller.getDialog().getWaypointsTable().getSelectedRows();
        Vector<Integer> consideredLines = new Vector<>();
        if (selectedLines.length > 0) {
            for (int i = 0; i < selectedLines.length; ++i) {
                consideredLines.add(selectedLines[i]);
            }
        } else {
            for (int i = 0; i < waypointTM.getRowCount(); ++i) {
                consideredLines.add(Integer.valueOf(i));
            }
        }

        // keep only lines where a node can be added
        for (int i = 0; i < consideredLines.size(); ++i) {
            if (waypointTM.nodes.elementAt(consideredLines.elementAt(i)) != null)
                workingLines.add(consideredLines.elementAt(i));
        }
    }

    @Override
    public boolean executeCommand() {
        nodesForUndo.clear();
        for (int i = 0; i < workingLines.size(); ++i) {
            int j = workingLines.elementAt(i).intValue();
            Node node = waypointTM.nodes.elementAt(j);
            nodesForUndo.add(node);
            if (node == null)
                continue;
            waypointTM.nodes.set(j, null);
            MainApplication.getLayerManager().getEditDataSet().removePrimitive(node);
            node.setDeleted(true);
        }
        return true;
    }

    @Override
    public void undoCommand() {
        for (int i = 0; i < workingLines.size(); ++i) {
            int j = workingLines.elementAt(i).intValue();
            Node node = nodesForUndo.elementAt(i);
            waypointTM.nodes.set(j, node);
            if (node == null)
                continue;
            node.setDeleted(false);
            MainApplication.getLayerManager().getEditDataSet().addPrimitive(node);
        }
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Disable waypoints");
    }
}
