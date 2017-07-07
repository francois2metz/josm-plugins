// License: GPL. For details, see LICENSE file.

package org.openstreetmap.josm.plugins.pt_assistant.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.dialogs.relation.sort.RelationSorter;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopToWayAssigner;
import org.openstreetmap.josm.tools.Utils;

public class SortPTStopsAction extends JosmAction {

    private static final long serialVersionUID = 1714879296430852530L;
    private static final String ACTION_NAME = "Sort PT Stops";

    /**
     * Creates a new SortPTStopsAction
     */
    public SortPTStopsAction() {
        super(ACTION_NAME, "icons/sortptstops", ACTION_NAME, null, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Relation rel = (Relation) getLayerManager().getEditDataSet().getSelected().iterator().next();
        List<RelationMember> members = rel.getMembers();

        for (int i = 0; i < members.size(); i++) {
            rel.removeMember(0);
        }

        members = new RelationSorter().sortMembers(members);

        List<RelationMember> stops = new ArrayList<>();
        List<RelationMember> wayMembers = new ArrayList<>();
        List<Way> ways = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            RelationMember rm = members.get(i);
            String role = rm.getRole();
            if (role != null && (role.startsWith("platform") || role.startsWith("stop")))
                stops.add(rm);
            else {
                wayMembers.add(rm);
                if (rm.getType() == OsmPrimitiveType.WAY)
                    ways.add(rm.getWay());
            }
        }

        Map<String, PTStop> stopsByName = new HashMap<>();
        stops.forEach(rm -> {
            String name = getStopName(rm.getMember());
            if (name != null) {
                if (!stopsByName.containsKey(name))
                    stopsByName.put(name, new PTStop(rm));
                else
                    stopsByName.get(name).addStopElement(rm);
            }
        });

        StopToWayAssigner assigner = new StopToWayAssigner(ways);
        List<PTStop> ptstops = new ArrayList<>(stopsByName.values());
        Map<Way, List<PTStop>> wayStop = new HashMap<>();
        ptstops.forEach(stop -> {
            Way way = assigner.get(stop);
            if (!wayStop.containsKey(way))
                wayStop.put(way, new ArrayList<PTStop>());
            wayStop.get(way).add(stop);
        });

        wayMembers.forEach(wm -> {
            if (wm.getType() != OsmPrimitiveType.WAY)
                return;
            List<PTStop> stps = wayStop.get(wm.getWay());
            if (stps == null)
                return;
            stps.forEach(stop -> {
                if (stop != null) {
                    if (stop.getStopPositionRM() != null)
                        rel.addMember(stop.getStopPositionRM());
                    if (stop.getPlatformRM() != null)
                        rel.addMember(stop.getPlatformRM());
                }
            });
        });

        wayMembers.forEach(rel::addMember);
    }

    private static String getStopName(OsmPrimitive p) {
        for (Relation ref : Utils.filteredCollection(p.getReferrers(), Relation.class)) {
            if (ref.hasTag("type", "public_transport") && ref.hasTag("public_transport", "stop_area") && ref.getName() != null) {
                return ref.getName();
            }
        }
        return p.getName();
    }

    @Override
    protected void updateEnabledState(
            Collection<? extends OsmPrimitive> selection) {
        setEnabled(false);
        if (selection == null || selection.size() != 1)
            return;
        OsmPrimitive selected = selection.iterator().next();
        if (selected.getType() == OsmPrimitiveType.RELATION &&
                RouteUtils.isPTRoute((Relation) selected)) {
            setEnabled(true);
        }
    }
}
