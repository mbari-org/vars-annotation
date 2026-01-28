package org.mbari.vars.ui.messages;

/**
 * Change the message shown in the status bar
 * @author Brian Schlining
 * @since 2017-08-22T10:35:00
 */
public class SetStatusBarMsg {

    private final String msg;

    public SetStatusBarMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

}
