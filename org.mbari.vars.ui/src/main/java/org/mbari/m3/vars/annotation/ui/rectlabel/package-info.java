/**
 * Notes:
 * - When opened. Lookup all images in the  current video set
 *      - Refreshe button will reload them
 *      - Add images to imageList
 *      - Listen for changes in media. Reload images on change
 * - When an image is selected, lookup annotations for that image
 *      - Add annotations to imageAnnotation list
 * - When an annotaiton is selected
 *      - If it has a bounding box, enable delete
 *      - If it does not have a bounding box, allow user to draw one
*  - If new annotation button is selected
 *      - When clicking on the image, allow user to draw bounding box
 *      - After drawign is complete, show a dialog allowing user to specify concept
 *
 *
 * @author Brian Schlining
 * @since 2018-05-04T15:52:00
 */
package org.mbari.m3.vars.annotation.ui.rectlabel;