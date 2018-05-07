/*
 * @(#)AppDemo.java   2017.10.30 at 05:27:03 PDT
 *
 * Copyright 2011 MBARI
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.m3.vars.annotation;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class AppDemo extends App {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(AppDemo.class);
        Initializer.getToolBox()
                .getEventBus()
                .toObserverable()
                .subscribe(e -> log.debug(e.toString()));
        App.main(args);

        ObservableList<Annotation> annotations = Initializer.getToolBox()
                .getData()
                .getAnnotations();

        annotations.addListener((InvalidationListener) observable ->
                log.debug("Annotation count: " + annotations.size()));
    }
}
