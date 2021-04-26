package academy.learnprogramming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Route {
    int routeIndex;
    ArrayList<Node> route;
    int currentVehiclePosition;
    int currentDemand;
    int vehicleCapacity;

    public Route(int routeIndex, int vehicleCapacity) {
        this.routeIndex = routeIndex;
        route= new ArrayList<>();
        currentVehiclePosition = 0;
        currentDemand = 0;
        this.vehicleCapacity = vehicleCapacity;
    }

    public Route(Route route){
        this.routeIndex = route.routeIndex;
        this.route = new ArrayList<>(route.route);
        this.currentDemand = route.currentDemand;
        this.vehicleCapacity = route.vehicleCapacity;
    }

    public void setRouteIndex(int routeIndex) {
        this.routeIndex = routeIndex;
    }

    public Route(ArrayList<Node> route, int vehicleCapacity, int demand, int index){
        this.route = route;
        this.vehicleCapacity = vehicleCapacity;
        this.currentDemand = demand;
        this.routeIndex = index;
    }



    // wird ausgeführt wenn Kunde der Route hinzugefügt wird
    public void addCustomer(Node node){
        currentDemand += node.getDemand();
        currentVehiclePosition = node.getIndex();
        route.add(node);
        if (node.getIndex()>0) {
            node.makeDelivery();
        }
    }

    public void addCustomer(Node node, int index){
        currentDemand += node.getDemand();
        currentVehiclePosition = node.getIndex();
        route.add(index,node);
        if (node.getIndex()>0) {
            node.makeDelivery();
        }
    }

    class Saving implements Comparable<Saving>{
        double saving;
        Route route;

        public Saving(double saving, Route route) {
            this.saving = saving;
            this.route = route;
        }

        public double getSaving() {
            return saving;
        }

        public Route getRoute() {
            return route;
        }

        @Override
        public int compareTo(Saving o) {
            if (this.getSaving() < o.getSaving()){
                return 1;
            } else if (this.getSaving() == o.getSaving()){
                return 0;
            }
            return -1;
        }
    }


    // gibt Speicherklasse aus: Route mit Saving derjenigen Routenkombination mit dem größten erzielbaren Saving
    // lässt nur Routenkombinationen zu, die die Bedarfsrestriktion einhalten können
    public Route.Saving calculateSaving(double[][] distanceMatrix, Route candidate){

        double maxSaving = 0;
        Route newRoute;
        Route.Saving bestSaving = new Route.Saving(0,null);
        //falls Routen sich kombinieren lassen
        if (this.currentDemand + candidate.currentDemand <= vehicleCapacity) {
            // Start Start Verknüpfung : aus [0,i,...,0] und [0,j,...,0] wird [0,...,i,j,...0]
            double cij = distanceMatrix[route.get(1).getIndex()][candidate.getRoute().get(1).getIndex()];
            double c0i = distanceMatrix[0][route.get(1).getIndex()];
            double c0j = distanceMatrix[0][candidate.getRoute().get(1).getIndex()];
            double saving = c0i + c0j - cij;
            if (saving > maxSaving) {
//                System.out.println("1");
                newRoute = concatenateRoutes(candidate, true, true);
                maxSaving = saving;
                bestSaving = new Route.Saving(saving,newRoute);
            }


            // Ende Ende
            cij = distanceMatrix[route.get(route.size() - 2).getIndex()][candidate.getRoute().get(candidate.getRoute().size() - 2).getIndex()];
            c0i = distanceMatrix[0][route.get(route.size() - 2).getIndex()];
            c0j = distanceMatrix[0][candidate.getRoute().get(candidate.getRoute().size() - 2).getIndex()];
            saving = c0i + c0j - cij;
            if (saving > maxSaving) {
//                System.out.println("2");
                newRoute = concatenateRoutes(candidate, false, false);
                maxSaving = saving;
                bestSaving = new Route.Saving(saving,newRoute);
            }


            //Start Ende
            cij = distanceMatrix[route.get(1).getIndex()][candidate.getRoute().get(candidate.getRoute().size() - 2).getIndex()];
            c0i = distanceMatrix[0][route.get(1).getIndex()];
            c0j = distanceMatrix[0][candidate.getRoute().get(candidate.getRoute().size() - 2).getIndex()];
            saving = c0i + c0j - cij;
            if (saving > maxSaving) {
//                System.out.println("3");
                newRoute = concatenateRoutes(candidate, true, false);
                maxSaving = saving;
                bestSaving = new Route.Saving(saving,newRoute);
            }


            //Ende Start
            cij = distanceMatrix[route.get(route.size() - 2).getIndex()][candidate.getRoute().get(1).getIndex()];
            c0i = distanceMatrix[0][route.get(route.size() - 2).getIndex()];
            c0j = distanceMatrix[0][candidate.getRoute().get(1).getIndex()];
            saving = c0i + c0j - cij;
            if (saving > maxSaving) {
//                System.out.println("4");
                newRoute = concatenateRoutes(candidate, false, true);
                bestSaving = new Route.Saving(saving,newRoute);
            }


        }
        if (bestSaving.getRoute() == null){
            return null;
        }

        return bestSaving;
    }

    //wenn nicht start ´mit partner verbunden wird, dann wird das Ende einer Route mit dem partner verbunden
    public Route concatenateRoutes(Route partner,boolean combineAtThisStart, boolean combineAtPartnersStart){
        Route newRoute;
        if (combineAtThisStart && combineAtPartnersStart){
            newRoute = new Route(this);
            newRoute.removeDepot(true);
            int counter = 0;
            for (int i = partner.getRoute().size() -1; i > 0;i--){
                newRoute.addCustomer(partner.getRoute().get(i),counter);
                counter++;
            }
            return newRoute;
        }else if (combineAtThisStart){
            newRoute = new Route(this);
            newRoute.removeDepot(true);
            for (int i = 0; i<partner.getRoute().size() - 1;i++){
                newRoute.addCustomer(partner.getRoute().get(i),i);
            }
            return newRoute;
        }else if (combineAtPartnersStart){
            newRoute = new Route(this);
            newRoute.removeDepot(false);
            for (int i = 1; i<partner.getRoute().size();i++){
                newRoute.addCustomer(partner.getRoute().get(i));
            }
            return newRoute;
        }else {
            newRoute = new Route(this);
            newRoute.removeDepot(false);
            for (int i = partner.getRoute().size() -2; i >= 0;i--){
                newRoute.addCustomer(partner.getRoute().get(i));
            }
            return newRoute;
        }
    }

    private void removeDepot(boolean atStart){
        if (atStart){
            this.route.remove(0);
        }else {
            this.route.remove(route.size()-1);
        }
    }



    public int getCurrentVehiclePosition() {
        return currentVehiclePosition;
    }

    public boolean checkSpace(Node node){
        return currentDemand + node.getDemand() <= vehicleCapacity;
    }

    public ArrayList<Node> getRoute() {
        return route;
    }

    @Override
    public String toString() {
        String o = "[";
        for (Node customer: route){
            o = o +  ", " + customer.getIndex();
        }
        o = o + " ::: " + Problem.calculateDistance(this) + "  sumOfDemand: " + currentDemand +  "]";
        return o;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj instanceof Route){
            Route o = (Route) obj;
            return o.toString().equals(this.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode()+56;
    }

    public int getCurrentDemand() {
        return currentDemand;
    }

    public boolean contains(int index){
        boolean containsIndex = false;
        for (Node node: route){
            if (node.getIndex() == index){
                containsIndex = true;
            }
        }
        return containsIndex;
    }

    public Route innerChange(int node1Pos, Node node1, int node2Pos, Node node2){
        route.set(node1Pos,node1);
        route.set(node2Pos,node2);
        return this;
    }

    public Route swap(Node newNode1, Node newNode2, Node oldNode1, Node oldNode2,int n1Pos, int n2Pos){
        if (this.currentDemand - oldNode1.getDemand() - oldNode2.getDemand() + newNode1.getDemand() + newNode2.getDemand() > vehicleCapacity){
            return null;
        }
        Route newRoute = new Route(this);
        newRoute.currentDemand = newRoute.currentDemand + newNode1.getDemand() + newNode2.getDemand() - oldNode1.getDemand() - oldNode2.getDemand();
        newRoute.set(n1Pos,newNode1);
        newRoute.set(n2Pos,newNode2);
        return newRoute;
    }

    public void set(int index, Node node){
        route.set(index,node);
    }

    public Route swap(Node newNode1, Node oldNode1, int n1Pos){
        if (this.currentDemand - oldNode1.getDemand()  + newNode1.getDemand()  > vehicleCapacity){
            return null;
        }
        Route newRoute = new Route(this);
        newRoute.currentDemand+= newNode1.getDemand();
        newRoute.currentDemand-= oldNode1.getDemand();
        newRoute.getRoute().remove(oldNode1);
        newRoute.getRoute().add(n1Pos,newNode1);
        return newRoute;
    }

    public Route tabuRemove(Node n){
        Route newRoute = new Route(this);
        newRoute.currentDemand -= n.getDemand();
        newRoute.getRoute().remove(n);
        return newRoute;
    }

    public Route tabuAdd(Node n){
        if (this.currentDemand   + n.getDemand()  > vehicleCapacity){
            return null;
        }
        Route newRoute = new Route(this);
        newRoute.currentDemand += n.getDemand();
        newRoute.getRoute().add(1,n);
        return newRoute;
    }

    public boolean removeCrossPattern(){
        boolean warKreuzig = false;
        ArrayList<Node> newRoute;
        for (int i=0; i<route.size()-2;i++){
            Node edge1Start = route.get(i);
            Node edge1End = route.get(i+1);
            for (int j=i+2; j<route.size()-1; j++){
                Node edge2Start = route.get(j);
                Node edge2End = route.get(j+1);
                if (hasIntersection(edge1Start,edge1End,edge2Start,edge2End)){
                    warKreuzig = true;
                    newRoute = new ArrayList<>();
                    boolean switchww = false;
                    int position = 0;
                    for (int k = 0; k<route.size();k++){
                        if (!switchww){
                            newRoute.add(route.get(k));
                            if (route.get(k).getIndex()==edge1Start.getIndex()){
                                switchww = true;
                                position = k+1;
                            }

                        }else {
                            newRoute.add(position,route.get(k));
                            if (route.get(k).getIndex() == edge2Start.getIndex()){
                                switchww = false;
                            }
                        }
                    }
                    Route newR = new Route(newRoute,this.vehicleCapacity,this.currentDemand,this.routeIndex);
                    if (Problem.calculateDistance(newR)<Problem.calculateDistance(this)){
                        this.route = newR.getRoute();
                    }
                }


            }
        }
        return warKreuzig;
    }

    private boolean hasIntersection(Node edge1Start, Node edge1End, Node edge2Start,Node edge2End){
        double m1 = (double)(edge1End.getyPos() -edge1Start.getyPos())/(double) (edge1End.getxPos()-edge1Start.getxPos() + 0.0000000001);
        double m2 = (double)(edge2End.getyPos() -edge2Start.getyPos())/(double)(edge2End.getxPos()-edge2Start.getxPos() + 0.0000000001);
        double c1 = edge1Start.getyPos() - m1 * edge1Start.getxPos();
        double c2 = edge2Start.getyPos() - m2 * edge2Start.getxPos();
        double x = (c2-c1)/(m1-m2);
        if (edge1Start.getIndex() == 0 && edge2End.getIndex() == 0){
            return false;
        }

        if (edge1Start.getxPos() >= edge1End.getxPos()){
            if (x > edge1Start.getxPos() || x < edge1End.getxPos()){
                return false;
            }
        }

        if (edge1Start.getxPos() < edge1End.getxPos()){
            if (x < edge1Start.getxPos() || x > edge1End.getxPos()){
                return false;
            }
        }

        if (edge2Start.getxPos() >= edge2End.getxPos()){
            if (x > edge2Start.getxPos() || x < edge2End.getxPos()){
                return false;
            }
        }

        if (edge2Start.getxPos() < edge2End.getxPos()){
            if (x < edge2Start.getxPos() || x > edge2End.getxPos()){
                return false;
            }
        }
        return true;

    }

    public boolean isPlatzPatrone(){
        if (this.route.get(1).getIndex()==0){
            return true;
        }
        return false;
    }

    public boolean contains(Node node){
        for (Node n: route){
            if (n.getIndex()== node.getIndex()){
                return true;
            }
        }
        return false;
    }

}
