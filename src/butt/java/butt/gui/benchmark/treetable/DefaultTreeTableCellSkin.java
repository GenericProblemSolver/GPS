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

import com.sun.javafx.scene.control.skin.TreeTableCellSkin;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

/**
 * TreeTableCellSkin that handles row graphic in its leftPadding, if it is in
 * the treeColumn of the associated TreeTableView.
 * <p>
 * It assumes that per-row graphics - including the graphic of the TreeItem, if
 * any - is folded into the TreeTableRow graphic and patches its
 * leftLabelPadding to account for the graphic width.
 * <p>
 * 
 * Note: TableRowSkinBase seems to be designed to cope with variations of row
 * graphic - it has a method <code>graphicProperty()</code> that's always used
 * internally when calculating offsets in the treeColumn. Subclasses override as
 * needed, the layout code remains constant. The real problem is the
 * TreeTableCell hard-codes the TreeItem as the only graphic owner.
 * 
 * @author unknown, probably Jeanette Winzenburg, Berlin
 * @author haker@uni-bremen.de (added to the project)
 * 
 */
public class DefaultTreeTableCellSkin<S, T> extends TreeTableCellSkin<S, T> {

    /**
     * @param treeTableCell
     */
    public DefaultTreeTableCellSkin(TreeTableCell<S, T> treeTableCell) {
        super(treeTableCell);
    }

    /**
     * Overridden to adjust the padding returned by super for row graphic.
     */
    @Override
    protected double leftLabelPadding() {
        double padding = super.leftLabelPadding();
        padding += getRowGraphicPatch();
        return padding;
    }

    /**
     * Returns the patch for leftPadding if the tableRow has a graphic of its
     * own.
     * <p>
     * 
     * Note: this implemenation is a bit whacky as it relies on super's handling
     * of treeItems graphics offset. A cleaner implementation would override
     * leftLabelPadding from scratch.
     * <p>
     * PENDING JW: doooooo it!
     * 
     * @return
     */
    protected double getRowGraphicPatch() {
        if (!isTreeColumn()) {
            return 0;
        }
        Node graphic = getSkinnable().getTreeTableRow().getGraphic();
        if (graphic != null) {
            double height = getCellSize();
            // start with row's graphic
            double patch = graphic.prefWidth(height);
            // correct for super's having added treeItem's graphic
            TreeItem<S> item = getSkinnable().getTreeTableRow().getTreeItem();
            if (item.getGraphic() != null) {
                double correct = item.getGraphic().prefWidth(height);
                patch -= correct;
            }
            return patch;
        }
        return 0;
    }

    /**
     * Checks and returns whether our cell is attached to a treeTableView/column
     * and actually has a TreeItem.
     * 
     * @return
     */
    protected boolean isTreeColumn() {
        if (getSkinnable().isEmpty()) {
            return false;
        }
        TreeTableColumn<S, T> column = getSkinnable().getTableColumn();
        TreeTableView<S> view = getSkinnable().getTreeTableView();
        if (column.equals(view.getTreeColumn())) {
            return true;
        }
        return view.getVisibleLeafColumns().indexOf(column) == 0;
    }

}