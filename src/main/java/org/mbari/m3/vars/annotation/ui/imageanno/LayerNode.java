package org.mbari.m3.vars.annotation.ui.imageanno;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.value.ChangeListener;
import javafx.scene.shape.Shape;
import org.mbari.m3.vars.annotation.model.Association;

public class LayerNode<T, S extends Shape> {
    private final T data;
    private final S shape;
    private final ChangeListener<? super Number> changeListener;
    private final Association association;

    protected static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public LayerNode(T data,
                     S shape,
                     ChangeListener<? super Number> resizeChangeListener,
                     Association association) {
        this.data = data;
        this.shape = shape;
        this.changeListener = resizeChangeListener;
        this.association = association;
    }

    public T getData() {
        return data;
    }

    /**
     *
     * @return The shape used to represent this node
     */
    public S getShape() {
        return shape;
    }

    /**
     *
     * @return The change listener registered to modify this node
     *  as the parent pane is resized
     */
    public ChangeListener<? super Number> getResizeChangeListener() {
        return changeListener;
    }

    /**
     *
     * @return The association that contains the data represented by
     *   this node.
     */
    public Association getAssociation() {
        return association;
    }

}
