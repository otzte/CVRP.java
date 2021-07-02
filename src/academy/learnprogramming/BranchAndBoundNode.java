package academy.learnprogramming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BranchAndBoundNode implements Comparable<BranchAndBoundNode> {

    //verfügbaren Nodes auf index reduziert
    ArrayList<Node> availableSuccessors;
    ArrayList<BranchAndBoundNode> children = new ArrayList<>();
    static int nodeNumber;
    static double UB;
    int index;
    int xPos;
    int yPos;
    int rank;
    Route fixatedRoute;
    boolean finished = false;
    double LB;
    double solutionValue;

    public BranchAndBoundNode(ArrayList<Node> availableSuccessors, int xPos, int yPos, Route fixatedRoute) {
        this.availableSuccessors = availableSuccessors;
        this.xPos = xPos;
        this.yPos = yPos;
        this.fixatedRoute = fixatedRoute;
        this.LB = calculateLowerBound();
//        System.out.println("AVAIL SUCC " + availableSuccessors.size());
//        System.out.println("LB = " + LB);
//        System.out.println("UB = " + UB);
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public ArrayList<BranchAndBoundNode> createSubNodes() {
        ArrayList<BranchAndBoundNode> subNodes = new ArrayList<>();
        //es werden nur kinder erzeugt, wenn lb < ub
        if (this.LB < UB) {
            //in availableSuccessors sind die Kunden, die noch besucht werden müssen
            for (Node child : availableSuccessors) {
                ArrayList<Node> subsAvailableSuccessors = new ArrayList<>(availableSuccessors);
                subsAvailableSuccessors.remove(child);
                Route subsRoute = new Route(fixatedRoute);
                subsRoute.addCustomer(child);
                BranchAndBoundNode subNode = new BranchAndBoundNode(subsAvailableSuccessors, child.getxPos(), child.getyPos(), subsRoute);
                subNodes.add(subNode);
                if (subsAvailableSuccessors.size() == 0) {
                    subNode.finished = true;
                }
            }

        }
        return subNodes;
    }

    public ArrayList<BranchAndBoundNode> getChildren() {
        return children;
    }

    public Route getFixatedRoute() {
        return fixatedRoute;
    }

//    public boolean allCustomersServed() {
//        boolean allServed = true;
//        for (Node node : originalNodes) {
//            if (node.getIndex() == 0) {
//                continue;
//            }
//            if (!node.isServed()) {
//                allServed = false;
//            }
//        }
//        return allServed;
//    }

    public double calculateLowerBound() {
        return routeFertigMachen();
    }  // + ClusterKruskal

    private double routeFertigMachen() {
        //Term für Lower Bound aus Erweiterung der fixierten Route
        double lowerBoundFixated = Problem.calculateDistance(fixatedRoute);

        //Kruskal
        ArrayList<KruskalEdge> kruskalEdges = new ArrayList<>();
        for (int i = 0; i < availableSuccessors.size(); i++) {
            Node ni = availableSuccessors.get(i);
            for (int j = i + 1; j < availableSuccessors.size(); j++) {
                Node nj = availableSuccessors.get(j);
                kruskalEdges.add(new KruskalEdge(ni, nj, Problem.distances[ni.getIndex()][nj.getIndex()]));
            }
        }
        Collections.sort(kruskalEdges);

        ArrayList<KruskalEdge> minimalSpanningTree = new ArrayList<>();
        for (int i = 0; i < kruskalEdges.size(); i++) {
            KruskalEdge newEdge = kruskalEdges.get(i);
            if (noCircle(minimalSpanningTree, newEdge)) {
                for (KruskalEdge e : minimalSpanningTree) {
                    if (newEdge.hasSameNode(e)) {
                        newEdge.connect(e);
                    }
                }
                for (KruskalEdge e : newEdge.connectedWith) {
                    e.connectedWith = kruskalEdges.get(i).connectedWith;
                }
                    minimalSpanningTree.add(kruskalEdges.get(i));

                if (minimalSpanningTree.size() >= availableSuccessors.size() - 1) {
                    break;
                }
            }

        }
//        ArrayList<Route> spanningTreeEdges = new ArrayList<>();
//        for (KruskalEdge e : minimalSpanningTree) {
//            ArrayList<Node> edge = new ArrayList<>();
//            edge.add(e.i);
//            edge.add(e.j);
//            spanningTreeEdges.add(new Route(edge, 20, 20, 10));
//        }
//        Draw.drawRoutes(spanningTreeEdges,"SpanningTree",1000);
//        System.out.println(minimalSpanningTree.size());

        if (availableSuccessors.size()>1) {
            for (KruskalEdge e : minimalSpanningTree) {
                lowerBoundFixated += e.cij;
            }
        }


        // kürzeste Kante zwischen anfang fixierter Route und Spannbaum
        double minStart = Integer.MAX_VALUE;
        double minEnd = Integer.MAX_VALUE;
        Node lastCustomer = fixatedRoute.getNode(fixatedRoute.size() - 1);
        for (Node n : availableSuccessors) {
            if (minStart > Problem.distances[0][n.getIndex()]) {
                minStart = Problem.distances[0][n.getIndex()];
            }
            if (minEnd > Problem.distances[lastCustomer.getIndex()][n.getIndex()]) {
                minEnd = Problem.distances[lastCustomer.getIndex()][n.getIndex()];
            }
        }

        if (fixatedRoute.size()>1) {
            lowerBoundFixated += minEnd + minStart;
        }

        return lowerBoundFixated;
    }

    private boolean noCircle(ArrayList<KruskalEdge> minimalSpanningTree, KruskalEdge edge) {
        for (KruskalEdge e : minimalSpanningTree) {
            if (e.hasTwoIntersections(edge)) {
                return false;
            }
        }
        return true;
    }


    class KruskalEdge implements Comparable<KruskalEdge> {
        Node i;
        Node j;
        double cij;
        ArrayList<KruskalEdge> connectedWith = new ArrayList<>();

        public KruskalEdge(Node i, Node j, double cij) {
            this.i = i;
            this.j = j;
            this.cij = cij;
            connectedWith.add(this);
        }

        @Override
        public int compareTo(KruskalEdge o) {
            return Double.compare(this.cij, o.cij);
        }

        private boolean hasSameNode(KruskalEdge e) {
            return this.i.equals(e.i) || this.j.equals(e.i) || this.i.equals(e.j) || this.j.equals(e.j);
        }

        private boolean hasSameNode(Node n) {
            return this.i.equals(n) || this.j.equals(n);
        }

        private void connect(KruskalEdge e) {
            connectedWith.addAll(e.connectedWith);
        }

        private boolean hasTwoIntersections(KruskalEdge e) {
            boolean i1 = false;
            boolean i2 = false;

            for (KruskalEdge thisEdges : connectedWith) {
                if (thisEdges.hasSameNode(e.i)) {
                    i1 = true;
                }
                if (thisEdges.hasSameNode(e.j)) {
                    i2 = true;
                }
            }
            return i1 & i2;
        }

        @Override
        public String toString() {
            return " i: " + i.getIndex() + " j: " + j.getIndex();
        }
    }

    private double getFixatedLength(ArrayList<Node> fixRoute) {
        double dist = 0;
        int start = 0;
        for (Node node : fixRoute) {
            dist += Problem.distances[start][node.getIndex()];
            start = node.getIndex();
        }
        return dist;
    }

    private boolean spaceAvailable(int demand) {
        return demand < Problem.vehicleCapacity;
    }

//    private ArrayList<Node> getUnservedNodes() {
//        ArrayList<Node> unservedCustomers = new ArrayList<>();
//        for (Integer customerId : availableSuccessors) {
//            unservedCustomers.add(originalNodes[customerId]);
//        }
//        return unservedCustomers;
//    }

    public double getLB() {
        return LB;
    }

//    public ArrayList<Route> getFinishedRoutes() {
//        return new ArrayList<Route>(finishedRoutes);
//    }

    public boolean isFinished() {
        return finished;
    }

    public double calculateSolutionValue() {
        this.solutionValue = Problem.calculateDistance(fixatedRoute);
        return this.solutionValue;
    }

    public double getSolutionValue() {
        return solutionValue;
    }

    @Override
    public int compareTo(BranchAndBoundNode o) {
        return Double.compare(this.LB, o.LB);
    }

    @Override
    public String toString() {
        return "BranchAndBoundNode{" +
                "LB=" + LB +
                '}';
    }
}
