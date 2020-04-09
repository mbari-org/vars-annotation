# Using VARS with Sharktopoda

![MBARI logo](../images/mbari-logo.png)

## tl;dr

Sharktopoda is one of serveral video players that can interact with VARS. It is a macOS applications and provides robust playback support for h264 and ProRes video codecs.

## Installation

Download the latest release of [Sharktopda](https://github.com/mbari-media-management/Sharktopoda).

## Configuration

Sharktopoda and VARS communiate via a simple [UDP](https://en.wikipedia.org/wiki/User_Datagram_Protocol) protocol. The only configuration is that you will need to set the UDP port number to the same value in both Sharktopoda and VARS.

### Configuring Sharktopoda

Launch Sharktopoda and then go to _Sharktopoda_ > _Preferences_. In the preferences dialog, set the port number. At MBARI, we typically use `8800`. Make sure the `Run UDP Server` checkbox is checked.

![Sharktopoda Settings](../images/sharktopoda-settings.png)

### Configuring VARS

1. Launch VARS Annotation
2. Open the settings (i.e. click on the gear icon on the toolbar)
3. Select the `Sharktopoda` tab.
4. Set the `Sharktopoda port` value to match the value you set in Sharktopoda (e.g. `8800`)
5. You can leave the `Framecapture port` number as is.

![VARS Settings](../images/vars-annotation-settings2.png)
