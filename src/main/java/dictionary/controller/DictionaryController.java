package dictionary.controller;
import java.util.List;

import dictionary.model.*;

public class DictionaryController {
    private DictionaryModel dm = new DictionaryModel();

    public void initialize(String path) {
        dm.loadDictionary(path);
    }

    public void printAll() {
        List<DictionaryEntry> lst = dm.getWordsList();
        for (DictionaryEntry de : lst) {
            System.out.println(de.getWord());
        }

        System.out.println("Number of words: " + Integer.toString(lst.size()));
    }
}
