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

    public void initialize() {
        this.dm.loadDictionary("slang.txt");
    }

    public void printAll() {
        List<DictionaryEntry> lst = dm.getWordsList();
        for (DictionaryEntry de : lst) {
            System.out.println(de.getWord());
        }

        System.out.println("Number of words: " + Integer.toString(lst.size()));
    }

    public void onSearch(String word) {
//        System.out.println("From controller: " + word);
        if (word == null || word.isBlank()) {
            dv.showMessage("Please enter a word");
            return;
        }
        word = word.trim();

        DictionaryEntry wordEntry = dm.findWordByName(word);
        if (wordEntry == null)  {
//            System.out.println("No word found");
            dv.showMessage("Word not found!");
        } else {
//            System.out.println("Found word");
            dv.showWord(wordEntry);
        }
    }
}
