package org.openstreetmap.josm.plugins.czechaddress.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.ListDataEvent;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.czechaddress.MapUtils;
import org.openstreetmap.josm.plugins.czechaddress.NotNullList;
import org.openstreetmap.josm.plugins.czechaddress.PrimUtils;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.ReasonerListener;
import org.openstreetmap.josm.plugins.czechaddress.gui.utils.UniversalListRenderer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Dialog for displaying and handling conflicts.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class ConflictResolver extends ExtendedDialog {

    private static ConflictResolver singleton = null;
    public static ConflictResolver getInstance() {
        if (singleton == null)
            singleton = new ConflictResolver();
        return singleton;
    }

    public static Logger logger = Logger.getLogger(ConflictResolver.class.getName());

    /**
     * Creates new dialog, but does not display it, nor hook to messages.
     */
    private ConflictResolver() {

        super(Main.parent, "Řešení konfliktů", new String[] {}, true);
        initComponents();

        mainField.setModel(conflictModel);
        Reasoner.getInstance().addListener(new ReasonerHook());

        // Create those lovely 'zoom' icons for professional look.
        Icon zoomIcon = ImageProvider.get("zoom.png");
        mainZoomButton.setIcon(zoomIcon); mainZoomButton.setText("");
        candZoomButton.setIcon(zoomIcon); candZoomButton.setText("");

        Icon cursorIcon = ImageProvider.get("cursor.png");
        mainPickButton.setIcon(cursorIcon); mainPickButton.setText("");
        candPickButton.setIcon(cursorIcon); candPickButton.setText("");

        // And finalize initializing the form.
        setupDialog(mainPanel, new String[] {});
        // TODO: Why does it always crash if the modality is set in constructor?
        setModal(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        candZoomButton = new javax.swing.JButton();
        mainLabel = new javax.swing.JLabel();
        reassignButton = new javax.swing.JButton();
        candLabel = new javax.swing.JLabel();
        mainZoomButton = new javax.swing.JButton();
        mainField = new javax.swing.JComboBox();
        candField = new javax.swing.JComboBox();
        candPickButton = new javax.swing.JButton();
        mainPickButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        candZoomButton.setText("     ");
        candZoomButton.setEnabled(false);
        candZoomButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                candZoomButtonActionPerformed(evt);
            }
        });

        mainLabel.setText("Nejednoznačný prvek:");

        reassignButton.setText("Určit jako nejlepší přiřažení");
        reassignButton.setEnabled(false);
        reassignButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reassignButtonActionPerformed(evt);
            }
        });

        candLabel.setText("Kandidáti na přiřazení:");

        mainZoomButton.setText("     ");
        mainZoomButton.setEnabled(false);
        mainZoomButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainZoomButtonActionPerformed(evt);
            }
        });

        mainField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        mainField.setRenderer(new UniversalListRenderer());

        candField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " " }));
        candField.setRenderer(new UniversalListRenderer());

        candPickButton.setText("     ");
        candPickButton.setEnabled(false);
        candPickButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                candPickButtonActionPerformed(evt);
            }
        });

        mainPickButton.setText("     ");
        mainPickButton.setEnabled(false);
        mainPickButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainPickButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(candLabel)
                    .addComponent(mainLabel))
                .addGap(6, 6, 6)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(candField, javax.swing.GroupLayout.Alignment.TRAILING, 0, 430, Short.MAX_VALUE)
                    .addComponent(mainField, 0, 430, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(candZoomButton)
                    .addComponent(mainZoomButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainPickButton)
                    .addComponent(candPickButton)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reassignButton))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(mainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(mainLabel))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(mainZoomButton)
                            .addComponent(mainPickButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(candPickButton)
                            .addComponent(candZoomButton)
                            .addComponent(candField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(candLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(reassignButton))
        );

        getContentPane().add(mainPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mainZoomButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainZoomButtonActionPerformed
        zoomTo(mainField.getSelectedItem());
    }//GEN-LAST:event_mainZoomButtonActionPerformed

    private void candZoomButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_candZoomButtonActionPerformed
        zoomTo(candField.getSelectedItem());
    }//GEN-LAST:event_candZoomButtonActionPerformed

    private void zoomTo(Object item) {
        if (item instanceof OsmPrimitive)
            MapUtils.zoomTo((OsmPrimitive) item);

        else if (item instanceof AddressElement) {
            OsmPrimitive prim = Reasoner.getInstance().translate((AddressElement) item);
            if (prim != null)
                MapUtils.zoomTo(prim);
        }
    }

    public void focusElement(AddressElement elem) {
        int index = Collections.binarySearch(conflictModel.elements, elem);
        if (index >= 0) {
            mainField.setSelectedIndex(index);
            mainField.repaint();
            setVisible(true);
        }
    }

    private void reassignButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reassignButtonActionPerformed

        Reasoner r = Reasoner.getInstance();

        synchronized (r) {
            r.openTransaction();

            if (conflictModel.getSelectedItem() instanceof OsmPrimitive) {
                OsmPrimitive prim = (OsmPrimitive) conflictModel.getSelectedItem();

                if (r.translate(prim) != null)
                    r.unOverwrite(prim, r.translate(prim));
                
                ComboBoxModel model = candField.getModel();
                for (int i = 0; i < model.getSize(); i++) {
                    AddressElement elem = (AddressElement) model.getElementAt(i);
                    r.unOverwrite(prim, elem);
                    if (r.translate(elem) != null)
                        r.unOverwrite(r.translate(elem), elem);
                }
                
                r.doOverwrite(prim, (AddressElement) model.getSelectedItem());
            }

            if (conflictModel.getSelectedItem() instanceof AddressElement) {
                AddressElement elem = (AddressElement) conflictModel.getSelectedItem();

                if (r.translate(elem) != null)
                    r.unOverwrite(r.translate(elem), elem);

                ComboBoxModel model = candField.getModel();
                for (int i = 0; i < model.getSize(); i++) {
                    OsmPrimitive prim = (OsmPrimitive) model.getElementAt(i);
                    r.unOverwrite(prim, elem);
                    if (r.translate(prim) != null)
                        r.unOverwrite(prim, r.translate(prim));
                }

                r.doOverwrite((OsmPrimitive) model.getSelectedItem(), elem);
            }

            r.closeTransaction();
        }
    }//GEN-LAST:event_reassignButtonActionPerformed

    private void mainPickButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainPickButtonActionPerformed
        if (mainField.getSelectedItem() instanceof House)
            FactoryDialog.getInstance().setSelectedHouse((House) mainField.getSelectedItem());
    }//GEN-LAST:event_mainPickButtonActionPerformed

    private void candPickButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_candPickButtonActionPerformed
        if (candField.getSelectedItem() instanceof House)
            FactoryDialog.getInstance().setSelectedHouse((House) candField.getSelectedItem());
    }//GEN-LAST:event_candPickButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox candField;
    private javax.swing.JLabel candLabel;
    private javax.swing.JButton candPickButton;
    private javax.swing.JButton candZoomButton;
    private javax.swing.JComboBox mainField;
    private javax.swing.JLabel mainLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton mainPickButton;
    private javax.swing.JButton mainZoomButton;
    private javax.swing.JButton reassignButton;
    // End of variables declaration//GEN-END:variables

