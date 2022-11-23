package org.mbari.vars.ui.swing.annotable;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Observation;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.ui.javafx.annotable.FGSValue;
import org.mbari.vars.ui.swing.JIcons;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class FGSCellRenderer extends JPanel implements TableCellRenderer {


    // These should match those in annotation.css
    private static final Color defaultColor = Colors.DEFAULT.getColor();
    private static final Color concurrentColor = Colors.CONCURRENT.getColor();
    private static final Color imageColor = Colors.IMAGE.getColor();
    private static final Color jsonColor = Colors.JSON.getColor();
    private static final Color sampleColor = Colors.SAMPLE.getColor();


    private final JLabel imageLabel = buildLabel(Icons.IMAGE, imageColor, "labelImage");
    private final JLabel sampleLabel = buildLabel(Icons.NATURE_PEOPLE, sampleColor, "labelSample");
    private final JLabel jsonLabel = buildLabel(Icons.TIMELINE, jsonColor, "labelJson");
    private final JLabel concurrentLabel = buildLabel(Icons.FORMAT_SHAPES, concurrentColor, "labelConcurrent");

    /**
     * This is the default constructor
     */
    public FGSCellRenderer() {
        super();
        initialize();
    }

    private JLabel buildLabel(Icons icon, Color color, String name) {
        var label = new JLabel();
        label.setText("");
        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        var disabledIcon = JIcons.asSwing(icon, 20, defaultColor);
        var fontIcon = JIcons.asSwing(icon, 20, color);
        label.setDisabledIcon(disabledIcon);
        label.setIcon(fontIcon);
        label.setName(name);
        return label;
    }

    /**
     * @param  table Description of the Parameter
     * @param  value Description of the Parameter
     * @param  isSelected Description of the Parameter
     * @param  hasFocus Description of the Parameter
     * @param  row Description of the Parameter
     * @param  column Description of the Parameter
     * @return  The tableCellRendererComponent value
     * @see  javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        }
        else {
            setBackground(table.getBackground());
        }

        final Annotation annotation = (Annotation) value;
        var fgsValue = new FGSValue(annotation);

        imageLabel.setEnabled(fgsValue.hasImage());
        sampleLabel.setEnabled(fgsValue.hasSample());
        concurrentLabel.setEnabled(fgsValue.isConcurrent());
        jsonLabel.setEnabled(fgsValue.hasJson());

        return this;
    }

    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(imageLabel);
        add(sampleLabel);
        add(concurrentLabel);
        add(jsonLabel);
    }
}
