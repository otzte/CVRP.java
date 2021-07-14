package academy.learnprogramming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class SbAS extends Heuristiken {

    private Pheromone globalPheromone;
    private final int vehicleCapacity;
    private final double trailPersistence = 0.95; //varainte b 0.999
    private final double savingsSolutionValue;
    private final double startingPheromone;
    private double savings[][];
    private final int numOfSubProblems = 8; //varainte b 10
    private double averageDistance;

    public SbAS(Problem problem, double startingPheromone, double sv, double averageDistance) {
        super(problem);
        //initialisieren der globalen Pheromonmatrix mit startwert des Pheromons
        this.startingPheromone = startingPheromone;
        vehicleCapacity = problem.getVehicleCapacity();
        savingsSolutionValue = sv;
        calculateSavings(problem.numberOfCustomers, problem.getNodes());
        globalPheromone = new Pheromone(problem.numberOfCustomers, startingPheromone);
        this.averageDistance = averageDistance;
    }

    public void resetPheromone(){
        globalPheromone = new Pheromone(problem.numberOfCustomers, startingPheromone);
    }







    private void calculateSavings(int numOfCustomers, Node[] customers) {
        savings = new double[numOfCustomers + 1][numOfCustomers + 1];
        for (int i = 1; i < numOfCustomers; i++) {
            Node customerI = customers[i];
            for (int j = 1; j < numOfCustomers + 1; j++) {
                Node customerJ = customers[j];
                double cij = problem.getDistances()[customerI.getIndex()][customerJ.getIndex()];
                double c0i = problem.getDistances()[0][customerI.getIndex()];
                double c0j = problem.getDistances()[0][customerJ.getIndex()];
                savings[i][j] = c0i + c0j - cij;
            }
        }
    }

    private class SubproblemA {
        ArrayList<Node> Customers;
        double bestValue;

        public SubproblemA(ArrayList<Node> customers, double bestValue) {
            Customers = customers;
            this.bestValue = bestValue;
        }

        public double getBestValue() {
            return bestValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SubproblemA)){
               return false;
            }
            for (Node n : this.Customers) {
                if (!((SubproblemA) obj).getCustomers().contains(n)) {
                    return false;
                }
            }
            for (Node n : ((SubproblemA) obj).getCustomers()) {
                if (!this.Customers.contains(n)) {
                    return false;
                }
            }
            return true;
        }

        public ArrayList<Node> getCustomers() {
            return Customers;
        }


    }

    private void subUpdate(Solution s, ArrayList<Node> c) {
        //   double sumPherSubSet = globalPheromone.sumSub(c);
//        System.out.println("before:" + sumPherSubSet);
//        globalPheromone.evaporate(trailPersistence,c);
        for (Route r : s.getRoutes()) {
            Node start = r.getRoute().get(0);
            for (Node n : r.getRoute()) {
                if (start.getIndex() == n.getIndex()) {
                    continue;
                }
                globalPheromone.update(s.getSolutionValue(), start.getIndex(), n.getIndex(), 1); //Enhancement Factor bei Varainte A: 1 Variante B: 72
                start = n;
            }
        }
//        System.out.println("in between: " + globalPheromone.sumSub(c));
//        globalPheromone.normalize(sumPherSubSet,c);
//        System.out.println("after: " + globalPheromone.sumSub(c));
    }

    @Override
    public Solution solve() {
        double startTime = System.currentTimeMillis();
        int iteration = 0;
        Solution bestSolution = null;
        boolean flag = true;
        double bestSolutionValue;
        ArrayList<Node> customers = new ArrayList<>();
        for (Node n : problem.getNodes()) {
            customers.add(n);
        }
        while (flag) {

            Solution globalSolution = ACO(customers, problem.numberOfCustomers, Integer.MAX_VALUE, 1, 3, true,false );
            bestSolution = globalSolution;
            System.out.println("Startlösung: " + bestSolution + " nach " + (System.currentTimeMillis() - startTime));


            //RESTET LATER ENTRIES
            XLData = new double[30][2];
            XLData[0][0] = bestSolution.getSolutionValue();
            XLData[0][1] = System.currentTimeMillis() - startTime;
            bestSolutionValue = bestSolution.getSolutionValue();

            ArrayList<SubproblemA> subproblemAS = new ArrayList<>();
            int improvements = 0;
            while (System.currentTimeMillis() - startTime < 120000) {
                // globale Lösung
                //Problem aufteilen entsprechend der Routen
                ArrayList<Node> centersOfGravity = miehleAlgorithm(bestSolution.getRoutes());
                ArrayList<Subproblem> customerSets = sweep(numOfSubProblems, centersOfGravity, globalSolution);
                //subproblem mit größerer Iterationszahl lösen
                Solution subProbCombinedSolution = new Solution("combined");
                for (int i = 0; i < customerSets.size(); i++) {

                    Solution subProbSolution = ACO(customerSets.get(i).getCustomers(), customerSets.get(i).getCustomers().size(), 10000, 10, 3, false,false);
                    subProbCombinedSolution.addSubSolution(subProbSolution);

                    if (subProbSolution.getSolutionValue() < customerSets.get(i).getOriginalDistance()) {
                        subUpdate(subProbSolution, customerSets.get(i).getCustomers());
                    }

                }

                iteration++;


                Solution newGlobalSolution = ACO(customers, problem.numberOfCustomers, Integer.MAX_VALUE, 1, 3, true,false);
                if (newGlobalSolution.getSolutionValue() > subProbCombinedSolution.getSolutionValue()){
                    newGlobalSolution = subProbCombinedSolution;
                }

                if (newGlobalSolution != null) {
                    if (newGlobalSolution.getSolutionValue() < globalSolution.getSolutionValue()) {
                        globalSolution = newGlobalSolution;
                        if (globalSolution.getSolutionValue() < bestSolution.getSolutionValue()) {
                            bestSolution = globalSolution;
                            bestSolutionValue = bestSolution.getSolutionValue();
                            System.out.println("neuer bester zfw" + bestSolutionValue);
                            System.out.println("Laufzeit: " + (System.currentTimeMillis() - startTime));
                            improvements++;
                            XLData[improvements][0] = bestSolution.getSolutionValue();
                            XLData[improvements][1] = System.currentTimeMillis() - startTime;
                        }
                    }
                }


            }
            if (bestSolution.getSolutionValue() < 10000) {
                flag = false;
            }
            iteration = 0;
        }
        System.out.println(bestSolution.getRoutes());
        return bestSolution;
    }




    //Eingabe der Kunden, mit denen ACO durchgeführt werden soll
    //erlaubt flexibilität, sodass sowohl globales Problem als auch subproblem mit der Funktion gelöst werden kann
    //numofelitists : wieviel der besten Lösungen erzeugen eig pheromonupdate?
    public Solution ACO(ArrayList<Node> customers, int antsInColony, double best, int numOfIterations, int numOfElitists, boolean isGlobal, boolean randomAnt) {
        //beziehen einer lokalen Pheromonmatrix
        Pheromone localPheromone;
        if (!isGlobal) {
            localPheromone = this.globalPheromone.getLocalPheromone();
        } else {
            localPheromone = globalPheromone;
        }
        Solution bestSolution = null;
        ArrayList<Solution> allSolutions = new ArrayList<>();
        //jede Ameise soll alle Kunden beliefern. Jeden Kunden genau einmal.

        for (int it = 0; it < numOfIterations; it++) {
            allSolutions.clear();
            for (int ant = 0; ant < antsInColony; ant++) {

                ArrayList<Route> antsRoutes = new ArrayList<>();
                //initialen 0,i,0 Routen erstellen
                for (int i = 0; i < customers.size(); i++) {
                    if (customers.get(i).getIndex() == 0) {
                        continue;
                    }
                    Route antsRoute = new Route(i, vehicleCapacity);
                    antsRoute.addCustomer(customers.get(0));
                    antsRoute.addCustomer(customers.get(i));
                    antsRoute.addCustomer(customers.get(0));
                    antsRoutes.add(antsRoute);

                }

                ArrayList<Zeta> antsAvailableOptions = new ArrayList<>();

                for (int i = 1; i < customers.size() - 1; i++) {
                    Node customerI = customers.get(i);
                    for (int j = i + 1; j < customers.size(); j++) {
                        Node customerJ = customers.get(j);
                        if (problem.getDistances()[customerI.getIndex()][customerJ.getIndex()]>averageDistance){
                            continue;
                        }
                        //Formel Testen für computational Study
                        //Grid Search mit unterschiedlichen Parameterwerten
                        double sav = Math.pow(savings[customerI.getIndex()][customerJ.getIndex()], 5);
                        if (sav <= 0) {
                            continue;
                        }
                        double value = sav * Math.pow(localPheromone.getPheromone(customerI.getIndex(), customerJ.getIndex()), 5);
                        antsAvailableOptions.add(new Zeta(value, customerI, customerJ));
                    }
                }
                Collections.sort(antsAvailableOptions);
                //solange sich noch routen zusammenfügen lassen
                boolean improvementPossible = true;
                while (improvementPossible) {
                    int max = Math.min(Math.min(antsAvailableOptions.size(), antsInColony / 4),25);

                    double sum = 0;
                    for (int i = 0; i < max; i++) {
                        sum += antsAvailableOptions.get(i).getValue();
                    }
                    for (int i = 0; i < max; i++) {
                        double proba = antsAvailableOptions.get(i).getValue() / sum;
                        if (i == 0) {
                            antsAvailableOptions.get(i).setProbability(proba);
                            continue;
                        }
                        antsAvailableOptions.get(i).setProbability(proba + antsAvailableOptions.get(i - 1).getProbability());
                    }
                    //Ermittlung einer zufälligen Kombination
                    double monteCarlo = Math.random();
                    Zeta candidate = null;
                    int candidateID = 0;
                    for (int i = 0; i < antsAvailableOptions.size(); i++) {

                        double zetaprob = antsAvailableOptions.get(i).getProbability();
                        if (zetaprob > monteCarlo) {
                            candidate = antsAvailableOptions.get(i);
                            candidateID = i;
                            break;
                        }
                    }
                    //zwei zugehörigen routen ermitteln und zusammenfügen wenn die Kunden nicht mitten in einer Route drinstecken
                    if (candidate != null) {
                        boolean thisStart = false;
                        boolean partnerStart = false;
                        Route route1 = null;
                        Route route2 = null;
                        for (Route r : antsRoutes) {
                            if (r.contains(candidate.getCustomer1().getIndex())) {
                                if (checkIfStartOrEnd(r, candidate.getCustomer1().getIndex())) {
                                    route1 = r;
                                    if(route1.getRoute().indexOf(candidate.getCustomer1())==1){
                                        thisStart = true;
                                    }
                                }
                            }
                            if (r.contains(candidate.getCustomer2().getIndex())) {
                                if (checkIfStartOrEnd(r, candidate.getCustomer2().getIndex())) {
                                    route2 = r;
                                    if (route2.getRoute().indexOf(candidate.getCustomer2())==1){
                                        partnerStart=true;
                                    }
                                }
                            }

                        }
                        boolean routesExist = (route1 != null) && (route2 != null);
                        if (routesExist) {
                            if (route1 == route2) {
                                antsAvailableOptions.remove(candidateID);
                                continue;
                            }
                            if (route1.getCurrentDemand() + route2.getCurrentDemand() <= problem.getVehicleCapacity()) {
                                for (int i = 0; i < antsRoutes.size(); i++) {
                                    Route oldRoute = antsRoutes.get(i);
                                    if (oldRoute.contains(route1.getRoute().get(1).getIndex()) || oldRoute.contains(route2.getRoute().get(1).getIndex())) {
                                        antsRoutes.remove(i);
                                        i--;
                                    }
                                    //Routen mit kombinierten Knoten entfernen weil saving nicht mehr realisiert werden kann

                                }
                                // ZUFÄLLIGE VERKNÜPFUNG
                                if (ant % 3 == 0 && randomAnt){
                                    thisStart = true;
                                    partnerStart = true;
                                }
                                Route newRoute = route1.concatenateRoutes(route2, thisStart, partnerStart);
                                antsRoutes.add(newRoute);
//                                for (int i=0;i<antsAvailableOptions.size();i++){
//                                    if (antsAvailableOptions.get(i).contains(candidate.customer1) || antsAvailableOptions.get(i).contains(candidate.customer2)){
//                                        antsAvailableOptions.remove(i);
//                                        i--;
//                                    }
//                                }
                            }
                        }

                        antsAvailableOptions.remove(candidateID);
                    } else {
                        improvementPossible = false;
                    }
                }
                Solution antsSolution = new Solution("ant" + ant, antsRoutes);
//              antsSolution = localSearch(antsSolution, 1000);
                LocalImprovement improvement = new LocalImprovement(antsSolution, 45, problem);
                antsSolution = improvement.impmrove();
                allSolutions.add(antsSolution);

            }
            Collections.sort(allSolutions);
            if (allSolutions.get(0).getSolutionValue() < best) {
                best = allSolutions.get(0).getSolutionValue();
                bestSolution = allSolutions.get(0);
            }//Variante A : alle k besten lösungen erzeugen update
            double sumPherSubSet = globalPheromone.sumSub(customers);
            localPheromone.evaporate(trailPersistence);
            numOfElitists = Math.min(numOfElitists, allSolutions.size());
            for (int i = 0; i < numOfElitists; i++) {
                ArrayList<Route> antsRoutes = allSolutions.get(i).getRoutes();
                Solution antsSolution = allSolutions.get(i);
//                    System.out.println(antsSolution.getRoutes());
                for (Route r : antsRoutes) {
                    for (int j = 1; j < r.getRoute().size(); j++) {
                        localPheromone.update(antsSolution.getSolutionValue(), r.getRoute().get(j - 1).getIndex(), r.getRoute().get(j).getIndex(), numOfElitists - i);
//                            System.out.println("i:   " + r.getRoute().get(j - 1).getIndex() +  "   j:   " + r.getRoute().get(j).getIndex() + " --->"+ globalPheromone.pheromoneMatrix[r.getRoute().get(j - 1).getIndex()][r.getRoute().get(j).getIndex()]);
                    }
                }

            }
//                localPheromone.normalize(sumPherSubSet, customers);
            // } //Variante B: nur bei einer verbesserung erzeugen k besten lösungen ein update

        }
        return bestSolution;

    }

    private boolean checkIfStartOrEnd(Route r, int id) {
        if (r.getRoute().get(1).getIndex() == id || r.getRoute().get(r.getRoute().size() - 2).getIndex() == id) {
            return true;
        }
        return false;
    }

    private Solution localSearch(Solution startingSolution, int numOfIterations) {
        Random rand = new Random();
        Solution neighborSolution = null;


        //Ausgangslösung zu Beginn Startlösung gleichsetzen
        ArrayList<ArrayList<Integer>> currentBestArray = copyArray(startingSolution.getRoutes());
        ArrayList<ArrayList<Integer>> tempArray = copyArray(startingSolution.getRoutes());


        double neighborValue = 10000;
        double currentBest;
        int counter = 0;

        while (counter < numOfIterations) {
            // System.out.println(counter);
            ArrayList<ArrayList<Integer>> copyOfTempArray = copyArray(tempArray, 2);
            //Tausch zweier zufälliger Kunden: NBL erzeugen
            int routeIndex1 = rand.nextInt(tempArray.size());
            int routeIndex2 = rand.nextInt(tempArray.size());
            ArrayList<Integer> Route1 = tempArray.get(routeIndex1);
            ArrayList<Integer> Route2 = tempArray.get(routeIndex2);
            int nodeIndex1 = rand.nextInt(Route1.size() - 2) + 1;
            int nodeIndex2 = rand.nextInt(Route2.size() - 2) + 1;
            int temp = tempArray.get(routeIndex1).get(nodeIndex1);
            tempArray.get(routeIndex1).set(nodeIndex1, tempArray.get(routeIndex2).get(nodeIndex2));
            tempArray.get(routeIndex2).set(nodeIndex2, temp);

            //falls Ladung nicht reinpasst soll Tausch rückgängig gemacht werden
            if (!checkSpace(tempArray)) {
                tempArray = copyOfTempArray;
            }

            //neue Distanz berechnen
            neighborValue = calculateDistance(tempArray);
            currentBest = calculateDistance(currentBestArray);


            if (neighborValue >= currentBest) {
                tempArray = copyArray(currentBestArray, 1);
            } else {
                currentBestArray = copyArray(tempArray, 1);
            }
            counter++;
        }
        ArrayList<Route> rs = retransform(currentBestArray);
        return new Solution("improvement", rs);


    }

    private ArrayList<ArrayList<Integer>> copyArray(ArrayList<Route> arrayToCopy) {
        ArrayList<ArrayList<Integer>> arrayCopy = new ArrayList<>();
        for (int i = 0; i < arrayToCopy.size(); i++) {
            ArrayList<Integer> route = new ArrayList<>();
            Route r = arrayToCopy.get(i);
            for (int j = 0; j < r.getRoute().size(); j++) {
                route.add(r.getRoute().get(j).getIndex());
            }
            arrayCopy.add(route);
        }
        return arrayCopy;
    }

    private ArrayList<ArrayList<Integer>> copyArray(ArrayList<ArrayList<Integer>> arrayToCopy, int x) {
        ArrayList<ArrayList<Integer>> arrayCopy = new ArrayList<>();
        for (int i = 0; i < arrayToCopy.size(); i++) {
            ArrayList<Integer> innerArrayToCopy = arrayToCopy.get(i);
            arrayCopy.add(new ArrayList<Integer>());
            for (int j = 0; j < innerArrayToCopy.size(); j++) {
                arrayCopy.get(i).add(innerArrayToCopy.get(j));
            }
        }
        return arrayCopy;
    }

    private boolean checkSpace(ArrayList<ArrayList<Integer>> tempArray) {
//        TEMPARRAY WIRD RICHTIG ÜBERGEBEN
        Node[] nodes = problem.getNodes();
        int load = 0;

        for (int i = 0; i < tempArray.size(); i++) {
            ArrayList<Integer> route = tempArray.get(i);
            for (int j = 0; j < route.size(); j++) {
                int nodeId = route.get(j);
                load += nodes[nodeId].getDemand();
            }
            if (load > vehicleCapacity) {
                return false;
            }
            load = 0;
        }
        return true;

    }

    public double calculateDistance(ArrayList<ArrayList<Integer>> solutionArray) {
        double[][] distanceMatrix = problem.getDistances();
        double distance = 0;
        int start = 0;
        for (int i = 0; i < solutionArray.size(); i++) {
            for (int j = 0; j < solutionArray.get(i).size(); j++) {
                distance += distanceMatrix[start][solutionArray.get(i).get(j)];
                start = solutionArray.get(i).get(j);

            }
        }
        return distance;
    }

    private ArrayList<Route> retransform(ArrayList<ArrayList<Integer>> routes) {
        int i = 0;
        ArrayList<Route> routes1 = new ArrayList<>();
        for (ArrayList<Integer> r : routes) {
            Route route = new Route(i, problem.getVehicleCapacity());
            i++;
            for (Integer integer : r) {
                route.addCustomer(problem.getNodes()[integer]);
            }
            routes1.add(route);
        }
        return routes1;
    }

    private ArrayList<Node> miehleAlgorithm(ArrayList<Route> solution) {
        //dummy nodes, die center of gravity darstellen
        ArrayList<Node> centersOfGravity = new ArrayList<>();
        int counter = 0;
        for (Route route : solution) {
            int sumOfDemand = 0;
            double xpos = 0;
            double ypos = 0;
            for (Node node : route.getRoute()) {
                sumOfDemand += node.getDemand();
                xpos += node.getDemand() * node.getxPos();
                ypos += node.getDemand() * node.getyPos();
            }
            xpos /= sumOfDemand;
            ypos /= sumOfDemand;
            Node dummy = new Node(counter, (int) xpos, (int) ypos, (int) Problem.calculateDistance(route));
            centersOfGravity.add(dummy);
            counter++;
        }
        return centersOfGravity;
    }

    public class Subproblem implements Comparable<Subproblem> {
        private ArrayList<Node> customers;
        private double angleToSweepStart;
        private int index;
        private double originalDistance;

        public Subproblem(ArrayList<Node> customers, double angleToSweepStart, double pD) {
            this.customers = customers;
            this.angleToSweepStart = angleToSweepStart;
            this.originalDistance = pD;
        }

        public void addDist(double dist) {
            originalDistance += dist;
        }

        @Override
        public int compareTo(Subproblem o) {
            if (this.angleToSweepStart > o.getAngleToSweepStart()) {
                return 1;
            } else if (this.angleToSweepStart < o.getAngleToSweepStart()) {
                return -1;
            }
            return 0;
        }

        public double getOriginalDistance() {
            return originalDistance;
        }

        public boolean addCustomers(ArrayList<Node> customers) {
            return this.customers.addAll(customers);
        }

        public double getAngleToSweepStart() {
            return angleToSweepStart;
        }

        public ArrayList<Node> getCustomers() {
            return customers;
        }

        public void setIndex(int i) {
            this.index = i;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return "" + angleToSweepStart;
        }
    }

    private ArrayList<Subproblem> sweep(int numOfSubProblems, ArrayList<Node> cog, Solution solution) {
        ArrayList<Subproblem> subProblems = new ArrayList<>();
        ArrayList<Subproblem> cogs = new ArrayList<>();
        double start = 360 * Math.random();
        start = Math.toRadians(start);
        double radius;
        int counter = 0;
        for (Node node : cog) {

            //Vektor für Center of gravity
            double x_cenOfGrav = node.getxPos() - problem.depotXPos;
            double y_cenOfGrav = node.getyPos() - problem.depotYPos;
            radius = Math.sqrt(x_cenOfGrav * x_cenOfGrav + y_cenOfGrav * y_cenOfGrav);
            //Vektor für Start des sweeps
            double x_startSweep = Math.cos(start) * radius;
            double y_startSweep = Math.sin(start) * radius;
//            System.out.println("sweep x Start " + x_startSweep + "sweep y Start " + y_startSweep);
//            System.out.println("cog x " + x_cenOfGrav + "cog y " + y_cenOfGrav);

            double alpha = Math.toDegrees(Math.acos((x_cenOfGrav * x_startSweep + y_cenOfGrav * y_startSweep) / Math.pow(radius, 2)));
            //start in quadrant 1
            boolean otherSide = false;
            if (x_startSweep >= 0 && y_startSweep >= 0) {
                if (y_cenOfGrav > y_startSweep || x_cenOfGrav < -(x_startSweep)) {
                    otherSide = true;
                }
                //Start in quadrant 4
            } else if (x_startSweep >= 0 && y_startSweep < 0) {
                if (y_cenOfGrav > -(y_startSweep) || x_cenOfGrav > x_startSweep) {
                    otherSide = true;
                }
                //Start in quadrant 3

            } else if (x_startSweep < 0 && y_startSweep <= 0) {
                if (x_cenOfGrav > -x_startSweep || y_cenOfGrav < y_startSweep) {
                    otherSide = true;
                }
            }
            // Start in quadrant 2
            else if (x_startSweep < 0 || y_startSweep > 0) {
                if (x_cenOfGrav < x_startSweep || y_cenOfGrav < -y_startSweep) {
                    otherSide = true;
                }
            }
            if (otherSide) {
                alpha = 360 - alpha;
            }
            //Hilfsklasse mit Winkel zw sweep Start und Center of gravity + den zugehörigen Kunden zum CoG
            cogs.add(new Subproblem(solution.getRoutes().get(counter).getRoute(), alpha, Problem.calculateDistance(solution.getRoutes().get(counter))));
            counter++;

        }
        Collections.sort(cogs);
        for (int i = 0; i < cogs.size(); i++) {
            cogs.get(i).setIndex(i);
        }

        int routesPerSubProblem = (cogs.size() / numOfSubProblems);

        if (routesPerSubProblem == 0) {
            return cogs;
        }

        int n_extra = cogs.size() - numOfSubProblems * routesPerSubProblem;

        int noOfSubset = 1;
        int cogsInSubset = 0;
        double prevDist = 0;
        ArrayList<Node> subset = new ArrayList<>();
        for (Subproblem s : cogs) {
            int cogsToAdd = routesPerSubProblem;
            if (noOfSubset <= n_extra) {
                cogsToAdd = routesPerSubProblem + 1;
            }
            if (cogsInSubset < cogsToAdd) {
                subset.addAll(s.getCustomers());
                prevDist += s.getOriginalDistance();
                cogsInSubset++;
            }
            if (cogsInSubset == cogsToAdd) {
                for (int x = 1; x < subset.size(); x++) {
                    if (subset.get(x).getIndex() == 0) {
                        subset.remove(x);
                        x--;
                    }
                }
                subProblems.add(new Subproblem(subset, 0, prevDist));
                prevDist = 0;
                subset = new ArrayList<>();
                noOfSubset++;
                cogsInSubset = 0;
            }
        }


//        int n = 0;
//        ArrayList<Node> tempCustomers = new ArrayList<>();
//        for (int i = 0; i < cogs.size(); i++) {
//            ArrayList<Node> customers = cogs.get(i).getCustomers();
//            if (n < routesPerSubProblem + addition) {
//                tempCustomers.addAll(customers);
//                n++;
//            } else {
//                n = 0;
//                for (int x = 1; x < tempCustomers.size(); x++) {
//                    if (tempCustomers.get(x).getIndex() == 0) {
//                        tempCustomers.remove(x);
//                        x--;
//                    }
//                }
//                subProblems.add(tempCustomers);
//                tempCustomers = new ArrayList<>();
//                tempCustomers.addAll(customers);
//            }
//        }
//        for (int x = 1; x < tempCustomers.size(); x++) {
//            if (tempCustomers.get(x).getIndex() == 0) {
//                tempCustomers.remove(x);
//                x--;
//            }
//        }
//        if (tempCustomers.size() > 0) {
//            subProblems.add(tempCustomers);
//        }
        return subProblems;
    }


}
