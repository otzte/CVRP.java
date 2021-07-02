package academy.learnprogramming;

import java.util.ArrayList;


//Solution Klasse dient als Speicher,
//so wenig Kalkulationen innerhalb der Klasse durchführen wie möglich
public class Solution implements Comparable<Solution> {
    String nameOfSolvingHeuristik;
    ArrayList<Route> routes;
    double solutionValue ;
    static double bestSolutionValue = Integer.MAX_VALUE;

    public Solution(String nameOfSolvingHeuristik) {
        this.nameOfSolvingHeuristik = nameOfSolvingHeuristik;
        routes = new ArrayList<>();

    }

    public Solution(String nameOfSolvingHeuristik,ArrayList<Route> rs) {
        this.nameOfSolvingHeuristik = nameOfSolvingHeuristik;
        routes = rs;
        calcSolutionValue();
    }

    public Solution(String nameOfSolvingHeuristik,Route r) {
        this.nameOfSolvingHeuristik = nameOfSolvingHeuristik;
        routes = new ArrayList<>();
        routes.add(r);
        calcSolutionValue();
    }

    public Solution(Solution s){
        this.solutionValue = s.getSolutionValue();
        this.routes = new ArrayList<>();
        for (Route r: s.getRoutes()){
            this.routes.add(new Route(r));
        }
        this.nameOfSolvingHeuristik = "COPY";
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    //für nearest Neighbor Heuristik
    public void addRoute(Route route, double distance){
        solutionValue += distance;
        this.routes.add(route);
    }

    public void SetRoute(int pos, Route r){
        routes.set(pos, r);
        calcSolutionValue();
    }


    public void addRoute(Route route){
        this.routes.add(route);
        calcSolutionValue();
    }

    public double getSolutionValue() {
        return solutionValue;
    }

    public void setSolutionValue(double value) {
            solutionValue = value;
            if (value < bestSolutionValue){
                bestSolutionValue = value;
            }
    }

    public double calcSolutionValue(){
        double solVal = 0;
        for (Route r: routes){
            solVal += Problem.calculateDistance(r);
        }
        this.solutionValue = solVal;
        if (solVal < bestSolutionValue){
            bestSolutionValue = solVal;
        }
        return solVal;

    }

    @Override
    public int compareTo(Solution o) {
        if (this.getSolutionValue() > o.getSolutionValue()){
            return 1;
        }else if (this.getSolutionValue()< o.getSolutionValue()){
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "" + getSolutionValue();
    }

    public void addRoutes(ArrayList<Route> rs){
        routes.addAll(rs);
        calcSolutionValue();
    }

    public void addSubSolution(Solution subSolution){
        routes.addAll(subSolution.getRoutes());
        this.solutionValue += subSolution.getSolutionValue();
    }

    public int size(){
        return routes.size();
    }







}
