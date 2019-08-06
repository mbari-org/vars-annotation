# Workflow: Real-time Annotation

This document describes the flow of opening a real time annotation session.

## UI Flow

1. A user selects the real-time option from the `open` popup menu.
2. A dialog like the one below is displayed
![Dialog mockup](../resources/images/real-time-dialog.png)
3. When `OK` or `Done` is selected the following values steps occur:
   1. Generate a real-time URI in the form of `urn:rtva:org.mbari:<camera_id>_<sequence_number>`. Be sure to replace all spaces with underscores or it will not be a valid URI. (rtva = real-time video annotation)
   2. Lookup the URI in vampire-squid. If it exists, open it. If not go to step 4.
4. Generate parameters for creating a new media in vampire squid:
  - `camera_id`: from the `cameraIdCombobox`, e.g. Doc Ricketts
  - `video_sequence_name`: `<camera_id> <sequence_num>`. e.g. Doc Ricketts 0931
  - `start_timestamp`: The current time on the host computer in UTC.
  - `video_name`: `camera_id <sequence_num> <timestamp>`. Doc Ricketts 0931 20171220T035701Z
  - `uri`: The value create above
 5. Send POST request to vampire squid to create media. Open the media returned by the request.
 6. When closing a real-time annotation (or exiting the app when one is open), set the duration based on the time of the last annotation in the annotation group.
 
## MediaPlayer
 
 Remove or disable sharktopodacontrolpane? Need to look into removing/adapting/ or repacing the media controls when in real-time mode.