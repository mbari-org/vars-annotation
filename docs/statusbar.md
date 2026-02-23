# VARS Annotation Status Bar

![status bar](assets/images/statusbar/statusbar.png)

The status bar contains controls that affect how annotations are created and displayed in VARS.

## Group

![group](assets/images/statusbar/group.png)

A _group_ is a field on every annotation. This control shows all groups currently in use across the loaded annotations. New groups can be created by typing a name into this control and then creating an annotation that uses it.

When a new annotation is created, it is assigned the group value shown in this control. An annotation's group can be changed later using the _Bulk Editor_ panel.

## Activity

![activity](assets/images/statusbar/activity.png)

An _activity_ is a field on every annotation. This control works the same way as the group control. An annotation's activity can be changed in the _Bulk Editor_ panel.

## Show concurrent annotations

![concurrent](assets/images/statusbar/concurrent.png)

VARS supports the concept of overlapping videos. For example, a portion of a deployment or dive might be annotated in real time, on a master copy of video, and also against a proxy video. Annotations on different videos that overlap with the currently open video are called _concurrent annotations_.

When this box is unchecked, only annotations made on the video currently open in VARS are shown. When checked, annotations for the same deployment on other videos are also shown, but only those whose timestamps fall within the current video's time range. Concurrent annotations display a yellow symbol next to them in the FG/S column.

![icons](assets/images/statusbar/icons.png)

## Show JSON associations

![json](assets/images/statusbar/showjson.png)

Associations (also called Details) can be stored in various formats. JSON is commonly used to store localization data, but the volume of this data can clutter the annotation interface. When this box is checked, all associations are displayed. When unchecked, JSON associations are hidden.

If an annotation has JSON associations, a purple icon is displayed in the FG/S column.

![icons](assets/images/statusbar/icons.png)

## Show selected group only

![icons](assets/images/statusbar/showgroup.png)

Machine learning annotations are stored in their own _group_ to keep them separate from manually-created annotations. When this box is checked, only annotations belonging to the group shown in the group control are displayed. When unchecked, all annotations are shown regardless of group.