//==============================================================================

    /**
     * Listenes to the {@link Reasoner} and updates data models.
     */
    private class ReasonerHook implements ReasonerListener {

        public void elementChanged(AddressElement elem) {
            if (!(elem instanceof House)) return;
            logger.log(Level.FINER, "hook: element changed", elem.getName());

            if (Reasoner.getInstance().inConflict(elem))
                conflictModel.put(elem);
            else
                conflictModel.remove(elem);
        }

        public void primitiveChanged(OsmPrimitive prim) {
            if (!House.isMatchable(prim)) return;
            logger.log(Level.FINER, "hook: primitive changed", AddressElement.getName(prim));

            if (Reasoner.getInstance().inConflict(prim))
                conflictModel.put(prim);
            else
                conflictModel.remove(prim);
        }

        public void resonerReseted() {
        }
    }

//==============================================================================

    private ConflictsModel conflictModel = new ConflictsModel();
    private class ConflictsModel implements ComboBoxModel {

        ArrayList<AddressElement> elements = new ArrayList<AddressElement>();
        ArrayList<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
        Set<ListDataListener> listeners = new HashSet<ListDataListener>();

        Object selected = null;

        public void put(AddressElement elem) {
            int index = Collections.binarySearch(elements, elem);
            if (index < 0) {
                logger.log(Level.FINE, "conflicts: adding element",
                                        "[" + String.valueOf(-index-1) + "]=„"
                                        + elem.getName() + "“");
                elements.add(-index-1, elem);

                ListDataEvent evt = new ListDataEvent(this,
                        ListDataEvent.INTERVAL_ADDED,
                        -index-1, -index-1);
                for (ListDataListener listener : listeners)
                    listener.intervalAdded(evt);

                if (mainField.getSelectedItem() == null)
                    mainField.setSelectedIndex(-index-1);
            }
        }

        public void put(OsmPrimitive prim) {
            int index = Collections.binarySearch(primitives, prim, PrimUtils.comparator);
            if (index < 0) {
                logger.log(Level.FINE, "conflicts: adding primitive",
                                       "["+String.valueOf(-index-1)+"]=„"
                                        +AddressElement.getName(prim) + "“");
                primitives.add(-index-1, prim);
                
                ListDataEvent evt = new ListDataEvent(this,
                        ListDataEvent.INTERVAL_ADDED,
                        -index-1 + elements.size(), -index-1 + elements.size());
                for (ListDataListener listener : listeners)
                    listener.intervalAdded(evt);
                
                if (mainField.getSelectedItem() == null)
                    mainField.setSelectedIndex(-index-1);
            }
        }

        public void remove(AddressElement elem) {
            int index = Collections.binarySearch(elements, elem);
            //index = primitives.indexOf(elem);
            if (index >= 0) {
                logger.log(Level.FINE, "conflicts: removing element",
                                        "[" + String.valueOf(index) + "]=„"
                                        + elem.getName() + "“");
                elements.remove(index);
                
                if (selected == elem)
                    setSelectedItem(null);

                ListDataEvent evt = new ListDataEvent(this,
                        ListDataEvent.INTERVAL_REMOVED,
                        index, index);
                for (ListDataListener listener : listeners)
                    listener.intervalRemoved(evt);
            }
        }

        public void remove(OsmPrimitive prim) {
            int index = Collections.binarySearch(primitives, prim);
            index = primitives.indexOf(prim);
            if (index >= 0) {
                logger.log(Level.FINE, "conflicts: removing primitive",
                                        "[" + String.valueOf(index) + "]=„"
                                        + AddressElement.getName(prim) + "“");
                primitives.remove(index);

                if (selected == prim)
                    setSelectedItem(null);
                
                ListDataEvent evt = new ListDataEvent(this,
                        ListDataEvent.INTERVAL_REMOVED,
                        index + elements.size(), index + elements.size());
                for (ListDataListener listener : listeners)
                    listener.intervalRemoved(evt);
            }
        }

        public void clear() {
            logger.log(Level.FINE, "conflicts: clearing");

            ListDataEvent evt = new ListDataEvent(this,
                    ListDataEvent.CONTENTS_CHANGED,
                    0, elements.size() + primitives.size() - 1);

            elements.clear();
            primitives.clear();
            
            for (ListDataListener listener : listeners)
                listener.contentsChanged(evt);
        }
        
        public void setSelectedItem(Object anItem) {

            if (anItem == null && getSize() > 0) {
                mainField.setSelectedIndex(0);
                return;
            }
            selected = anItem;
            updateZoomButtons(selected, mainZoomButton);
            updatePickButtons(selected, mainPickButton);
            updateCandidatesModel(anItem);
        }

        public Object getSelectedItem() {
            return selected;
        }

        public int getSize() {
            return primitives.size() + elements.size();
        }

        public Object getElementAt(int index) {
            if (index< elements.size())
                return elements.get(index);
            
            index -= elements.size();

            if (index< primitives.size())
                return primitives.get(index);
            
            return null;
        }

        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }
    }

    private void updateCandidatesModel(Object selected) {
        
        if (selected instanceof AddressElement) {
            AddressElement selElem = (AddressElement) selected;
            List<OsmPrimitive> conflPrims = new NotNullList<OsmPrimitive>();
            conflPrims.addAll(Reasoner.getInstance().getCandidates(selElem));
            Collections.sort(conflPrims, PrimUtils.comparator);
            candField.setModel(new CandidatesModel<OsmPrimitive>(conflPrims));

        } else if (selected instanceof OsmPrimitive) {
            OsmPrimitive selElem = (OsmPrimitive) selected;
            List<AddressElement> conflElems = new NotNullList<AddressElement>();
            conflElems.addAll(Reasoner.getInstance().getCandidates(selElem));
            Collections.sort(conflElems);
            candField.setModel(new CandidatesModel<AddressElement>(conflElems));

        } else {
            candField.setModel(new DefaultComboBoxModel());
            candZoomButton.setEnabled(false);
            reassignButton.setEnabled(false);
        }

        candZoomButton.setEnabled(false);
        candPickButton.setEnabled(false);
        reassignButton.setEnabled(false);
        if (candField.getModel().getSize() > 0)
            candField.setSelectedIndex(0);
        else
            candField.setSelectedIndex(-1);
    }

    private void updateZoomButtons(Object selected, JButton button) {
        if (selected instanceof OsmPrimitive) {
            button.setEnabled(true);
        } else if (selected instanceof AddressElement) {
            button.setEnabled(Reasoner.getInstance().translate(
                        (AddressElement) selected) != null);
        } else {
            button.setEnabled(false);
        }
    }

    private void updatePickButtons(Object selected, JButton button) {
        button.setEnabled(selected instanceof House || selected instanceof Street);
    }

    private class CandidatesModel<E> implements ComboBoxModel {

        Set<ListDataListener> listeners = new HashSet<ListDataListener>();
        List<? extends E> primitives;
        Object selected = null;

        public CandidatesModel(List<? extends E> data) {
            primitives = data;
        }

        public void setSelectedItem(Object anItem) {
            selected = anItem;
            updateZoomButtons(selected, candZoomButton);
            updatePickButtons(selected, candPickButton);

            if (conflictModel.getSelectedItem() instanceof AddressElement) {
                reassignButton.setEnabled( selected !=
                        Reasoner.getInstance().getStrictlyBest(
                            (AddressElement) conflictModel.getSelectedItem()));

                reassignButton.setEnabled(true);
            } else if (conflictModel.getSelectedItem() instanceof OsmPrimitive) {
                reassignButton.setEnabled( selected !=
                        Reasoner.getInstance().getStrictlyBest(
                            (OsmPrimitive) conflictModel.getSelectedItem()));
                reassignButton.setEnabled(true);
            } else
                reassignButton.setEnabled(false);


        }

        public Object getSelectedItem() {
            return selected;
        }

        public int getSize() {
            return primitives.size();
        }

        public Object getElementAt(int index) {
            if (index < primitives.size())
                return primitives.get(index);
            return null;
        }

        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }
    }
}
