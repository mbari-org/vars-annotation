package org.mbari.m3.vars.annotation.model;

/**
 * @author Brian Schlining
 * @since 2018-08-13T15:40:00
 */
public class CreatedImageData {

    private ImageUploadResults imageUploadResults;
    private Image image;

    public CreatedImageData(ImageUploadResults imageUploadResults, Image image) {
        this.imageUploadResults = imageUploadResults;
        this.image = image;
    }

    public CreatedImageData() {
    }

    public ImageUploadResults getImageUploadResults() {
        return imageUploadResults;
    }

    public Image getImage() {
        return image;
    }

    public void setImageUploadResults(ImageUploadResults imageUploadResults) {
        this.imageUploadResults = imageUploadResults;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
