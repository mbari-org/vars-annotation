# VARS Annotation Setup

## Configuration

### Service Configuration

VARS requires that you have setup a microservice stack needed to power VARS. Once that's done you can do the following:

1. Download VARS from [GitHub](https://github.com/mbari-media-management/vars-annotation/releases)
2. If you're running on macOS, and you get a message that VARS is damaged and can't be opened, you can ignore that message. That's Apple's super aggressive security. To fix that:
    1. Open a terminal (The Terminal.app is located in /Applications/Utilities).
    2. cd to where VARS is downloaded. e.g. `cd /Applications`
    3. Run `sudo xattr -d -r com.apple.quarantine "VARS Annotation.app"`
    4. Relaunch VARS Annotation
3. Point VARS at your [configuration server](https://github.com/mbari-media-management/raziel).

#### First, click on the settings button

![VARS Annotation 1](assets/images/VARSAnnotation1.jpeg)

#### Add your configuration server

This should include the URL to your config server and the login (username, pwd) for one of the user accounts in VARS.

![Configuration Dialog](assets/images/ConfigServerDialog.png)

#### Test your configuration

Click the test button to verify that VARS is configured. If your dialog looks like the image below, just click the 'OK' button.

![Configuration Dialog Success](assets/images/ConfigServerDialogSuccess.png)

### Video Player Configuration

VARS communicates with custom external video players via [UDP](https://en.wikipedia.org/wiki/User_Datagram_Protocol). Both VARS and the video player (e.g. Sharktopoda) need to have the UDP port number set to the same value:

#### VARS Port setting

![Sharktopoda Port](assets/images/SharktopodaDialogPort.jpeg)

#### Sharktopoda Port Setting

Under __Sharktopoda > Preferences__:

![Sharktopoda Preferences](assets/images/SharktopodaPrefs.png)

#### Cthulhu Port Setting

Open the prefereces by clicking on the gear icon:

![Cthulhu App](assets/images/CthulhuControlsPrefs.jpeg)

Then on the network tab set the control port:

![Cthulhu Control Port](assets/images/CthulhuPrefsNetworkPort.jpeg)
