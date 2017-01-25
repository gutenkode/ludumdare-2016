package terminal.file;

/**
 * Created by Peter on 1/24/17.
 */
public class TextFile implements TerminalFile {

    private String name, contents;
    public TextFile(String n, String c) {
        name = n;
        contents = c;
    }

    @Override
    public String name() {
        return name+".txt";
    }

    @Override
    public String contents() {
        return contents;
    }
}
