// License: GPL. For details, see LICENSE file.
package gpxfilter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class AddEGpxLayerAction extends JosmAction {

    public AddEGpxLayerAction() {
        super(tr("Add EGPX layer"), (String) null, tr("Add EGPX layer"),
            Shortcut.registerShortcut("gpxfilter:egpx", tr("Tool: {0}", tr("Add EGPX layer")),
                 KeyEvent.VK_X, Shortcut.ALT_SHIFT),
            true, "gpxfilter/addegpxlayer", true);
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getEditDataSet() != null);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        getLayerManager().addLayer(new EGpxLayer(MainApplication.getMap().mapView.getRealBounds()));
    }
}
