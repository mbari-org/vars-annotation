package org.mbari.vars.ui.demos.javafx.shared;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import org.mbari.vars.ui.UIToolBox;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-06-12T14:57:00
 */
public class AlertFatalErrorController extends AlertErrorController {

    public AlertFatalErrorController(Alert.AlertType alertType, UIToolBox toolBox) {
        super(alertType, toolBox);
    }

    public void showAndWait(String title, String headerText, Exception ex) {
        super.showAndWait(title, headerText, randomHaiku(), ex);
    }

    private static String randomHaiku() {
        final List<String> haikus = new ArrayList<String>() {
            {
                add("Chaos reigns within.\nReflect, repent, and restart.\nOrder shall return.");
                add("Errors have occurred.\nWe won't tell you where or why.\nLazy programmers.");
                add("A crash reduces\nyour expensive computer\nto a simple stone.");
                add("There is a chasm\nof carbon and silicon\nthe software can't bridge");
                add("Yesterday it worked\nToday it is not working\nSoftware is like that");
                add("To have no errors\nWould be life without meaning\nNo struggle, no joy");
                add("Error messages\ncannot completely convey.\nWe now know shared loss.");
                add("The code was willing,\nIt considered your request,\nBut the chips were weak.");
                add("Wind catches lily\nScatt'ring petals to the wind:\nApplication dies");
                add("Three things are certain:\nDeath, taxes and lost data.\nGuess which has occurred.");
                add("Rather than a beep\nOr a rude error message,\nThese words: \"Restart now.\"");
                add("ABORTED effort:\nClose all that you have.\nYou ask way too much.");
                add("The knowledgebase crashed.\nI am the Blue Screen of Death.\nNo one hears your screams.");
                add("No-one can tell\nwhat God or Heaven will do\nIf you divide by zero.");
                add("Some bugs have names\nOthers inscrutable numbers\nYours has not even that.");
                add("Riddle for student\nWhat error is so fatal\nIt has no message");
                add("Technical support\nWould be a flowing source of\nSweet commiseration.");
                add("Something you entered\ntranscended parameters\nSo much is unknown.");
                add("Your data was big\nIt might be very useful\nBut now it is gone.");
                add("In a wired world\n distractions consume ones thoughts\n... mindfulness returns");
            }
        };

        return haikus.get((int) Math.floor(Math.random() * haikus.size()));
    }

    public Image randomImage() {
        final List<String> images = new ArrayList<String>() {
            {
                add("/images/fatal/202.jpg");
                add("/images/fatal/2010-01-04-null-pointer-exception.png");
                add("/images/fatal/1204185247268.jpg");
                add("/images/fatal/actualbug.jpg");
                add("/images/fatal/bug.png");
                add("/images/fatal/catchAllTheErrors-615x461.png");
                add("/images/fatal/epic-fail4.jpg");
                add("/images/fatal/error_cartoon.gif");
                add("/images/fatal/fatal-error-cartoon.jpg");
                add("/images/fatal/Flag_of_Edward_England.svg.png");
                add("/images/fatal/Funny-Animals-EPIC-FAIL-Indeed.jpg");
                add("/images/fatal/its_okay_i_wrote_an_exception.jpg");
                add("/images/red-frown_small.png");
                add("/images/fatal/xkcd2.png");
            }

        };
        String image = images.get((int) Math.floor(Math.random() * images.size()));
        return new Image(getClass().getResource(image).toExternalForm());
    }
}
