package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vcr4j.remote.control.RVideoIO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SharktopodaState {

//    private final List<UUID> selectedLocalizations = new CopyOnWriteArrayList<>();

    private final List<LocalizationPair> selectedLocalizations = new CopyOnWriteArrayList<>();

    private final Map<UUID, LocalizationPair> localizations = new HashMap<>();

    public void setSelectedLocalizations(Collection<LocalizationPair> selectedLocalizations) {
        synchronized (this.selectedLocalizations) {
            this.selectedLocalizations.clear();
            this.selectedLocalizations.addAll(selectedLocalizations);
        }
    }

    public void clear() {
        selectedLocalizations.clear();
        localizations.clear();
    }

    public void addLocalizationPairs(Collection<LocalizationPair> pairs) {
        pairs.forEach(this::addLocalizationPair);
    }

    public void addLocalizationPair(LocalizationPair localizationPair) {
        localizations.put(localizationPair.localization().getUuid(), localizationPair);
    }

    public void removeLocalizationPairs(Collection<LocalizationPair> pairs) {
        pairs.forEach(p -> removeLocalizationPair(p.localization().getUuid()));
    }

    public void removeLocalizationPair(UUID localizationUuid) {
        localizations.remove(localizationUuid);
    }


    public boolean isDifferentThanSelected(Collection<UUID> localizations) {
        if (localizations.isEmpty() && selectedLocalizations.isEmpty()) {
            return false;
        }
        else if (!localizations.isEmpty() && !selectedLocalizations.isEmpty()) {
            var copy = new ArrayList<>(localizations);
            var copySelected = selectedLocalizations.stream().map(lp -> lp.localization().getUuid()).toList();
            if (copy.size() == copySelected.size()) {
                copy.removeAll(copySelected);
                return copy.size() != 0;
            }
        }
        return true;
    }

    public static List<LocalizationPair> from(Annotation a, UIToolBox toolBox) {
        return LocalizedAnnotation.from(a)
                .stream()
                .flatMap(la -> la.toLocalization(toolBox).map(loc -> new LocalizationPair(la, loc)).stream())
                .toList();
    }

    public static List<LocalizationPair> from(Collection<Annotation> xs, UIToolBox toolBox) {
        return xs.stream()
                .flatMap(a -> SharktopodaState.from(a, toolBox).stream())
                .toList();
    }

    // TODO MOve this to static shared method for reuse
//    public static void addLocalization(Collection<Annotation> annotations, UIToolBox toolBox) {
//        var head = annotations.iterator().next();
//        var ass = addedAssociations.iterator().next();
//        var io = toolBox.getMediaPlayer().getVideoIO();
//        if (io instanceof RVideoIO rio) {
//            var la = new LocalizedAnnotation(head, ass);
//            var opt = la.toLocalization(toolBox);
//            opt.ifPresent(loc -> {
//                rio.send(new AddLocalizationsCmd(rio.getUuid(), List.of(loc)));
//            });
//        }
//    }
//
//    // TODO MOve this to static shared method for reuse
//    public static void removeLocalization(Collection<Annotation> originalAnnotations, UIToolBox toolBox) {
//        var head = originalAnnotations.iterator().next();
//        var ass = addedAssociations.iterator().next();
//        var io = toolBox.getMediaPlayer().getVideoIO();
//        if (io instanceof RVideoIO rio) {
//            var la = new LocalizedAnnotation(head, ass);
//            var opt = la.toLocalization(toolBox);
//            opt.ifPresent(loc -> {
//                rio.send(new RemoveLocalizationsCmd(rio.getUuid(), List.of(loc.getUuid())));
//            });
//        }
//    }
}
