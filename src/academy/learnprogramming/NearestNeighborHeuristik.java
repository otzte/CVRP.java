package academy.learnprogramming;

import java.util.ArrayList;

public class NearestNeighborHeuristik extends Heuristiken{
    Node[] nodes;
    double[][] distances;

    public NearestNeighborHeuristik(Problem problem) {
        super(problem);
        this.nodes = problem.getNodes();
        this.distances = problem.getDistances();
    }

    private boolean allCustomersServed(){
        boolean allServed = true;
        for (Node node: nodes){
            if (!node.isServed()){
                allServed = false;
            }
        }
        return allServed;
    }

    @Override
    public Solution solve() {
        Solution solution = new Solution("Nearest Neighbor Heuristik");
        int routeIndex = 0;
        Route route = new Route(routeIndex,super.problem.getVehicleCapacity());
        //jede Route startet im Depot
        route.addCustomer(nodes[0]);
        //solange noch nicht alle Kunden beliefert sind
        while (!allCustomersServed()){
            int currentPostition = route.getCurrentVehiclePosition();

            int nearestNeighborIndex = 0;
            double minDistance = Integer.MAX_VALUE;
            for (int i=1; i<nodes.length;i++){
                if ((!nodes[i].isServed())){
                    double distanceToNeighbor = distances[currentPostition][i];
                    if (distanceToNeighbor<minDistance) {
                        minDistance = distanceToNeighbor;
                        nearestNeighborIndex = i;
                    }
                }
            }
            if (nearestNeighborIndex>0) {
                Node candidate = nodes[nearestNeighborIndex];
                if(route.checkSpace(candidate)){
                    route.addCustomer(candidate);
                }else {
                    route.addCustomer(nodes[0]);
                    double distance = super.problem.calculateDistance(route);
                    solution.addRoute(route,distance);
                    routeIndex ++;
                    route = new Route(routeIndex,super.problem.getVehicleCapacity());
                    route.addCustomer(nodes[0]);
                }
            }
        }
        route.addCustomer(nodes[0]);
        double distance = super.problem.calculateDistance(route);
        solution.addRoute(route,distance);
        return solution;
    }





}
