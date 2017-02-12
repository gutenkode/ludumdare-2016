package terminal.filesystem;

/**
 * Created by Peter on 2/11/17.
 */
public class Node {

    protected String name = "Unnamed";
    protected Directory parent;

    public Node(String n, Directory p) {
        name = n;
        parent = p;
    }

    public String name() { return name; }
    public String path() {
        String path = "";
        Node n = this;
        while (n != null) {
            if (n.parent != null)
                path = "/"+n.name+path; // TODO fix this for things other than directories
            n = n.parent();
        }
        if (path.isEmpty())
            path = "/";
        return path;
    }
    public void setParent(Directory d) { parent = d; }
    public Directory parent() { return parent; }
}
