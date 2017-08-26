// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.ImageProvider;

import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class DuplicateChosenRelationAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public DuplicateChosenRelationAction(ChosenRelation rel) {
        super(tr("Duplicate relation"));
        putValue(SMALL_ICON, ImageProvider.get("duplicate"));
        putValue(SHORT_DESCRIPTION, tr("Create a copy of this relation and open it in another editor window"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(rel.get() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Relation r = rel.get();
        Relation copy = new Relation(r, true);
        MainApplication.undoRedo.add(new AddCommand(copy));
        rel.set(copy);
        if (MainApplication.getLayerManager().getEditDataSet() != null) {
            MainApplication.getLayerManager().getEditDataSet().setSelected(copy);
        }
    }

    @Override
    public void chosenRelationChanged(Relation oldRelation, Relation newRelation) {
        setEnabled(newRelation != null);
    }
}
