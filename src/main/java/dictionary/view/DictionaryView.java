package dictionary.view;

import dictionary.controller.DictionaryController;
import dictionary.model.DictionaryEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Set;

public class DictionaryView {

    private Stage stageRef;
    private enum SceneId { LOOKUP, EDITOR, GAME }
    private SceneId currentScene = null;
    private final java.util.Map<SceneId, Scene> scenes = new java.util.EnumMap<>(SceneId.class);
    private boolean switchingScene = false;

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

    private Scene dictionaryLookupScene, dictionaryEditorScene, dictionaryGameScene;

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

    private ObservableList<String> fxNames;
    private javafx.collections.transformation.FilteredList<String> filteredNames;
    private SortedList<String> sortedList;

    //game scene
    private VBox centerBox;


    private HBox buildNavBar(String scene) {
        Button btnLookup = new Button("Dictionary");
        Button btnEditor = new Button("Edit");
        Button btnGame   = new Button("Game");

        if (scene.equals("LOOKUP")) {
            btnLookup.setDisable(true);
        } else if (scene.equals("EDITOR")) {
            btnEditor.setDisable(true);
        } else if (scene.equals("GAME")) {
            btnGame.setDisable(true);
        }

        btnLookup.setOnAction(e -> switchScene(SceneId.LOOKUP));
        btnEditor.setOnAction(e -> switchScene(SceneId.EDITOR));
        btnGame.setOnAction(e -> switchScene(SceneId.GAME));

        HBox nav = new HBox(8, btnLookup, btnEditor, btnGame);
        nav.setPadding(new Insets(8, 10, 8, 10));
        nav.setStyle("-fx-background-color: linear-gradient(to bottom, #fafafa, #ececec); -fx-border-color: #ddd;");
        return nav;
    }

