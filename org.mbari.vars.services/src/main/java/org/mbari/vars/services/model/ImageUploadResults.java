package org.mbari.vars.services.model;

import java.net.URI;

/**
 * @author Brian Schlining
 * @since 2017-08-31T13:08:00
 */
public class ImageUploadResults {
    private String cameraId;
    private String deploymentId;
    private String name;
    private URI uri;

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
