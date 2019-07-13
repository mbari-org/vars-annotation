/**
 * Classes that end with 'BC' stand for button controllers. They should inherit from
 * AbstractBC to reduce code duplication. The general usage is:
 * <pre>
 *     public class MyBC extends AbstractBC {
 *         public MyBC(Button button, UIToolBox toolBox) {
 *             super(button, toolbox)
 *         }
 *
 *         protected void init() {
 *             // Configure tooltip and icon
 *             String tooltip = toolBox.getI18nBundle().getString("buttons.newnumber");
 *             MaterialIconFactory iconFactory = MaterialIconFactory.get();
 *             Text icon = iconFactory.createIcon(MaterialIcon.FIBER_NEW, "30px");
 *             initializeButton(tooltip, icon); // implemented in AbstractBC
 *         }
 *
 *         protected void apply() {
 *             toolBox.getEventBus()
 *                  .send(new SomeActionCmd());
 *         }
 *     }
 * </pre>
 * @author Brian Schlining
 * @since 2017-08-22T14:18:00
 */
package org.mbari.m3.vars.annotation.ui.buttons;