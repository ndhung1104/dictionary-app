package dictionary.view;

import dictionary.controller.DictionaryController;
import dictionary.model.DictionaryEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

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

    private TextField searchDefinitionField;
    private ContextMenu definitionResultsMenu;
    private ListView<String> definitionResultsListView;
    private CustomMenuItem definitionResultsContainer;

    private Scene dictionaryLookupScene, dictionaryEditorScene, gameScene;

    //editor scene var
    private TextField editorSearchField;
    private Button editorSearchBtn;

    private ListView<String> dictionaryEditorWordsList;

    private TextArea editorWordArea;
    private TextArea editorDefinitionArea;

    private Button addWordBtn;
    private Button editWordBtn;
    private Button deleteWordBtn;
    private Button resetListBtn;

    private void dictionaryLookupSceneInit(){
        // Top: search box
        searchField = new TextField();
        searchField.setPromptText("Enter a word...");
        Button searchBtn = new Button("Search");

        searchDefinitionField = new TextField();
        searchDefinitionField.setPromptText("Endter keywords...");
        Button searchDefinitionBtn = new Button("Search");
        definitionResultsMenu = new ContextMenu();
        definitionResultsMenu.prefWidthProperty().bind(searchDefinitionField.widthProperty());
        definitionResultsListView = new ListView<>();
        definitionResultsContainer = new CustomMenuItem(definitionResultsListView, false);
        HBox topBar = new HBox(8, new Label("Word:"), searchField, searchBtn, searchDefinitionField, searchDefinitionBtn);
        topBar.setPadding(new Insets(10));

        // Left: word list
        wordsList = new ListView<>();

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
            dc.onSearchWord(searchField.getText());
        });

        searchDefinitionBtn.setOnAction(event -> {
            System.out.println(searchDefinitionField.getText());
            dc.onSearchDefinition(searchDefinitionField.getText());
        });

        wordsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || updatingWordList) return;
            dc.onSearchWord(newVal);
        });

        definitionResultsListView.setPrefHeight(240);
        definitionResultsListView.setFocusTraversable(false);
        definitionResultsMenu.getItems().setAll(definitionResultsContainer);

        definitionResultsListView.setOnMouseClicked(e -> {
            String sel = definitionResultsListView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                dc.onSearchWord(sel);
                definitionResultsMenu.hide();
            }
        });

        definitionResultsMenu.setOnShowing(ev ->
                definitionResultsListView.setPrefWidth(searchDefinitionField.getWidth())
        );

        dictionaryLookupScene = new Scene(root, 800, 500);
    }

    private void dictionaryEditorSceneInit() {
        editorSearchField = new TextField();
        editorSearchField.setPromptText("Enter a word...");
        editorSearchBtn = new Button("Search");

        HBox topBar = new HBox(8, new Label("Word:"), editorSearchField, editorSearchBtn);
        topBar.setPadding(new Insets(10));

        dictionaryEditorWordsList = new ListView<>();
        List<String> names = dc.getWordNamesSorted(); // pure data
        ObservableList<String> fxNames = FXCollections.observableArrayList(names); // convert for UI
        dictionaryEditorWordsList.setItems(fxNames);

        Label editorWordLabel = new Label("Word:");
        editorWordArea = new TextArea();
        editorWordArea.setWrapText(true);
        editorWordArea.setPrefHeight(40);
        editorWordArea.setPromptText("Enter only 1 word...");

        Label editorDefinitionLabel = new Label("Definition:");
        editorDefinitionArea = new TextArea();
        editorDefinitionArea.setWrapText(true);
        editorDefinitionArea.setPromptText("Enter definitions separated by the | character...");

        VBox centerBox = new VBox(6, editorWordLabel, editorWordArea, editorDefinitionLabel, editorDefinitionArea);
        centerBox.setPadding(new Insets(10));
        VBox.setVgrow(editorDefinitionArea, Priority.ALWAYS);

        addWordBtn = new Button("Add New Word");
        editWordBtn = new Button("Edit");
        deleteWordBtn = new Button("Delete");
        resetListBtn = new Button("Reset List");

        editWordBtn.setDisable(true);
        deleteWordBtn.setDisable(true);
        editWordBtn.disableProperty().bind(
                dictionaryEditorWordsList.getSelectionModel().selectedItemProperty().isNull()
        );
        deleteWordBtn.disableProperty().bind(
                dictionaryEditorWordsList.getSelectionModel().selectedItemProperty().isNull()
        );

        HBox buttonBar = new HBox(10, addWordBtn, editWordBtn, deleteWordBtn, resetListBtn);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setStyle("-fx-background-color: #f0f0f0;");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(dictionaryEditorWordsList);
        root.setCenter(centerBox);
        root.setBottom(buttonBar);
        BorderPane.setMargin(dictionaryEditorWordsList, new Insets(10));

        dictionaryEditorScene = new Scene(root, 800, 500);


        // Search (reuse controller API or keep stub to wire later)
//        editorSearchBtn.setOnAction(e -> {
//            String word = editorSearchField.getText();
//            if (word != null && !word.isBlank()) {
//                // Example: fetch and populate editor fields
//                // dc.onEditorSearchWord(word);
//                // For now, just fill the word box:
//                editorWordArea.setText(word);
//                // editorDefinitionArea.setText(... result from controller/model ...);
//            }
//        });

//        editorSearchField.setOnAction(e -> editorSearchBtn.fire());

        dictionaryEditorWordsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            dc.onEditWord(newVal);
        });
