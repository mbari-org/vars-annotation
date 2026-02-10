package org.mbari.vars.annotation.ui.messages;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-06-12T11:10:00
 */
public class ShowNonfatalErrorAlert extends ShowExceptionAlert {

    public ShowNonfatalErrorAlert(String title, String headerText, String contentText, Exception exception) {
        super(title, headerText, contentText, exception);
    }

    public static ShowNonfatalErrorAlert from(String i18nName, Exception e, ResourceBundle i18n) {
        var content = i18n.getString(i18nName + ".content");
        var header = i18n.getString(i18nName + ".header");
        var title = i18n.getString(i18nName + ".title");
        return new ShowNonfatalErrorAlert(title, header, content, e);
    }

}
