package org.mbari.m3.vars.annotation.services;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.util.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-10-26T14:21:00
 */
public class FileBrowsingDecorator {
    private final UIToolBox toolBox;
    private final FileChooser fileChooser = new FileChooser();
    private final Logger log = LoggerFactory.getLogger(getClass());

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
                log.warn("Failed to lookup file by sha512 sidecar file", e);
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
                .getMediaService()
                .findByFilename(file.getName())
                .thenAccept(media -> {
                    if (media.isEmpty()) {
                        // TODO Show not found dialog
                    }
                    else if (media.size() > 1) {
                        // TODO Show to many matches found dialog
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
                .getMediaService()
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
