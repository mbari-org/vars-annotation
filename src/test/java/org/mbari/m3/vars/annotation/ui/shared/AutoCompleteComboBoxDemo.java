package org.mbari.m3.vars.annotation.ui.shared;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * @author Brian Schlining
 * @since 2017-06-28T15:41:00
 */
public class AutoCompleteComboBoxDemo extends Application {

    private static final String[] LISTA = { "Abacate", "Abacaxi", "Ameixa", "Amora", "Araticum", "Atemoia", "Avocado",
            "Banana prata", "Caju", "Cana descascada", "Caqui", "Caqui Fuyu", "Carambola", "Cereja", "Coco verde",
            "Figo", "Figo da Índia", "Framboesa", "Goiaba", "Graviola", "Jabuticaba", "Jambo", "Jambo rosa", "Jambolão",
            "Kino (Kiwano)", "Kiwi", "Laranja Bahia", "Laranja para suco", "Laranja seleta", "Laranja serra d’água",
            "Laranjinha kinkan", "Lichia", "Lima da pérsia", "Limão galego", "Limão Taiti", "Maçã argentina",
            "Maçã Fuji", "Maçã gala", "Maçã verde", "Mamão formosa", "Mamão Havaí", "Manga espada", "Manga Haden",
            "Manga Palmer", "Manga Tommy", "Manga Ubá", "Mangostim", "Maracujá doce", "Maracujá para suco", "Melancia",
            "Melancia sem semente", "Melão", "Melão Net", "Melão Orange", "Melão pele de sapo", "Melão redinha",
            "Mexerica carioca", "Mexerica Murcote", "Mexerica Ponkan", "Mirtilo", "Morango", "Nectarina",
            "Nêspera ou ameixa amarela", "Noni", "Pera asiática", "Pera portuguesa", "Pêssego", "Physalis", "Pinha",
            "Pitaia", "Romã", "Tamarilo", "Tamarindo", "Uva red globe", "Uva rosada", "Uva Rubi", "Uva sem semente",
            "Abobora moranga", "Abobrinha italiana", "Abobrinha menina", "Alho", "Alho descascado",
            "Batata baroa ou cenoura amarela", "Batata bolinha", "Batata doce", "Batata inglesa", "Batata yacon",
            "Berinjela", "Beterraba", "Cebola bolinha", "Cebola comum", "Cebola roxa", "Cenoura", "Cenoura baby",
            "Couve flor", "Ervilha", "Fava", "Gengibre", "Inhame", "Jiló", "Massa de alho", "Maxixe", "Milho",
            "Pimenta biquinho fresca", "Pimenta de bode fresca", "Pimentão amarelo", "Pimentão verde",
            "Pimentão vermelho", "Quiabo", "Repolho", "Repolho roxo", "Tomate cereja", "Tomate salada",
            "Tomate sem acidez", "Tomate uva", "Vagem", "Agrião", "Alcachofra", "Alface", "Alface americana",
            "Almeirão", "Brócolis", "Broto de alfafa", "Broto de bambu", "Broto de feijão", "Cebolinha", "Coentro",
            "Couve", "Espinafre", "Hortelã", "Mostarda", "Rúcula", "Salsa", "Ovos brancos", "Ovos de codorna",
            "Ovos vermelhos" };

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Startswith matcher
//        ComboBox<String> cmb = new AutoCompleteComboBox<String>((txt, obj) -> {
//            return obj.toUpperCase().startsWith(txt.toUpperCase());
//        });

        // Fuzzy matcher
        ComboBox<String> cmb = new AutoCompleteComboBox<String>((txt, obj) -> {
            System.out.println("TXT = " + txt + ", OBJ = " + obj);
            char[] target = obj.toUpperCase().toCharArray();
            char[] source = txt.toUpperCase().toCharArray();


            return obj.toUpperCase().startsWith(txt.toUpperCase());
        });
        cmb.setTooltip(new Tooltip());
        cmb.setItems(FXCollections.observableArrayList(LISTA));
        stage.setScene(new Scene(new StackPane(cmb)));
        stage.show();
        stage.setTitle("Filtrando um ComboBox");
        stage.setWidth(300);
        stage.setHeight(300);
    }

}