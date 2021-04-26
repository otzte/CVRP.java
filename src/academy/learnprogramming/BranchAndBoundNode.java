package academy.learnprogramming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BranchAndBoundNode {

    //verfügbaren Nodes auf index reduziert
    ArrayList<Integer> availableSuccessors;
    Map<Integer, BranchAndBoundNode> children = new HashMap<>();
    static int nodeNumber;
    static double UB = Solution.bestSolutionValue;
    int index;
    int xPos;
    int yPos;
    int demand;
    int rank;
    Route fixatedRoute;
    ArrayList<Route> finishedRoutes = new ArrayList<>();
    boolean finished = false;
    Node[] originalNodes = Problem.nodes;
    double LB;
    double solutionValue;

    public BranchAndBoundNode(ArrayList<Integer> references, int index, int xPos, int yPos, Route fixatedRoute, int demand, ArrayList<Route> finishedRoutes) {
        this.availableSuccessors = references;
        this.index = index;
        this.xPos = xPos;
        this.yPos = yPos;
        this.fixatedRoute = fixatedRoute;
        this.demand = demand;
        if (finishedRoutes != null) {
            this.finishedRoutes = finishedRoutes;
        }
        this.LB = calculateLowerBound();
        nodeNumber ++;
    }

    public Map<Integer, BranchAndBoundNode> createSubNodes() {
        //es werden nur kinder erzeugt, wenn lb < ub
        if (this.LB < UB ) {
            //in availableSuccessors sind die Kunden, die noch besucht werden müssen
            for (Integer childIndex : availableSuccessors) {
                boolean childFinished = false;
                ArrayList<Integer> childsAvailableSuccessors = new ArrayList<>(availableSuccessors);
                ArrayList<Route> finishedRouteOfChild = new ArrayList<>(finishedRoutes);
                //depot darf nicht entfernt werden, wird ein Kunde besucht, wird er aus availableSuccessors removed. Depot kann mehrfach besucht werden.
                Route newRoute = new Route(this.fixatedRoute);
                newRoute.addCustomer(originalNodes[childIndex]);
                //keine depot zu depot Route zulassen
                if (this.index == 0 && childIndex == 0) {
                    continue;
                } else if (childIndex != 0) {
                    //nur removen wenn rein passt
                    childsAvailableSuccessors.remove(childIndex);
                } else {
                    //falls depot ausgehend von kunden angefahren wird
                    finishedRouteOfChild.add(newRoute);
                    if (childsAvailableSuccessors.size()==1){
                        childFinished = true;
                    }
                    int routeIndex = newRoute.routeIndex;
                    int vehCapacity = newRoute.vehicleCapacity;
                    // bisherige route abschließen und den fertigen routen anheften
//                newRoute.addCustomer(originalNodes[0]);
                    // neue route eröffnen die im depot startet
                    newRoute = new Route(routeIndex + 1, vehCapacity);
                    newRoute.addCustomer(originalNodes[0]);
                }
                Node childNode = originalNodes[childIndex];

                //erstes Auslotungskriterium: nur falls die Kapazität des Fahrzeugs ausreicht um den zusätzlichen Kunden zu beliefern wird das Kind erzeugt
                if (this.demand + childNode.getDemand() < newRoute.vehicleCapacity) {
                    int demand = 0;
                    if (this.index != 0){
                        demand = this.demand;
                    }
                    BranchAndBoundNode bnbChild = new BranchAndBoundNode(childsAvailableSuccessors, childIndex, childNode.getxPos(), childNode.getyPos(), newRoute, demand + childNode.getDemand(), finishedRouteOfChild);
                    children.put(childIndex, bnbChild);
                    bnbChild.finished = childFinished;
                }
            }
            return new HashMap<>(children);
        }
        return new HashMap<>();
    }

    public Map<Integer, BranchAndBoundNode> getChildren() {
        return children;
    }

    public Route getFixatedRoute() {
        return fixatedRoute;
    }

    public boolean allCustomersServed() {
        boolean allServed = true;
        for (Node node : originalNodes) {
            if (node.getIndex() == 0) {
                continue;
            }
            if (!node.isServed()) {
                allServed = false;
            }
        }
        return allServed;
    }

    private double calculateLowerBound() {
        return routeFertigMachen();
    }  // + ClusterKruskal

    private double routeFertigMachen() {
        //Term für Lower Bound aus Erweiterung der fixierten Route
        ArrayList<Node> fixatedTerm = new ArrayList<>(fixatedRoute.getRoute());
        ArrayList<Node> availableNodes = getUnservedNodes();
        //Zwischenspeicher der Kunden, die beim Kruskal hinzugefügt werden
        ArrayList<Node> undoDelivery = new ArrayList<>();
        double lowerBoundFixated = getFixatedLength(fixatedTerm);
        int demand = this.demand;
        //solange noch mehr Nodes als das depot zur verfügung stehen
        while (availableNodes.size()>1 && spaceAvailable(demand)) {
            double minDistance = Integer.MAX_VALUE;

            int end = -1;
            for (Node fixated : fixatedTerm) {
                for (Node available : availableNodes) {
                    //depot soll nicht beliefert werden, d.h. es bleibt für immer in der Liste availableNodes
                    if (available.getIndex() == 0 ){
                        continue;
                    }
                    double distance = Problem.distances[fixated.getIndex()][available.getIndex()];
                    if (minDistance > distance) {
                        minDistance = distance;
                        end = available.getIndex();
                    }
                }
            }
            //Kruskal Werte
            lowerBoundFixated += minDistance;
            demand += originalNodes[end].getDemand();


            originalNodes[end].makeDelivery();
            fixatedTerm.add(originalNodes[end]);
            availableNodes.remove(originalNodes[end]);
            if (!undoDelivery.contains(originalNodes[end])) {
                undoDelivery.add(originalNodes[end]);
            }

        }
        //plus kürzeste Verbindung der Nodes zum depot
        double shortestDistanceToDepot = Integer.MAX_VALUE;
        for (Node node: fixatedTerm){
            if (node.getIndex() == 0 ){
                continue;
            }
            double distance = Problem.distances[0][node.getIndex()];
            if (shortestDistanceToDepot>distance){
                shortestDistanceToDepot = distance;
            }
        }
        lowerBoundFixated += shortestDistanceToDepot;




        //weil die Kruskal-Kunden in Wirklichkeit nicht beliefert wurden muss der Hilfseintrag wieder Rückgängig gemacht werden
        for (Node node : undoDelivery) {
            node.undoDelivery();
        }

        return lowerBoundFixated;
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

    private ArrayList<Node> getUnservedNodes() {
        ArrayList<Node> unservedCustomers = new ArrayList<>();
        for (Integer customerId : availableSuccessors) {
            unservedCustomers.add(originalNodes[customerId]);
        }
        return unservedCustomers;
    }

    public double getLB() {
        return LB;
    }

    public ArrayList<Route> getFinishedRoutes() {
        return new ArrayList<Route>(finishedRoutes);
    }

    public boolean isFinished() {
        return finished;
    }

    public double calculateSolutionValue(){
        double solutionvalue = 0;
            for (Route route: finishedRoutes){
                solutionvalue += getFixatedLength(route.getRoute());
            }
            this.solutionValue = solutionvalue;
            return solutionvalue;

    }

    public double getSolutionValue() {
        return solutionValue;
    }
}
