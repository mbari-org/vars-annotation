package org.mbari.vars.annotation.ui.messages;

/**
 * @author Brian Schlining
 * @since 2018-01-11T13:11:00
 */
public class ShowExceptionAlert extends ShowAlert {

    private final Exception exception;

    public ShowExceptionAlert(String title, String headerText, String contentText, Exception exception) {
        super(title, headerText, contentText);
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
