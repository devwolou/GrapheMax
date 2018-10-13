package com.example.kaelhosvalde.graphemax;

public class Arc {

    private Node nodeFrom;

    public Arc(Node _nodeFrom) {
        this.nodeFrom = _nodeFrom;
    }

    public Node getNodeFrom() {
        return nodeFrom;
    }
}
