// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.piclayer.layer.CalibrationFileFilter;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;

/**
 * Action to load the calibration file.
 *
 */
@SuppressWarnings("serial")
public class LoadPictureCalibrationAction extends JosmAction {

    // Owner layer of the action
    PicLayerAbstract m_owner = null;

    // Persistent FileChooser instance to remember last directory
    JFileChooser m_filechooser = null;

    /**
     * Constructor
     */
    public LoadPictureCalibrationAction(PicLayerAbstract owner) {
        super(tr("Load Picture Calibration..."), null, tr("Loads calibration data from a file"), null, false);
        // Remember the owner...
        m_owner = owner;
    }

    /**
     * Action handler
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        // Save dialog
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new CalibrationFileFilter());
        fc.setSelectedFile(new File(m_owner.getPicLayerName() + CalibrationFileFilter.EXTENSION));
        int result = fc.showOpenDialog(Main.parent);

        if (result == JFileChooser.APPROVE_OPTION) {

            // Load
            try {
                m_owner.loadCalibration(new FileInputStream(fc.getSelectedFile()));
            } catch (Exception e) {
                // Error
                e.printStackTrace();
                JOptionPane.showMessageDialog(Main.parent,
                        tr("Loading file failed: {0}", e.getMessage()), tr("Problem occurred"), JOptionPane.WARNING_MESSAGE);
            }
        }
    }

}
