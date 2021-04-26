package academy.learnprogramming;

import java.util.ArrayList;

public class Pheromone {

    int numberOfCustomers;
    double startPheromone;
    double[][] pheromoneMatrix;
    double sum;

    public Pheromone(int numberOfCustomers, double startPheromone) {
        this.numberOfCustomers = numberOfCustomers;
        this.startPheromone = startPheromone;
        pheromoneMatrix = new double[numberOfCustomers+1][numberOfCustomers+1];
        initialize();
        this.sum = sum();
    }

    // Konstruktor für lokale Pheromonmatrix
    public Pheromone(double[][] pm,double sum) {
        pheromoneMatrix = pm;
        this.sum = sum;
    }



    public Pheromone getLocalPheromone(){
        double[][] copyMatrix  = new double[numberOfCustomers+1][numberOfCustomers+1];
        for (int i = 0; i < pheromoneMatrix.length ; i++){
            for (int j = 0; j < pheromoneMatrix.length; j++){
                copyMatrix[i][j] = pheromoneMatrix[i][j];
            }
        }
        return new Pheromone(copyMatrix,this.sum);
    }

    private void initialize(){
        for (int i = 0; i < pheromoneMatrix.length ; i++){
            for (int j = 0; j < pheromoneMatrix.length; j++){
                pheromoneMatrix[i][j] = startPheromone;
            }
        }
    }

    public double sum() {
        double sum = 0;
        for (int i = 0; i < pheromoneMatrix.length ; i++) {
            for (int j =0; j < pheromoneMatrix.length; j++) {
                sum += pheromoneMatrix[i][j];
            }
        }
        return sum;
    }


    public double getPheromone(int i, int j){
        return pheromoneMatrix[i][j];
    }

    //enhancement factor ist rang der elitären Lösung
    public void update(double solutionValue,int start,int end, double enhancementFactor){
        if (!(end == 0 && start == 0)) {
            double pheromoneIncrease = enhancementFactor / solutionValue;
            pheromoneMatrix[start][end] += pheromoneIncrease;
            pheromoneMatrix[end][start] += pheromoneIncrease;
        }




    }

    public void evaporate (double factor){
        for (int i= 0 ; i<pheromoneMatrix.length; i++){
            for (int j= 0; j< pheromoneMatrix.length; j++){
                pheromoneMatrix[i][j]*= factor;
            }
        }

    }

    public void normalize() {
        double factor = sum / sum();
        for (int i = 0; i < pheromoneMatrix.length ; i++) {
            for (int j = 0; j < pheromoneMatrix.length; j++) {
                pheromoneMatrix[i][j] *= factor;
            }
        }
    }
    public double sumSub(ArrayList<Node> customerSet){
        double sumSub = 0;
        for (Node i: customerSet){
            for (Node j: customerSet){
                sumSub += pheromoneMatrix[i.getIndex()][j.getIndex()];
            }
        }
        return sumSub;
    }

    public void normalize(double sumSub, ArrayList<Node> cs) {
        double factor = sumSub / sumSub(cs);
        for (int i = 0; i < pheromoneMatrix.length ; i++) {
            for (int j = 0; j < pheromoneMatrix.length; j++) {
                pheromoneMatrix[i][j] *= factor;
            }
        }
    }

    public  void evaporate(double factor, ArrayList<Node> customers){
        for(int i=0; i<customers.size();i++){
            for (int j= 0; j<customers.size();j++){
                pheromoneMatrix[customers.get(i).getIndex()][customers.get(j).getIndex()]*=factor;
            }
        }
    }

    @Override
    public String toString() {
        String o = "";
        for (int i=0;i<pheromoneMatrix.length;i++){
            o = o + "\n";
            for (int j=0;j<pheromoneMatrix.length;j++){
                o = o + " - " + String.format("%.2f",pheromoneMatrix[i][j]) ;
            }
        }
        return o;
    }
}