    private void fadeIn(Parent root) {
        root.setOpacity(0.0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(180), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void switchScene(SceneId target) {
        if (switchingScene || target == currentScene) return;
        switchingScene = true;

        Scene s = scenes.get(target);
        if (s == null) { switchingScene = false; return; }

        stageRef.setScene(s);
        fadeIn(s.getRoot());
        currentScene = target;
        switchingScene = false;
    }

    private void dictionaryLookupSceneInit(){
        // Top: search box
        searchField = new TextField();
        searchField.setPromptText("Enter a word...");
        searchField.setPrefWidth(130);
        Button searchBtn = new Button("Search");

        searchDefinitionField = new TextField();
        searchDefinitionField.setPromptText("Enter keywords...");
        searchDefinitionField.setPrefWidth(110);
        Button searchDefinitionBtn = new Button("Search");
        definitionResultsMenu = new ContextMenu();
        definitionResultsMenu.prefWidthProperty().bind(searchDefinitionField.widthProperty());
        definitionResultsListView = new ListView<>();
        definitionResultsContainer = new CustomMenuItem(definitionResultsListView, false);
        Label wordSearchLabel = new Label("Word:");
        Label defSearchLabel = new Label("Definition:");
        Region lookupSpacer = new Region();
        HBox.setHgrow(lookupSpacer, Priority.ALWAYS);
        HBox searchRow = new HBox(8,
                wordSearchLabel, searchField, searchBtn,
                defSearchLabel, searchDefinitionField, searchDefinitionBtn,
                lookupSpacer, buildNavBar("LOOKUP")
        );
        searchRow.setAlignment(Pos.CENTER_LEFT);

        VBox topBar = new VBox(6, searchRow);
        topBar.setPadding(new Insets(10));

        // Left: word list
        Label historyLabel = new Label("History");
        historyLabel.setStyle("-fx-font-size: 14px;");
        wordsList = new ListView<>();
        VBox leftColumn = new VBox(6, historyLabel, wordsList);
        leftColumn.setPadding(new Insets(10));

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

        VBox definitionBox = new VBox(6);
        definitionBox.getChildren().addAll(wordNameLabel, wordNameArea, definitionLabel, definitionArea);
        definitionBox.setPadding(new Insets(10));
        VBox.setVgrow(definitionArea, Priority.ALWAYS);

        // Bottom: status
        statusLabel = new Label("Ready.");
        HBox bottomBar = new HBox(statusLabel);
        bottomBar.setPadding(new Insets(8));
        bottomBar.setStyle("-fx-background-color: #f0f0f0;");

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(leftColumn);
        root.setCenter(definitionBox);
        root.setBottom(bottomBar);
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
        scenes.put(SceneId.LOOKUP, dictionaryLookupScene);
    }

    private void dictionaryEditorSceneInit() {
        editorSearchField = new TextField();
        editorSearchField.setPromptText("Enter a word...");
        editorSearchField.setPrefWidth(130);
        editorSearchBtn = new Button("Search");

        Region editorSpacer = new Region();
        HBox.setHgrow(editorSpacer, Priority.ALWAYS);
        HBox searchRow = new HBox(8, new Label("Word:"), editorSearchField, editorSearchBtn, editorSpacer, buildNavBar("EDITOR"));
        searchRow.setAlignment(Pos.CENTER_LEFT);

        VBox topBar = new VBox(6, searchRow);
        topBar.setPadding(new Insets(10));

        Label editorListLabel = new Label("List of words");
        editorListLabel.setStyle("-fx-font-size: 14px;");
        dictionaryEditorWordsList = new ListView<>();
        List<String> names = dc.getWordNamesSorted(); // pure data
        fxNames = FXCollections.observableArrayList(names); // convert for UI

        filteredNames = new javafx.collections.transformation.FilteredList<>(fxNames, s -> true);

        sortedList = new SortedList<>(filteredNames, String.CASE_INSENSITIVE_ORDER);

        dictionaryEditorWordsList.setItems(sortedList);
        editorSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = (newVal == null) ? "" : newVal.trim().toLowerCase();

            if (filter.isEmpty()) {
                // Show all words when search box is empty
                filteredNames.setPredicate(s -> true);
            } else {
                // Show only words that contain the typed text (case-insensitive)
                filteredNames.setPredicate(name ->
                        name.toLowerCase().contains(filter)
                );
            }
        });

        editorSearchBtn.setOnAction(e -> {
            if (!sortedList.isEmpty()) {
                dictionaryEditorWordsList.getSelectionModel().select(0);
                dictionaryEditorWordsList.scrollTo(0);
            }
        });

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

        deleteWordBtn.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #b00020;");

        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        HBox buttonBar = new HBox(12, addWordBtn, editWordBtn, resetListBtn, buttonSpacer, deleteWordBtn);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setStyle("-fx-background-color: #f0f0f0;");

        VBox editorLeftColumn = new VBox(6, editorListLabel, dictionaryEditorWordsList);
        editorLeftColumn.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(editorLeftColumn);
        root.setCenter(centerBox);
        root.setBottom(buttonBar);

        dictionaryEditorScene = new Scene(root, 800, 500);


//        editorSearchField.setOnAction(e -> editorSearchBtn.fire());

        dictionaryEditorWordsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            dc.onEditWord(newVal);
        });
//
        addWordBtn.setOnAction(e -> {
            String w = editorWordArea.getText();
            String d = editorDefinitionArea.getText();
             dc.onAddWord(w, d);
        });
//
        editWordBtn.setOnAction(e -> {
            String selected = dictionaryEditorWordsList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            String newWord = editorWordArea.getText();
            String newDef  = editorDefinitionArea.getText();

            dc.onUpdateWord(selected, newWord, newDef);

            String targetSelection = (newWord == null) ? "" : newWord.trim();
            if (!targetSelection.isEmpty()) {
                Platform.runLater(() -> {
                    dictionaryEditorWordsList.getSelectionModel().select(targetSelection);
                    dictionaryEditorWordsList.scrollTo(targetSelection);
                });
            }
        });
