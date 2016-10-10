/*
 * Copyright 2016  Generic Problem Solver Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package butt.gui.benchmark.treetable;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.util.Callback;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class CheckBoxTreeTableRow<T> extends TreeTableRow<T> {

    private CheckBox checkBox;

    private ObservableValue<Boolean> booleanProperty;

    private BooleanProperty indeterminateProperty;

    public CheckBoxTreeTableRow() {
        setSelectedStateCallback(item -> {
            if (item instanceof CheckBoxTreeItem<?>) {
                return ((CheckBoxTreeItem<?>) item).selectedProperty();
            }
            return null;
        });
        this.checkBox = new CheckBox();
        // something weird going on with layout
        checkBox.setAlignment(Pos.TOP_LEFT);
    }

    // --- selected state callback property
    private ObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>> selectedStateCallback = new SimpleObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>>(
            this, "selectedStateCallback");

    /**
     * Property representing the {@link Callback} that is bound to by the 
     * CheckBox shown on screen.
     */
    public final ObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>> selectedStateCallbackProperty() {
        return selectedStateCallback;
    }

    /** 
     * Sets the {@link Callback} that is bound to by the CheckBox shown on screen.
     */
    public final void setSelectedStateCallback(
            Callback<TreeItem<T>, ObservableValue<Boolean>> value) {
        selectedStateCallbackProperty().set(value);
    }

    /**
     * Returns the {@link Callback} that is bound to by the CheckBox shown on screen.
     */
    public final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedStateCallback() {
        return selectedStateCallbackProperty().get();
    }

    /** {@inheritDoc} */
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            //            
            TreeItem<T> treeItem = getTreeItem();
            // PENDING JW: this is nuts but working ..  certainly will pose problems
            // when re-using the cell
            treeItem.setGraphic(checkBox);
            // this is what CheckBoxTreeCell does, setting the graphic
            // of the tableRow confuses the layout
            //            checkBox.setGraphic(treeItem == null ? null : treeItem.getGraphic());
            //            setGraphic(checkBox);

            // uninstall bindings
            if (booleanProperty != null) {
                checkBox.selectedProperty()
                        .unbindBidirectional((BooleanProperty) booleanProperty);
            }
            if (indeterminateProperty != null) {
                checkBox.indeterminateProperty()
                        .unbindBidirectional(indeterminateProperty);
            }

            // install new bindings.
            // We special case things when the TreeItem is a CheckBoxTreeItem
            if (treeItem instanceof CheckBoxTreeItem) {
                CheckBoxTreeItem<T> cbti = (CheckBoxTreeItem<T>) treeItem;
                booleanProperty = cbti.selectedProperty();
                checkBox.selectedProperty()
                        .bindBidirectional((BooleanProperty) booleanProperty);

                indeterminateProperty = cbti.indeterminateProperty();
                checkBox.indeterminateProperty()
                        .bindBidirectional(indeterminateProperty);
            } else {
                throw new IllegalStateException(
                        "item must be CheckBoxTreeItem");
            }
        }

    }
}
