package org.mbari.vars.ui.messages;

/**
 * @author Brian Schlining
 * @since 2017-06-12T11:10:00
 */
public class ShowNonfatalErrorAlert extends ShowExceptionAlert {

    public ShowNonfatalErrorAlert(String title, String headerText, String contentText, Exception exception) {
        super(title, headerText, contentText, exception);
    }

}
