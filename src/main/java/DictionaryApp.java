import dictionary.controller.*;

public class DictionaryApp {
    public void main(String[] args) {
        DictionaryController dc = new DictionaryController();
        dc.initialize("./slang.txt");
        dc.printAll();
    }

}
