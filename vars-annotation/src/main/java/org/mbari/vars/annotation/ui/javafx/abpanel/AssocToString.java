package org.mbari.vars.annotation.ui.javafx.abpanel;

import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;

public class AssocToString {

    private static final Loggers log = new Loggers(AssocToString.class);

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
