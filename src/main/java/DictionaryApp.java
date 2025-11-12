import dictionary.controller.*;
import dictionary.model.DictionaryModel;
import dictionary.view.DictionaryView;

import dictionary.controller.DictionaryController;
import javafx.application.Application;
import javafx.stage.Stage;


public class DictionaryApp extends Application {
    @Override
    public void start(Stage stage) {
        DictionaryModel dm = new DictionaryModel();
        DictionaryView dv = new DictionaryView();
        DictionaryController dc = new DictionaryController(dm, dv);
        dv.setDc(dc);
//        dc.initialize("slang.txt");
        dc.initialize();
        dv.start(stage);
    }
    public static void main(String[] args) {
        launch(args);
    }

}
