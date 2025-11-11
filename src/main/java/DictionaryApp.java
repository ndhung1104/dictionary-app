import dictionary.controller.*;
import dictionary.model.DictionaryModel;
import dictionary.view.DictionaryView;

public class DictionaryApp {
    public static void main(String[] args) {
        DictionaryModel dm = new DictionaryModel();
        dm.loadDictionary("slang.txt");
        DictionaryView dv = new DictionaryView();
        DictionaryController dc = new DictionaryController(dm, dv);
//        dc.initialize("slang.txt");
        dc.printAll();
    }

}
