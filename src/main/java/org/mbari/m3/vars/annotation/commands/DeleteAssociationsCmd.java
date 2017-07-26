package org.mbari.m3.vars.annotation.commands;

import com.google.common.base.Preconditions;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Association;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:06:00
 */
public class DeleteAssociationsCmd implements Command {

    private final List<Association> associations;

    public DeleteAssociationsCmd(List<Association> associations) {
        Preconditions.checkArgument(associations != null,
                "Can not delete a null annotation list");
        Preconditions.checkArgument(!associations.isEmpty(),
                "Can not delete an empty annotation list");
        this.associations = Collections.unmodifiableList(new ArrayList<>(associations));
    }

    @Override
    public void apply(UIToolBox toolBox) {

    }

    @Override
    public void unapply(UIToolBox toolBox) {

    }

    @Override
    public String getDescription() {
        return "Delete Associations";
    }

    // TODO annosaurus should support deletion of multiple associations in one transaction
}
