package org.mbari.vars.annotation.ui.javafx.abpanel;

import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssocToString {

    private static final Logger log = LoggerFactory.getLogger(AssocToString.class);

    /**
     * Normalizes the toString for Associations and NamedAssociations to
     * linkName | toConcept | linkValue
     * @param a
     * @param <A>
     * @return
     */
    public static <A extends Association> String asString(A a) {
        if (a instanceof NamedAssociation n) {
            return n.asAssociation().toString();
        }
        else {
            return a.toString();
        }
    }
}
