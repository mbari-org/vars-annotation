package org.mbari.m3.vars.annotation.messages;



/**
 * @author Brian Schlining
 * @since 2017-06-12T11:10:00
 */
public class ShowFatalErrorAlert extends ShowExceptionAlert {

    public ShowFatalErrorAlert(String title, String headerText, String contentText, Exception exception) {
        super(title, headerText, contentText, exception);
    }
}