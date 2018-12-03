/*
 * @(#)SearchableDetailEditorPaneController.java   2018.11.29 at 11:12:43 PST
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

package org.mbari.m3.vars.annotation.ui.shared;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.ConceptAssociationTemplate;
import org.mbari.m3.vars.annotation.model.Details;
import org.mbari.m3.vars.annotation.util.FXMLUtils;
import org.mbari.m3.vars.annotation.util.ListUtils;

/**
 * @author Brian Schlining
 * @since 2018-11-29T11:12:00
 */

public class SearchableDetailEditorPaneController {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    VBox root;
    @FXML
    private JFXTextField searchTextField;
    @FXML
    private JFXComboBox<ConceptAssociationTemplate> associationComboBox;

    private DetailEditorPaneController editorPaneController;
    private UIToolBox toolBox;

    @FXML
    void initialize() {

        JavaFxObservable.valuesOf(root.widthProperty())
                .subscribe(n -> associationComboBox.setPrefWidth(n.doubleValue() - 20D));

        // Set values in fields when an association is selected
        associationComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    if (newv == null) {
                        clear();
                    }
                    else {
                        setSelectedDetailsTemplate(newv);
                    }
                });

        // Trigger search when enter is pressed in search field
        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchTemplates(searchTextField.getText());
            }
        });
    }

    private void setDetailEditorPaneController(DetailEditorPaneController editorPaneController) {
        this.editorPaneController = editorPaneController;
        GridPane editorPane = editorPaneController.getRoot();
        this.root.getChildren().add(editorPane);
        JavaFxObservable.valuesOf(root.widthProperty())
                .subscribe(n -> editorPane.setPrefWidth(n.doubleValue() - 20D));
    }

    private void setSelectedDetailsTemplate(Details details) {
        editorPaneController.setDetails(details);
    }

    /**
     * Sets the acceptable templates that can be used for an editing session
     * @param templates The templates that can be used
     */
    public void setTemplates(Collection<ConceptAssociationTemplate> templates) {
        Platform.runLater(() -> {
            ObservableList<ConceptAssociationTemplate> items = associationComboBox.getItems();
            items.clear();
            items.addAll(templates);
            if (!items.contains(ConceptAssociationTemplate.NIL)) {
                items.add(ConceptAssociationTemplate.NIL);
            }
            associationComboBox.getSelectionModel().select(ConceptAssociationTemplate.NIL);
        });
    }

    private void clear() {
        searchTextField.setText(null);
        editorPaneController.setDetails(null);
    }

    /**
     *
     * @return The pane containing the detail editor
     */
    public VBox getRoot() {
        return root;
    }

    public JFXTextField getSearchTextField() {
        return searchTextField;
    }

    public JFXComboBox<?> getAssociationComboBox() {
        return associationComboBox;
    }

    /**
     * Retrieve the association containing values defined in this UI widget
     * @return None if any of the UI fields are empty. Otherwise it returns
     *  an Assocation
     */
    public Optional<Association> getCustomAssociation() {
        return editorPaneController.getCustomAssociation();
    }

    private void searchTemplates(String search) {
        List<ConceptAssociationTemplate> templates = associationComboBox.getItems();
        int startIdx = associationComboBox.getSelectionModel().getSelectedIndex() + 1;
        ListUtils.search(search, templates, startIdx, ConceptAssociationTemplate::toString)
                .ifPresent(cat -> associationComboBox.getSelectionModel().select(cat));
    }

    public static SearchableDetailEditorPaneController newInstance(UIToolBox toolBox) {
        SearchableDetailEditorPaneController controller = FXMLUtils.newInstance(SearchableDetailEditorPaneController.class,
                "/fxml/SearchableDetailEditorPane.fxml");
        controller.toolBox = toolBox;
        controller.setDetailEditorPaneController(DetailEditorPaneController.newInstance(toolBox));
        return controller;
    }
}