//
//        addWordBtn.setOnAction(e -> {
//            String w = editorWordArea.getText();
//            String d = editorDefinitionArea.getText();
//            // dc.onAddWord(w, d);
//
//        });
//
//        editWordBtn.setOnAction(e -> {
//            String selected = dictionaryEditorWordsList.getSelectionModel().getSelectedItem();
//            if (selected != null) {
//                String newWord = editorWordArea.getText();
//                String newDef  = editorDefinitionArea.getText();
//                // dc.onEditWord(selected, newWord, newDef);
//                // Optionally update list item text if word name changed:
//                // int idx = dictionaryEditorWordsList.getSelectionModel().getSelectedIndex();
//                // dictionaryEditorWordsList.getItems().set(idx, newWord);
//            }
//        });
//
//        // Delete selected word (enabled only when selected)
//        deleteWordBtn.setOnAction(e -> {
//            String selected = dictionaryEditorWordsList.getSelectionModel().getSelectedItem();
//            if (selected != null) {
//                // dc.onDeleteWord(selected);
//                int idx = dictionaryEditorWordsList.getSelectionModel().getSelectedIndex();
//                dictionaryEditorWordsList.getItems().remove(idx);
//                dictionaryEditorWordsList.getSelectionModel().clearSelection();
//                editorWordArea.clear();
//                editorDefinitionArea.clear();
//            }
//        });
//
//        // Reset list (always enabled) — for now just clear
//        resetListBtn.setOnAction(e -> {
//            dictionaryEditorWordsList.getItems().clear();
//            editorWordArea.clear();
//            editorDefinitionArea.clear();
//        });
    }




    public void start(Stage stage) {

        dictionaryLookupSceneInit();
        dictionaryEditorSceneInit();
        stage.setTitle("Dictionary App");
        stage.setScene(dictionaryEditorScene);
        stage.show();

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

    public void showDefinition(List<String> matchingWords){
        Platform.runLater(() -> {
            if (matchingWords == null || matchingWords.isEmpty()) {
                definitionResultsListView.setItems(FXCollections.observableArrayList("No matches found"));
                definitionResultsListView.setDisable(true);
                statusLabel.setText("No slang words found that match ALL keywords.");
            } else {
                definitionResultsListView.setDisable(false);
                ObservableList<String> items = FXCollections.observableArrayList(matchingWords);
                definitionResultsListView.setItems(items);
                statusLabel.setText("Found " + matchingWords.size() + " matching words.");
            }

            if (!definitionResultsMenu.isShowing()) {
                definitionResultsMenu.show(searchDefinitionField, Side.BOTTOM, 0, 0);
            }
        });
    }

    public void showMessage(String message) {
        statusLabel.setText(message);
//        definitionArea.clear();
    }

    public void showWordOnEditScreen(DictionaryEntry word){
        if (word == null) {
            editorWordArea.setText("");
            editorDefinitionArea.setText("");
        } else {
            editorWordArea.setText(word.getWord());
            editorDefinitionArea.setText(word.getDefinition());
        }
    }

    public void showAllWords(Set<String> words) {
        wordsList.getItems().setAll(words);
        statusLabel.setText("Loaded " + words.size() + " words.");
    }

}
