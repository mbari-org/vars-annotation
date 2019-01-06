package org.mbari.m3.vars.annotation.ui.imageanno;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.value.ChangeListener;
import javafx.scene.shape.Shape;
import org.mbari.m3.vars.annotation.model.Association;

import javax.annotation.Nonnull;

public class LayerNode<T, S extends Shape> {
    private final T datum;
    private final S shape;
    private final ChangeListener<? super Number> changeListener;
    private final Association association;

    protected static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public LayerNode(@Nonnull T datum,
                     @Nonnull S shape,
                     @Nonnull ChangeListener<? super Number> resizeChangeListener,
                     @Nonnull Association association) {
        this.datum = datum;
        this.shape = shape;
        this.changeListener = resizeChangeListener;
        this.association = association;
    }

    public T getDatum() {
        return datum;
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
