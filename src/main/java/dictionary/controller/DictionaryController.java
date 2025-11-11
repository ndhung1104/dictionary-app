package dictionary.controller;
import java.util.List;

import dictionary.model.*;
import dictionary.view.DictionaryView;

public class DictionaryController {
    private DictionaryModel dm;
    private DictionaryView dv;

    public DictionaryController(DictionaryModel dm, DictionaryView dv) {
        this.dm = dm;
        this.dv = dv;
    }

    public void printAll() {
        List<DictionaryEntry> lst = dm.getWordsList();
        for (DictionaryEntry de : lst) {
            System.out.println(de.getWord());
        }

        System.out.println("Number of words: " + Integer.toString(lst.size()));
    }
}
