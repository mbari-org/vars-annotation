package org.mbari.m3.vars.annotation.messages;

/**
 * @author Brian Schlining
 * @since 2017-06-12T11:10:00
 */
public class ShowNonfatalErrorAlert extends ShowAlert {

    private final Exception exception;

    public ShowNonfatalErrorAlert(String title, String headerText, String contentText, Exception exception) {
        super(title, headerText, contentText);
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