//
//        // Delete selected word (enabled only when selected)
        deleteWordBtn.setOnAction(e -> {
            String selected = dictionaryEditorWordsList.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to delete the word: \"" + selected + "\"?");

            ButtonType btnYes = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(btnYes, btnCancel);

            confirm.showAndWait().ifPresent(response -> {
                if (response == btnYes) {
                    dc.onDeleteWord(selected);
                    editorWordArea.clear();
                    editorDefinitionArea.clear();

                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Word \"" + selected + "\" has been deleted.");
                }
            });
        });

        resetListBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Reset Dictionary");
            confirm.setHeaderText(null);
            confirm.setContentText(
                    "This will reload the dictionary from the source and discard any unsaved changes.\n\n" +
                            "Do you want to continue?"
            );

            ButtonType btnReset = new ButtonType("Reset", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(btnReset, btnCancel);

            confirm.showAndWait().ifPresent(response -> {
                if (response == btnReset) {
                    dc.reloadDictionary();

                    List<String> newList = dc.getWordNamesSorted();
                    fxNames.setAll(newList);
                    editorWordArea.clear();
                    editorDefinitionArea.clear();

                    showAlert(Alert.AlertType.INFORMATION,
                            "Dictionary Reset",
                            "The dictionary has been reloaded.");
                }
            });
        });
        scenes.put(SceneId.EDITOR, dictionaryEditorScene);
    }

    private void dictionaryGameSceneInit() {
        HBox topBar = new HBox();
        Region navSpacer = new Region();
        HBox.setHgrow(navSpacer, Priority.ALWAYS);
        topBar.getChildren().addAll(navSpacer, buildNavBar("GAME"));
        topBar.setPadding(new Insets(10));

        Button show4WordBtn = new Button("GUESS THE DEFINITION");
        Button show4DefinitionBtn = new Button("GUESS THE WORD");
        show4WordBtn.setPrefWidth(220);
        show4DefinitionBtn.setPrefWidth(220);
        String baseGameButtonStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; "
                + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-font-size: 14px; "
                + "-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.25), 8, 0, 0, 2);";
        show4DefinitionBtn.setStyle(baseGameButtonStyle + " -fx-background-color: linear-gradient(#ff9a9e, #d4145a);");
        show4WordBtn.setStyle(baseGameButtonStyle + " -fx-background-color: linear-gradient(#00c6ff, #0072ff);");

        HBox buttonBar = new HBox(16, show4DefinitionBtn, show4WordBtn);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(0, 0, 50, 0));

        centerBox = new VBox(10);
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.CENTER);
        Label gameHint = new Label("Choose a mode to begin");
        gameHint.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
        centerBox.getChildren().add(gameHint);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
//        root.setLeft(dictionaryEditorWordsList);
        root.setCenter(centerBox);
        root.setBottom(buttonBar);

        show4DefinitionBtn.setOnAction(e -> dc.show4DefinitionGame());
        show4WordBtn.setOnAction(e -> dc.show4WordGame());

        dictionaryGameScene = new Scene(root, 800, 500);
        scenes.put(SceneId.GAME, dictionaryGameScene);
    }

    public void showQuestion(String prompt, List<String> options, int correctIndex) {
        centerBox.getChildren().clear();

        Label promptLabel = new Label(prompt);
        promptLabel.setWrapText(true);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.getChildren().add(promptLabel);

        for (int i = 0; i < options.size(); i++) {
            Button btn = new Button(options.get(i));
            btn.setMaxWidth(Double.MAX_VALUE);
            int idx = i;

            btn.setOnAction(e -> {
                for (Node n : content.getChildren()) {
                    if (n instanceof Button) {
                        n.setDisable(true);
                    }
                }

                // highlight correct one
                Button correctBtn = (Button) content.getChildren().get(correctIndex + 1);
                correctBtn.setStyle("-fx-background-color: lightgreen;");

                // if wrong, mark clicked red
                if (idx != correctIndex) {
                    btn.setStyle("-fx-background-color: lightcoral;");
                }
            });

            content.getChildren().add(btn);
        }

        centerBox.getChildren().add(content);
    }


    public void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void showDeleteAlert() {

    }

    public void onWordAdded(String name) {
        Platform.runLater(() -> fxNames.add(name));
    }
    public void onWordDeleted(String name) {
        Platform.runLater(() -> fxNames.remove(name));
    }

    public void showDuplicatedWordAlert(DictionaryEntry newWord){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Duplicated word!");
            alert.setHeaderText(null);
            alert.setContentText("There is another version of this word in the database. Do you want to override it, duplicate, or cancel?");

            ButtonType btnOverride = new ButtonType("Override");
            ButtonType btnDuplicate = new ButtonType("Duplicate");
            ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btnOverride, btnDuplicate, btnCancel);

            alert.showAndWait().ifPresent(response -> {
                if (response == btnOverride) {
                    dc.onOverrideWord(newWord);
                } else if (response == btnDuplicate) {
                    dc.onDuplicateWord(newWord);
                } else {
                    return;
                }
            });
        });
    }




    public void start(Stage stage) {

        this.stageRef = stage;

        dictionaryLookupSceneInit();
        dictionaryEditorSceneInit();
        dictionaryGameSceneInit();
        stage.setTitle("Dictionary App");
        stage.setScene(dictionaryLookupScene);
        stage.show();
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
