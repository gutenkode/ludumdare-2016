package terminal.filesystem;

import java.util.ArrayList;

/**
 * Created by Peter on 2/11/17.
 */
public class Directory extends Node {

    protected ArrayList<Node> children;

    public Directory(String n, ArrayList<Node> c, Directory d) {
        super(n,d);
        children = c;
    }

    @Override
    public String name() { return "/"+name; }
    public void addChild(Node n) { children.add(n); n.parent = this; }
    public ArrayList<Node> contents() { return children; }
}
