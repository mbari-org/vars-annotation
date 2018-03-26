package org.mbari.m3.vars.annotation.mediaplayers.vcr;


import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Brian Schlining
 * @since 2018-03-26T10:54:00
 */
public class MediaParams {

    public static final String URI_PREFIX = "urn:tid:mbari.org:";
    private final String cameraId;
    private final Integer diveNumber;
    private final Integer tapeNumber;
    private final boolean isHd;
    private final String videoSequenceName;
    private final String serialPort;
    private final URI uri;

    public MediaParams(String cameraId,
                       Integer diveNumber,
                       Integer tapeNumber,
                       boolean isHd,
                       String serialPort) {
        this.cameraId = cameraId;
        this.diveNumber = diveNumber;
        this.tapeNumber = tapeNumber;
        this.isHd = isHd;
        this.serialPort = serialPort;

        String hd = isHd ? "HD" : "";
        videoSequenceName = String.format("%s%04d-%02d%s",
                cameraId.toUpperCase().charAt(0),
                diveNumber,
                tapeNumber,
                hd);

        try {
            uri = new URI(URI_PREFIX + videoSequenceName);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getVideoSequencename() {
        return videoSequenceName;
    }

    public URI getUri() {
        return uri;
    }

    public String getCameraId() {
        return cameraId;
    }

    public Integer getDiveNumber() {
        return diveNumber;
    }

    public Integer getTapeNumber() {
        return tapeNumber;
    }

    public boolean isHd() {
        return isHd;
    }

    public String getVideoSequenceName() {
        return videoSequenceName;
    }

    public String getSerialPort() {
        return serialPort;
    }
}
