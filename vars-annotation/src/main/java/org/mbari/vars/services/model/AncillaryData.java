package org.mbari.vars.services.model;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-08-25T15:42:00
 */
public class AncillaryData {

    /** Used for merging only. Typically this will be null */
    private Instant recordedTimestamp;
    private Double altitude;
    private String crs;
    private Double depthMeters;
    private Double latitude;
    private Double lightTransmission;
    private Double longitude;
    private Double oxygenMlL;
    private Double phi;
    private String posePositionUnits;
    private Double pressureDbar;
    private Double psi;
    private Double salinity;
    private Double temperatureCelsius;
    private Double theta;
    private Double x;
    private Double y;
    private Double z;
    private Instant lastUpdatedTime;
    private UUID uuid;
    private UUID imagedMomentUuid;

    public AncillaryData() {
    }

    public AncillaryData(AncillaryData src) {
        setAltitude(src.getAltitude());
        setCrs(src.getCrs());
        setDepthMeters(src.getDepthMeters());
        setLatitude(src.getLatitude());
        setLightTransmission(src.getLightTransmission());
        setLongitude(src.getLongitude());
        setOxygenMlL(src.getOxygenMlL());
        setPhi(src.getPhi());
        setPosePositionUnits(src.getPosePositionUnits());
        setPressureDbar(src.getPressureDbar());
        setPsi(src.getPsi());
        setSalinity(src.getSalinity());
        setTemperatureCelsius(src.getTemperatureCelsius());
        setTheta(src.getTheta());
        setX(src.getX());
        setY(src.getY());
        setZ(src.getZ());
        uuid = src.getUuid();
        lastUpdatedTime = src.getLastUpdatedTime();
        setImagedMomentUuid(src.getImagedMomentUuid());
        setRecordedTimestamp(src.getRecordedTimestamp());
    }

    public Instant getRecordedTimestamp() {
        return recordedTimestamp;
    }

    public void setRecordedTimestamp(Instant recordedTimestamp) {
        this.recordedTimestamp = recordedTimestamp;
    }

    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public double getDepthMeters() {
        return depthMeters;
    }

    public void setDepthMeters(Double depthMeters) {
        this.depthMeters = depthMeters;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLightTransmission() {
        return lightTransmission;
    }

    public void setLightTransmission(Double lightTransmission) {
        this.lightTransmission = lightTransmission;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getOxygenMlL() {
        return oxygenMlL;
    }

    public void setOxygenMlL(Double oxygenMlL) {
        this.oxygenMlL = oxygenMlL;
    }

    public Double getPhi() {
        return phi;
    }

    public void setPhi(Double phi) {
        this.phi = phi;
    }

    public Double getPressureDbar() {
        return pressureDbar;
    }

    public void setPressureDbar(Double pressureDbar) {
        this.pressureDbar = pressureDbar;
    }

    public Double getPsi() {
        return psi;
    }

    public void setPsi(Double psi) {
        this.psi = psi;
    }

    public Double getSalinity() {
        return salinity;
    }

    public void setSalinity(Double salinity) {
        this.salinity = salinity;
    }

    public Double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(Double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public Double getTheta() {
        return theta;
    }

    public void setTheta(Double theta) {
        this.theta = theta;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getPosePositionUnits() {
        return posePositionUnits;
    }

    public void setPosePositionUnits(String posePositionUnits) {
        this.posePositionUnits = posePositionUnits;
    }

    public UUID getImagedMomentUuid() {
        return imagedMomentUuid;
    }

    public void setImagedMomentUuid(UUID imagedMomentUuid) {
        this.imagedMomentUuid = imagedMomentUuid;
    }


}
