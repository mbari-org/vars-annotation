package org.mbari.vars.annotation.ui.services;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.MediaChangedEvent;
import org.mbari.vars.annotation.ui.messages.ShowInfoAlert;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annotation.util.FormatUtils;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-10-26T14:21:00
 */
public class FileBrowsingDecorator {
    private final UIToolBox toolBox;
    private final FileChooser fileChooser = new FileChooser();
    private final Loggers log = new Loggers(getClass());

    public FileBrowsingDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        fileChooser.setTitle(toolBox.getI18nBundle().getString("filebrowsing.dialog.title"));
    }

    public void apply(Window owner) {
        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            // 1. Check for sidecar file
            Optional<byte[]> bytes = Optional.empty();
            try {
                bytes = readSha512(file);
            } catch (IOException e) {
                log.atInfo().log("Unable to locate sha512 sidecar file for " + file.getAbsolutePath());
            }
            if (bytes.isPresent()) {
                fetchBySha51(bytes.get(), file);
            }
            else {
                fetchByFilename(file);
            }
        }
    }
    /**
     * Read SHA512 from side car file
     * @param file The video file of interest
     * @return The sha512 as a byte array
     */
    public Optional<byte[]> readSha512(File file) throws IOException {
        File sideCar = new File(file.getAbsolutePath() + ".sha512");
        return Files.lines(Paths.get(sideCar.getAbsolutePath()), StandardCharsets.UTF_8)
                .filter(s -> s.length() == 128)
                .findFirst()
                .map(FormatUtils::fromHexString);
    }

    /**
     * Look up Media from the video asset manager using it's name
     *
     * @param file The video file of interest
     */
    private void fetchByFilename(File file) {
        toolBox.getServices()
                .mediaService()
                .findByFilename(file.getName())
                .thenAccept(media -> {
                    if (media.isEmpty()) {
                        // Show not found dialog
                        log.atWarn().log("Did not find any media in the video asset manager with the name " + file.getName());
                        ResourceBundle i18n = toolBox.getI18nBundle();
                        String title = i18n.getString("filebrowsing.open.missing.title");
                        String header = i18n.getString("filebrowsing.open.missing.header");
                        String content=i18n.getString("filebrowsing.open.missing.content");
                        toolBox.getEventBus()
                                .send(new ShowInfoAlert(title, header, content));
                    }
                    else if (media.size() > 1) {
                        // Show to many matches found dialog
                        log.atWarn().log("More than one media exists in the video asset manager with the name " + file.getName());
                        ResourceBundle i18n = toolBox.getI18nBundle();
                        String title = i18n.getString("filebrowsing.open.toomany.title");
                        String header = i18n.getString("filebrowsing.open.toomany.header");
                        String content1=i18n.getString("filebrowsing.open.toomany.content1");
                        String content2=i18n.getString("filebrowsing.open.toomany.content2");
                        String content = content1 + " " + file.getName() + " " + content2;
                        toolBox.getEventBus()
                                .send(new ShowInfoAlert(title, header, content));
                    }
                    else {
                        // 1 media, open it.
                        Media m = media.get(0);
                        m.setUri(file.toURI()); // Open local file, not remote one
                        toolBox.getEventBus()
                                .send(new MediaChangedEvent(FileBrowsingDecorator.this, m));
                    }
                });
    }

    private void fetchBySha51(byte[] sha512, File file) {
        toolBox.getServices()
                .mediaService()
                .findBySha512(sha512)
                .thenAccept(media -> {
                    if (media != null) {
                        media.setUri(file.toURI()); // Open local file, not remote one
                        toolBox.getEventBus()
                                .send(new MediaChangedEvent(FileBrowsingDecorator.this, media));
                    }
                });
    }


}
