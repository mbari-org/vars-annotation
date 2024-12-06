/*
 * @(#)ListTableCellRenderer.java   2011.12.10 at 09:13:01 PST
 *
 * Copyright 2009 MBARI
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package mbarix4j.swing.table;

import mbarix4j.swing.ListListModel;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.util.WeakHashMap;

/**
 * <p>Base class for the <code>TableCellRenderer</code> used to display
 * <code>List</code>s in <code>the AnnotationGUI</code>
 *
 * @author <a href="http://www.mbari.org">MBARI</a>
 * @version $Id: ListTableCellRenderer.java 332 2006-08-01 18:38:46Z hohonuuli $
 */
public class ListTableCellRenderer extends JList implements TableCellRenderer {

    /**
     *
     */
    private static final long serialVersionUID = 6278328970815317715L;
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    /**
     * Store value references here to avoid unescessary object creation
     */
    private WeakHashMap map = new WeakHashMap();

    private Color unselectedBackground;
    private Color unselectedForeground;

    /**
     * Creates a default table cell renderer.
     */
    public ListTableCellRenderer() {
        super();
        setOpaque(true);
        setBorder(noFocusBorder);
    }

    /**
     * Returns the default table cell renderer.
     *
     * @param  table       the <code>JTable</code>
     * @param  value       the value to assign to the cell at <code>[row, column]</code>
     * @param  isSelected  true if cell is selected
     * @param  row         the row of the cell to render
     * @param  column      the column of the cell to render
     * @param  hasFocus    Description of the Parameter
     * @return             the default table cell renderer
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        if (isSelected) {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        }
        else {
            super.setForeground((unselectedForeground != null) ? unselectedForeground : table.getForeground());
            super.setBackground((unselectedBackground != null) ? unselectedBackground : table.getBackground());
        }

        setFont(table.getFont());

        if (hasFocus) {
            setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));

            if (table.isCellEditable(row, column)) {
                super.setForeground(UIManager.getColor("Table.focusCellForeground"));
                super.setBackground(UIManager.getColor("Table.focusCellBackground"));
            }
        }
        else {
            setBorder(noFocusBorder);
        }

        setValue(value);

        // ---- begin optimization to avoid painting background ----
        Color back = getBackground();
        boolean colorMatch = (back != null) && (back.equals(table.getBackground())) && table.isOpaque();
        setOpaque(!colorMatch);

        // ---- end optimization to aviod painting background ----
        return this;
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to assign the unselected-background color to the specified color.
     *
     * @param  c  set the background color to this value
     */
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        unselectedBackground = c;
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to assign the unselected-foreground color to the specified color.
     *
     * @param  c  set the foreground color to this value
     */
    @Override
    public void setForeground(Color c) {
        super.setForeground(c);
        unselectedForeground = c;
    }

    /**
     * Sets the string for the cell being rendered to <code>value</code>.
     *
     * @param  value  the <code>List</code> for this cell; if value is <code>null</code> it sets the value to an empty string
     * @see           JLabel#setText
     */
    protected void setValue(Object value) {
        Object listModel = map.get(value);
        if (listModel == null) {
            listModel = new ListListModel((java.util.List) value);
            map.put(value, listModel);
        }

        // ListModel listModel = new ListListModel((java.util.List) value);
        this.setModel((ListModel) listModel);
    }
}
