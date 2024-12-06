package mbarix4j.swing;

import javax.swing.ListModel;

/**
 * @author Brian Schlining
 * @since 2014-11-19T16:17:00
 */
public interface MutableListModel extends ListModel {
    public boolean isCellEditable(int index);
    public void setValueAt(Object value, int index);
}