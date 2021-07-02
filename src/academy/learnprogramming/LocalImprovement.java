package academy.learnprogramming;

public class LocalImprovement {
    Solution solution;
    int noOfIterations;
    Problem problem;
    double startvalue;
    boolean onlyIn;

    public LocalImprovement(Solution solution, int noOfIterations, Problem problem) {
        this.solution = solution;
        this.noOfIterations = noOfIterations;
        this.problem = problem;
        startvalue = solution.getSolutionValue();
    }

    public LocalImprovement(Solution solution, int noOfIterations, Problem problem,boolean onlyIN) {
        this.solution = solution;
        this.noOfIterations = noOfIterations;
        this.problem = problem;
        startvalue = solution.getSolutionValue();
        this.onlyIn = onlyIN;
    }

    public Solution impmrove(){
        int iteration = 0;
        while (iteration < noOfIterations){
            // innerroutechange: es muss nicht auf Kapazität geachtet werden
            if ((iteration % 2 == 0)||onlyIn){
                int u = 0;
                for (Route r: solution.getRoutes()){
                    for (int i=1; i<r.getRoute().size()-2;i++){
                        for (int j=i+1;j<r.getRoute().size()-1;j++){
                            Route newRoute = new Route(r);
                            newRoute = newRoute.innerChange(j,r.getRoute().get(i),i,r.getRoute().get(j));
                            if (Problem.calculateDistance(newRoute) < Problem.calculateDistance(r)){
                                solution.SetRoute(u,newRoute);
                            }
                        }
                    }
                    u++;
                }

            }else {
                if (iteration>35){
                    iteration++;
                    continue;

                }
                if (solution.getRoutes().size() > 1){

                    for (int i=0; i<solution.getRoutes().size()-1;i++){
                        Route routeA = solution.getRoutes().get(i);
                        if (routeA.getRoute().size()<4){
                            continue;
                        }
                        for (int j=i+1; j<solution.getRoutes().size();j++){
                            Route routeB = solution.getRoutes().get(j);
                            if (routeB.getRoute().size()<4){
                                continue;
                            }

                                for (int k = 1; k < routeA.getRoute().size() -2 ; k = k +2){
                                    Node node1A = routeA.getRoute().get(k);
                                    Node node2A = routeA.getRoute().get(k+1);
                                    for (int l = 1; l<routeB.getRoute().size()-2 ; l = l+2){
                                        Node node1B = routeB.getRoute().get(l);
                                        Node node2B = routeB.getRoute().get(l+1);
                                        Route rA = routeA.swap(node1B,node2B,node1A,node2A,k,k+1);
                                        Route rB = routeB.swap(node1A,node2A,node1B,node2B,l,l+1);
                                        if (rB!= null && rA != null){
                                            if (Problem.calculateDistance(rA)+Problem.calculateDistance(rB)<Problem.calculateDistance(routeA)+Problem.calculateDistance(routeB)) {
//                                                System.out.println("true:(" + node1A + "," + node2A + ") routeA Index: " + i +" mit " + node1B + ", " + node2B  + "routeB Index: " +j );
//                                                System.out.println(routeA);
//                                                System.out.println(routeB);
                                                solution.getRoutes().set(i,rA);
                                                solution.getRoutes().set(j,rB);
                                                routeA = rA;
                                                routeB = rB;
                                                break;
//                                                System.out.println(routeA);
//                                                System.out.println(routeB);

                                            }
                                            solution.calcSolutionValue();
                                        }
                                    }
                                }




                        }
                    }
                }else {
                    iteration++;
                    continue;
                }
            }
            iteration++;
            for (Route r: solution.getRoutes()){
                r.removeCrossPattern();
            }
        }




        solution.calcSolutionValue();
        return solution;
    }
}
