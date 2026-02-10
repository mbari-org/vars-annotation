package org.mbari.vars.annotation.ui.javafx.abpanel;

import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.models.ConceptAssociationTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-10-12T15:49:00
 */
class NamedAssociation extends Association {
    private final String name;
    private static final Loggers log = new Loggers(NamedAssociation.class);

    public NamedAssociation(String linkName, String toConcept, String linkValue, String name) {
        super(linkName, toConcept, linkValue);
        this.name = name;
    }

    public NamedAssociation(ConceptAssociationTemplate cat, String name) {
        super(cat.getLinkName(), cat.getToConcept(), cat.getLinkValue());
        this.name = name;
    }

    public NamedAssociation(org.mbari.vars.oni.sdk.r1.models.ConceptAssociationTemplate cat, String name) {
        super(cat.getLinkName(), cat.getToConcept(), cat.getLinkValue());
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString() + " | " + name;
    }

    public Association asAssociation() {
        return new Association(getLinkName(), getToConcept(), getLinkValue());
    }

    public static Optional<NamedAssociation> parseNamed(String s) {
        Optional<NamedAssociation> na = Optional.empty();
        try {
            List<String> ss = Arrays.stream(s.split("[|]"))
                    .map(String::trim)
                    .toList();
            if (ss.size() == 4) {
                na = Optional.of(new NamedAssociation(ss.get(0), ss.get(1), ss.get(2), ss.get(3)));
            }
            else {
                throw new IllegalArgumentException("Bad association format: " + s);
            }
        }
        catch (Exception e) {
            // Do nothing
            log.atWarn().withCause(e).log("Failed to parse an association from " + s);
        }
        return na;
    }
}