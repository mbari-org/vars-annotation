package org.mbari.vars.annotation.ui.messages;


/**
 * @author Brian Schlining
 * @since 2017-06-12T11:11:00
 */
public abstract class ShowAlert {
    private final String title;
    private final String headerText;
    private final String contentText;

    ShowAlert(String title, String headerText, String contentText) {
        this.title = title;
        this.headerText = headerText;
        this.contentText = contentText;
    }


    public String getTitle() {
        return title;
    }

    public String getHeaderText() {
        return headerText;
    }

    public String getContentText() {
        return contentText;
    }

}
