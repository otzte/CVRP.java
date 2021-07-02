package academy.learnprogramming;

import java.util.ArrayList;
import java.util.Random;

public class Problem {
    int numberOfCustomers;
    int numberOfVehicles;
    static Node[] nodes;
    static double[][] distances;
    final int depotXPos;
    final int depotYPos;
    public static int vehicleCapacity;
    private int sumOfDemand;  //prüfen, ob problem überhaupt lösbar ist
    private int seed;

    public Problem(int numberOfCustomers, int numberOfVehicles, int vehicleCapacity, int depotXPos, int depotYPos,int seed) {
        this.numberOfCustomers = numberOfCustomers;
        this.numberOfVehicles = numberOfVehicles;
        this.depotXPos = depotXPos;
        this.depotYPos = depotYPos;
        this.seed = seed;
        nodes = new Node[numberOfCustomers +1];
        distances = new double[numberOfCustomers+1][numberOfCustomers+1];
        generateRandomNodes();
        generateDistanceMatrix();
        calculateSumOfDemand();
        this.vehicleCapacity = vehicleCapacity;
    }

    //Array mit Kunden erstellen, zufällig auf einer Fläche von 0-100 Längeneinheiten verteilt
    private void generateRandomNodes(){
            Random rand = new Random(seed);
            nodes[0] = new Node(depotXPos, depotYPos);
            for (int i = 1; i <= numberOfCustomers; i++) {
                int xpos = rand.nextInt(100) + 1;
                int ypos = rand.nextInt(100) + 1;
                int demand = rand.nextInt(10) + 1;
                nodes[i] = new Node(i, xpos, ypos, demand);
            }
    }

    public Node[] getNodes() {
        return nodes;
    }

    //Distanzmatrix erstellen: Werte werden auf null Nachkommastellen gerundet
    private void generateDistanceMatrix(){
            double[][] distanceMatrix = new double[numberOfCustomers + 1][numberOfCustomers + 1];
            double x_distance;
            double y_distance;

            for (int i = 0; i <= this.numberOfCustomers; i++) {
                for (int j = 0; j <= this.numberOfCustomers; j++) {
                    x_distance = nodes[i].getxPos() - nodes[j].getxPos();
                    y_distance = nodes[i].getyPos() - nodes[j].getyPos();

                    double distance = Math.sqrt(x_distance * x_distance + y_distance * y_distance);
                    distance = Math.round(distance);
                    distanceMatrix[i][j] = distance;

                }
            }
            this.distances = distanceMatrix;


        if (numberOfCustomers<25) {
            System.out.println("\t Distanzmatrix:");
            for (int i = 0; i < distances.length; i++) {
                if (i > 0) {
                    System.out.println();
                }
                for (int j = 0; j < distances.length; j++) {
                    System.out.printf("%3.0f", distances[i][j]);
                    System.out.print("\t");
                }
            }
        }
    }

    public double[][] getDistances() {
        return distances;
    }

    private void calculateSumOfDemand(){
        int sumOfDemand = 0;
        for (Node node: nodes){
            sumOfDemand += node.getDemand();
        }
        this.sumOfDemand = sumOfDemand;
    }

    public int getVehicleCapacity() {
        return vehicleCapacity;
    }


    public static double calculateDistance(Route r){
        ArrayList<Node> route = r.getRoute();
        int start = 0;
        double distance = 0;
        for (Node node: route){
            distance+=distances[start][node.getIndex()];
            start = node.getIndex();
        }
        return distance;
    }



    public void resetNodes(){
        for (Node node : nodes){
            node.undoDelivery();
        }
    }
}
