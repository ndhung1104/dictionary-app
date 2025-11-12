package dictionary.view;

import dictionary.controller.DictionaryController;
import dictionary.model.DictionaryEntry;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DictionaryView {

    public void setDc(DictionaryController dc) {
        this.dc = dc;
    }

    private DictionaryController dc;

    private TextField searchField;
    private TextArea definitionArea;
    private ListView<String> wordsList;
    private Label statusLabel;
    private TextArea wordNameArea;
    private boolean updatingWordList;
    public void start(Stage stage) {
        // Top: search box
        searchField = new TextField();
        searchField.setPromptText("Enter a word...");
        Button searchBtn = new Button("Search");

        HBox topBar = new HBox(8, new Label("Word:"), searchField, searchBtn);
        topBar.setPadding(new Insets(10));

        // Left: word list
        wordsList = new ListView<>();

//        // Center: definition area
//        definitionArea = new TextArea();
//        definitionArea.setEditable(false);
//        definitionArea.setWrapText(true);
        Label wordNameLabel = new Label("Word:");
        wordNameArea = new TextArea();
        wordNameArea.setEditable(false);
        wordNameArea.setWrapText(true);
        wordNameArea.setPrefHeight(40);

        // Definition area
        Label definitionLabel = new Label("Definition:");
        definitionArea = new TextArea();
        definitionArea.setEditable(false);
        definitionArea.setWrapText(true);

        VBox definitionBox = new VBox(5);
        definitionBox.getChildren().addAll(wordNameLabel, wordNameArea, definitionLabel, definitionArea);
        VBox.setVgrow(definitionArea, Priority.ALWAYS);

        // Bottom: status
        statusLabel = new Label("Ready.");
        HBox bottomBar = new HBox(statusLabel);
        bottomBar.setPadding(new Insets(8));
        bottomBar.setStyle("-fx-background-color: #f0f0f0;");

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(wordsList);
//        root.setCenter(definitionArea);
        root.setCenter(definitionBox);
        root.setBottom(bottomBar);
        BorderPane.setMargin(wordsList, new Insets(10));
//        BorderPane.setMargin(definitionArea, new Insets(10));
        BorderPane.setMargin(definitionBox, new Insets(10));
        searchBtn.setOnAction(event -> {
            System.out.println(searchField.getText());
            dc.onSearch(searchField.getText());
        });

        wordsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || updatingWordList) return;
            if (newVal != null) {
                dc.onSearch(newVal);
            }
        });


        // Scene
        Scene scene = new Scene(root, 800, 500);
        stage.setTitle("Dictionary App");
        stage.setScene(scene);
        stage.show();

        // Initialize data
        dc.initialize();
    }

    private String[] splitDefinition(DictionaryEntry wordEntry) {
        String definitionString = wordEntry.getDefinition();
        return definitionString.trim().split("\\|");
    }

    public void showWord(DictionaryEntry wordEntry) {
        Platform.runLater(() -> {
            updatingWordList = true;
            try {
                statusLabel.setText("Found: " + wordEntry.getWord());
                if (wordsList.getItems().contains(wordEntry.getWord())) {
                    wordsList.getItems().remove(wordEntry.getWord());
                }
                wordsList.getItems().add(0, wordEntry.getWord());
                if (wordsList.getItems().size() > 20) {
                    wordsList.getItems().remove(wordsList.getItems().size() - 1);
                }
                String[] definitionList = splitDefinition(wordEntry);
                wordNameArea.setText(wordEntry.getWord());
                StringBuilder bulletList = new StringBuilder();
                for (String def : definitionList) {
                    bulletList.append("• ").append(def).append("\n");
                }
                definitionArea.setText(bulletList.toString());
                wordsList.getSelectionModel().selectFirst();
                wordsList.scrollTo(0);
            } finally {
                updatingWordList = false;
            }
        });

    }

    public void showMessage(String message) {
        statusLabel.setText(message);
        definitionArea.clear();
    }

    public void showAllWords(Set<String> words) {
        wordsList.getItems().setAll(words);
        statusLabel.setText("Loaded " + words.size() + " words.");
    }

}
