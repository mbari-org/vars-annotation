package org.mbari.vars.services.model;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-08-25T15:42:00
 */
public class AncillaryData {

    private double altitude;
    private double depthMeters;
    private double latitude;
    private double lightTransmission;
    private double longitude;
    private double oxygenMlL;
    private double phi;
    private double pressureDbar;
    private double psi;
    private double salinity;
    private double temperatureCelsius;
    private double theta;
    private double x;
    private double y;
    private double z;
    private Instant lastUpdatedTime;
    private UUID uuid;

    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getAltitude() {

        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getDepthMeters() {
        return depthMeters;
    }

    public void setDepthMeters(double depthMeters) {
        this.depthMeters = depthMeters;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLightTransmission() {
        return lightTransmission;
    }

    public void setLightTransmission(double lightTransmission) {
        this.lightTransmission = lightTransmission;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getOxygenMlL() {
        return oxygenMlL;
    }

    public void setOxygenMlL(double oxygenMlL) {
        this.oxygenMlL = oxygenMlL;
    }

    public double getPhi() {
        return phi;
    }

    public void setPhi(double phi) {
        this.phi = phi;
    }

    public double getPressureDbar() {
        return pressureDbar;
    }

    public void setPressureDbar(double pressureDbar) {
        this.pressureDbar = pressureDbar;
    }

    public double getPsi() {
        return psi;
    }

    public void setPsi(double psi) {
        this.psi = psi;
    }

    public double getSalinity() {
        return salinity;
    }

    public void setSalinity(double salinity) {
        this.salinity = salinity;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
