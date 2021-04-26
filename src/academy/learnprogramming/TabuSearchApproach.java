package academy.learnprogramming;

import java.util.*;

public class TabuSearchApproach extends Heuristiken {

    private final Solution startingSolution;
    ArrayList<Puzzle> solutionPuzzles = new ArrayList<>();
    private Variable[][] searchHistory;
    private final int L = 150;

    public TabuSearchApproach(Problem problem, Solution startingSolution) {
        super(problem);
        this.startingSolution = startingSolution;
        initializeSearchHistory();
    }

    private void initializeSearchHistory() {
        searchHistory = new Variable[problem.numberOfCustomers + 1][problem.numberOfCustomers + 1];
        for (int i = 0; i <= problem.numberOfCustomers; i++) {
            for (int j = 0; j <= problem.numberOfCustomers; j++) {
                searchHistory[i][j] = new Variable();
            }
        }
    }

    private class Variable {
        private double searchHistoryValue = 0;
        private boolean locked = false;

        public Variable() {
        }

        public void update(double value) {
            this.searchHistoryValue += value;
        }

        public double getSearchHistoryValue() {
            return searchHistoryValue;
        }

        public void lock() {
            this.locked = true;
        }

        public void increase(double solutionvalue) {
            searchHistoryValue += (double) 1 / solutionvalue;
        }

        @Override
        public String toString() {
            return " " + searchHistoryValue;
        }
    }

    private class Puzzle implements Comparable<Puzzle> {
        private Route route;
        private double solutionValue;
        private double proba = 0;

        public double getProba() {
            return proba;
        }

        public void setProba(double proba) {
            this.proba = proba;
        }

        public Puzzle(Route route, double solutionValue) {
            this.route = route;
            this.solutionValue = solutionValue;
        }

        public Route getRoute() {
            return route;
        }

        public double getSolutionValue() {
            return solutionValue;
        }

        public boolean contains(Node node) {
            for (Node n : route.getRoute()) {
                if (n.getIndex() == node.getIndex()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return " SOLVAL: " + solutionValue;
        }

        @Override
        public int compareTo(Puzzle o) {
            if (o.getSolutionValue() < this.solutionValue) {
                return 1;
            } else if (o.getSolutionValue() > this.getSolutionValue()) {
                return -1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Puzzle)) {
                return false;
            }
            for (Node n : ((Puzzle) obj).getRoute().getRoute()) {
                if (!this.route.contains(n)) {
                    return false;
                }
            }

            for (Node n : this.route.getRoute()) {
                if (!((Puzzle) obj).getRoute().contains(n)) {
                    return false;
                }
            }

            if (((Puzzle) obj).getSolutionValue() == this.getSolutionValue()) {
                return true;
            }
            return false;

        }
    }


    private void addSolutionPuzzles(Solution s) {
        for (Route r : s.getRoutes()) {
            if (r.getRoute().size() > 3) {
                Puzzle newPuzzle = new Puzzle(r, s.getSolutionValue());
                if (!solutionPuzzles.contains(newPuzzle)) {
                    solutionPuzzles.add(newPuzzle);
                }
            }
        }
    }

    private void updateSearchHistory(Solution bestSolution) {
        for (Route r : bestSolution.getRoutes()) {
            int start = 0;
            for (int i = 1; i < r.getRoute().size(); i++) {
                int jIndex = r.getRoute().get(i).getIndex();
                searchHistory[start][jIndex].update(1d / bestSolution.getSolutionValue());
                searchHistory[jIndex][start].update(1d / bestSolution.getSolutionValue());
                start = jIndex;
            }
        }
    }

    @Override
    public Solution solve() {
        ArrayList<Node> customers = new ArrayList<>();
        Collections.addAll(customers, problem.getNodes());
        Solution bestSolution = new SbAS(problem, 0.14, 10000).ACO(customers, 100, 10000, 10, 3, true);

        updateSearchHistory(bestSolution);
        addSolutionPuzzles(startingSolution);
        double startAngle = 0;

        int startSubProblemIndex = 0;
        // ZEHN STARTLÖSUNGEN ERZEUGEN
        for (int grit = 0; grit < 2; grit++) {
            for (int it = 0; it < 5; it++) {
                int noOfSubProblems = 4;
                System.out.println("ITERATION " + it + " : " + bestSolution);
                //Problem partitionieren
                ArrayList<Node> cogs = miehleAlgorithm(bestSolution.getRoutes());
                ArrayList<Subproblems> subProblems = sweep(noOfSubProblems, cogs, bestSolution, startAngle); //10


                //Teilprobleme lösen und zusammenfügen
                Solution combinedSolution = null;
                for (Subproblems subPorblem : subProblems) {
                    if (combinedSolution == null) {
                        combinedSolution = tabuSearch(subPorblem.getRoutes(), 30, false);//30

                    } else {
                        combinedSolution.addSubSolution(tabuSearch(subPorblem.getRoutes(), 30, false));
                    }
                }

                for (int i = 0; i < combinedSolution.getRoutes().size(); i++) {
                    if (combinedSolution.getRoutes().get(i).isPlatzPatrone()) {
                        combinedSolution.getRoutes().remove(i);
                        i--;
                    }
                }


                addSolutionPuzzles(combinedSolution);
                System.out.println(combinedSolution);
                if (bestSolution.getSolutionValue() > combinedSolution.getSolutionValue()) {
                    bestSolution = combinedSolution;
                    updateSearchHistory(bestSolution);
                }
                startAngle = -sweep(noOfSubProblems, miehleAlgorithm(bestSolution.getRoutes()), bestSolution, 0).get(startSubProblemIndex).getAngleToZero() + 3;
                startSubProblemIndex+= (int)(Math.random() * 2 + 1);
                if (startSubProblemIndex >= noOfSubProblems) {
                    startSubProblemIndex = 0 ;
                    startAngle = 0;
                }
            }
            Collections.sort(solutionPuzzles);
            //2a
            for (int iteration = 1; iteration <= 400; iteration++) {
                for (int i = L; i < solutionPuzzles.size(); i++) {
                    solutionPuzzles.remove(i);
                    i--;
                }
                ArrayList<Puzzle> solPuzCopy = new ArrayList<>(solutionPuzzles);
                int max = Math.min(L, solPuzCopy.size() - 1);
                for (int i = max; i >= 0; i--) {
                    double previousProba = 0;
                    if (i < max) {
                        previousProba = solPuzCopy.get(i + 1).getProba();
                    }
                    solPuzCopy.get(i).setProba(previousProba + (double) (2 * (max - i + 1)) / ((max + 1) * (max + 2)));
                }

                //2b
                Solution newSolution = new Solution("Taillard iteration " + iteration);

                while (solPuzCopy.size() > 0) {
                    if (solPuzCopy.size() == 1) {
                        newSolution.addRoute(solPuzCopy.get(0).getRoute());
                        solPuzCopy.remove(0);
                    }
                    //Auswahl einer zufälligen Route
                    double rand = Math.random();
                    Route routeToAdd = null;
                    for (int i = 1; i < solPuzCopy.size(); i++) {
                        Puzzle p = solPuzCopy.get(i);
                        if (p.getProba() < rand) {
                            routeToAdd = solPuzCopy.get(i - 1).getRoute();
                            solPuzCopy.remove(solPuzCopy.get(i - 1));
                            break;
                        }
                    }
                    if (routeToAdd != null) {
                        newSolution.addRoute(routeToAdd);
                        for (Node n : routeToAdd.getRoute()) {
                            if (n.getIndex() == 0) {
                                continue;
                            }
                            for (int i = 0; i < solPuzCopy.size(); i++) {
                                if (solPuzCopy.get(i).contains(n)) {
                                    solPuzCopy.remove(i);
                                    i--;
                                }
                            }
                        }

                        max = Math.min(L, solPuzCopy.size() - 1);
                        for (int i = max; i >= 0; i--) {
                            double previousProba = 0;
                            if (i < max) {
                                previousProba = solPuzCopy.get(i + 1).getProba();
                            }
                            solPuzCopy.get(i).setProba(previousProba + (double) (2 * (max - i + 1)) / ((max + 1) * (max + 2)));
                        }
                    }
                }
                ArrayList<Node> remainingCustomers = getUnservedCustomers(newSolution);
                remainingCustomers.add(0, problem.getNodes()[0]);
                newSolution.addSubSolution(tabuSearch(remainingCustomers, 150, false));
                addSolutionPuzzles(newSolution);
                Collections.sort(solutionPuzzles);
                if (newSolution.getSolutionValue() < bestSolution.getSolutionValue()) {
                    bestSolution = newSolution;
                    System.out.println(newSolution + " in Iteration " + iteration);
                }
            }
        }
        return bestSolution;
    }

    private ArrayList<Node> getUnservedCustomers(Solution s) {
        ArrayList<Node> allCustomers = new ArrayList<>();
        Collections.addAll(allCustomers, problem.getNodes());

        for (Route r : s.getRoutes()) {
            for (Node n : r.getRoute()) {
                allCustomers.remove(n);
            }
        }
        return allCustomers;
    }

    private class Alternative implements Comparable<Alternative> {
        private double delta;
        Solution s;
        private double probability;
        Node node1;
        Node node2;
        int node1OldRouteIndex;
        int node2OldRouteIndex;
        String type;


        public Alternative(double delta, Solution s, Node node1, Node node2, int node1OldRouteIndex, int node2OldRouteIndex, String type) {
            this.delta = delta;
            this.s = s;
            this.node1 = node1;
            this.node2 = node2;
            this.node1OldRouteIndex = node1OldRouteIndex;
            this.node2OldRouteIndex = node2OldRouteIndex;
            this.type = type;
        }

        public void setProbability(double probability) {
            this.probability = probability;
        }

        @Override
        public int compareTo(Alternative o) {
            if (o.getDelta() > this.getDelta()) {
                return 1;
            } else if (o.getDelta() < this.getDelta()) {
                return -1;
            }
            return 0;
        }

        public double getDelta() {
            return delta;
        }


        @Override
        public String toString() {
            return " " + node1.getIndex() + " geht in Route " + node2OldRouteIndex + " Verbesserung: " + delta + "   TYP: " + type;
        }

        public double getProbability() {
            return probability;
        }

        public Solution getSolution() {
            return s;
        }
    }


    private Solution tabuSearch(ArrayList<Node> subproblemROutes, int noOfIterations, boolean useSH) {
        double kMax = 0;
        //Tabuduration nach Taillard 1993
        int tabuDuration = (int) -Math.round(subproblemROutes.size() * (0.05 + 0.05 *Math.random() )); //0,1 kleinere Tabu Duration besser?
        //ACO zur Erzeugung von Startlösung um möglichst unterschiedliche Startlösungen für die Subprobleme zu erhalten
        Solution startingSolution = new SbAS(problem, 0.5, 10000).ACO(subproblemROutes, 100, 10000, 5, 3, true);//100/4


        //        // Eine Random 0,i,0 Route hinzufügen
//        ArrayList<Route> savingsSolutionRoutes = startingSolution.getRoutes();
//        int routeIndex = (int)Math.floor(Math.random()*savingsSolutionRoutes.size());
//        //ersten Kunden aus der zufällig gewählten Route entfernen
//        Node nodeToRemove =  savingsSolutionRoutes.get(routeIndex).getRoute().get(1);
//        savingsSolutionRoutes.get(routeIndex).getRoute().remove(nodeToRemove);
//        Route newRoute = new Route(savingsSolutionRoutes.size(), problem.getVehicleCapacity());
//        newRoute.addCustomer(problem.getNodes()[0]);
//        newRoute.addCustomer(nodeToRemove);
//        newRoute.addCustomer(problem.getNodes()[0]);
//        savingsSolutionRoutes.add(newRoute);


//        System.out.println("STARTWERT" + startingSolution);
        Solution bestSolution = new Solution(startingSolution);
        Solution neighborSolution = new Solution(startingSolution);
        LocalImprovement li;
        int[][] tabuList = new int[problem.numberOfCustomers + 1][problem.numberOfCustomers + 1];
        int[] frequency = new int[problem.numberOfCustomers + 1];
        if (startingSolution.getRoutes().size() < 2) {
            return startingSolution;
        }
        for (int it = 1; it < noOfIterations; it++) {
            double penFac = 1 + 4 * Math.random();
            // TAUSCHALTENATIVEN AUFSTELLEN
            ArrayList<Alternative> options = new ArrayList<>();
            for (int i = 0; i < neighborSolution.getRoutes().size() - 1; i++) {
                for (int j = i + 1; j < neighborSolution.getRoutes().size(); j++) {
                    Route r1 = neighborSolution.getRoutes().get(i);
                    Route r2 = neighborSolution.getRoutes().get(j);
                    for (int k = 1; k < r1.getRoute().size() - 1; k++) {
                        boolean zeroOneDone = false;
                        for (int l = 1; l < r2.getRoute().size() - 1; l++) {
                            Node n1 = r1.getRoute().get(k);
                            Node n2 = r2.getRoute().get(l);


                            // (1-1) TAUSCH
                            Route newRoute1 = r1.swap(n2, n1, k);
                            Route newRoute2 = r2.swap(n1, n2, l);

                            if ((newRoute1 != null) && (newRoute2 != null)) {
                                if (newRoute2.getRoute().get(l).getIndex() == newRoute2.getRoute().get(l + 1).getIndex() || newRoute1.getRoute().get(k).getIndex() == newRoute1.getRoute().get(k + 1).getIndex()) {
                                    System.out.println("FALSCH");
                                }
                                Solution dummy = new Solution(neighborSolution);
                                double frequencyNode = ((double) (frequency[n1.getIndex()] + frequency[n2.getIndex()]) / (2 * it));
                                //STRAFTERM FÜR ZU OFT WECHSELNDE NODES
                                double penalty = (frequencyNode * penFac * kMax * Math.pow(Math.abs(tabuDuration) * (subproblemROutes.size() -1) * 2, 0.5));
                                if (tabuList[r2.routeIndex][n1.getIndex()] >= 0 && tabuList[r1.routeIndex][n1.getIndex()] >= 0) {
                                    dummy.SetRoute(i, newRoute1);
                                    dummy.SetRoute(j, newRoute2);
                                    li = new LocalImprovement(dummy, 45, problem, true);
                                    dummy = li.impmrove();
                                    int n1SuccessorIndex = newRoute2.getRoute().indexOf(n1) + 1;
                                    int n1PredecessorIndex = newRoute2.getRoute().indexOf(n1) - 1;
                                    int n2SuccessorIndex = newRoute1.getRoute().indexOf(n2) + 1;
                                    int n2PredecessorIndex = newRoute1.getRoute().indexOf(n2) - 1;
                                    if (n1PredecessorIndex < 0) {
                                        System.out.println("TFG§");
                                    }

                                    double searchHistValue = searchHistory[n1.getIndex()][n1SuccessorIndex].getSearchHistoryValue() + searchHistory[n1PredecessorIndex][n1.getIndex()].getSearchHistoryValue()
                                            + searchHistory[n2.getIndex()][n2SuccessorIndex].getSearchHistoryValue() + searchHistory[n2PredecessorIndex][n2.getIndex()].getSearchHistoryValue();
                                    if (Math.abs(dummy.getSolutionValue() - neighborSolution.getSolutionValue()) > kMax) {
                                        kMax = Math.abs(dummy.getSolutionValue() - neighborSolution.getSolutionValue());
                                    }


                                    double delta = (1d / (dummy.getSolutionValue() + penalty));
                                    if (useSH) {
                                        delta += searchHistValue;
                                    }
                                    options.add(new Alternative(delta, dummy, n1, n2, i, j, "1-1"));
                                    //ASPIRATIONSKRITERIUM: ZUG SEHR STARK GEWICHTEN DASS ER AUF JEDEN FALL GENWÄHLT WIRD
                                } else if (dummy.getSolutionValue() < bestSolution.getSolutionValue()) {
                                    options.add(new Alternative(1000000d / (dummy.getSolutionValue() + penalty), dummy, n1, n2, i, j, "1-1"));
                                }

                            }


                            // (0-1) TAUSCH
                            Route newRoute3 = r1.tabuRemove(n1);
                            Route newRoute4 = r2.tabuAdd(n1);
                            if (newRoute4 != null && !zeroOneDone) {
                                if (newRoute4.getRoute().get(l).getIndex() == newRoute4.getRoute().get(l + 1).getIndex()) {
                                    System.out.println("FALSCH");
                                }

                                Solution dummy = new Solution(neighborSolution);
                                //STRAFTERM FÜR ZU OFT WECHSELNDE NODES
                                double penalty = ((double) (frequency[n1.getIndex()]) / it) * penFac * kMax * Math.pow(Math.abs(tabuDuration) * (subproblemROutes.size() -1) * 2 , 0.5);
                                if (tabuList[r2.routeIndex][n1.getIndex()] >= 0) {
                                    zeroOneDone = true;
                                    dummy.SetRoute(i, newRoute3);
                                    dummy.SetRoute(j, newRoute4);
                                    li = new LocalImprovement(dummy, 7, problem, true);
                                    dummy = li.impmrove();

                                    int n1SuccessorIndex = newRoute4.getRoute().indexOf(n1) + 1;
                                    int n1PredecessorIndex = newRoute4.getRoute().indexOf(n1) - 1;

                                    double searchHistValue = searchHistory[n1.getIndex()][n1SuccessorIndex].getSearchHistoryValue() + searchHistory[n1PredecessorIndex][n1.getIndex()].getSearchHistoryValue();

                                    double delta = (1d / (dummy.getSolutionValue() + penalty));
                                    if (useSH) {
                                        delta += searchHistValue;
                                    }
                                    if (Math.abs(dummy.getSolutionValue() - neighborSolution.getSolutionValue()) > kMax) {
                                        kMax = Math.abs(dummy.getSolutionValue() - neighborSolution.getSolutionValue());
                                    }


                                    options.add(new Alternative(delta, dummy, n1, null, i, j, "0-1"));

                                }
                                //ASPIRATIONSKRITERIUM
                                else if (dummy.getSolutionValue() < bestSolution.getSolutionValue()) {
                                    options.add(new Alternative(1000000d / (dummy.getSolutionValue() + penalty), dummy, n1, null, i, j, "0-1"));
                                }
                            }
                        }
                    }
                }
            }
            Collections.sort(options);

            boolean flag = true;
            while (flag) {
                int max = Math.min(options.size(), 1);
                //Auslesen der besten Werte für zeta. Maximal 5 Werte
                double sum = 0;
                for (int i = 0; i < max; i++) {
                    sum += options.get(i).getDelta();
                }
                for (int i = 0; i < max; i++) {
                    double proba = options.get(i).getDelta() / sum;
                    if (i == 0) {
                        options.get(i).setProbability(proba);
                        continue;
                    }
                    options.get(i).setProbability(proba + options.get(i - 1).getProbability());
                }
                //Ermittlung einer zufälligen Kombination

                double monteCarlo = Math.random();
                double zetaprob;
                Alternative candidate = null;
                int candidateID = 0;
                for (int i = 0; i < options.size(); i++) {

                    zetaprob = options.get(i).getProbability();
                    if (zetaprob > monteCarlo) {
                        candidate = options.get(i);
                        candidateID = i;
                        break;
                    }
                }

                if (candidate != null) {
                    //FALLS DER AUSGEWÄHLTE TAUSCHZUG AUS EINEM 1 1 MOVE RESULTIERT
                    if (candidate.type.equals("1-1")) {
                        //falls kandidat nicht tabu ist --> Nachbarschaftslösung aktualisieren
                        if (tabuList[candidate.node2OldRouteIndex][candidate.node1.getIndex()] >= 0 && tabuList[candidate.node1OldRouteIndex][candidate.node2.getIndex()] >= 0) {
                            neighborSolution = candidate.getSolution();
                            tabuList[candidate.node2OldRouteIndex][candidate.node2.getIndex()] = tabuDuration;
                            tabuList[candidate.node1OldRouteIndex][candidate.node1.getIndex()] = tabuDuration;
                            frequency[candidate.node1.getIndex()]++;
                            frequency[candidate.node2.getIndex()]++;
                            flag = false;
//                            System.out.println("TAUSCH: Kunde " +candidate.node1.getIndex() +" aus Route " + candidate.node1OldRouteIndex +  "TAUSCH: Kunde " +candidate.node2.getIndex() +  " aus Route " + candidate.node2OldRouteIndex);
                        } else {
//                            System.out.println("TABUZUG: Kunde  " + candidate.node1.getIndex()+ " zurück in Route " + candidate.node2OldRouteIndex + " dauer: " + tabuList[candidate.node2OldRouteIndex][candidate.node1.getIndex()]);
//                            System.out.println("oder TABUZUG: Kunde  " + candidate.node2.getIndex()+ " zurück in Route " + candidate.node1OldRouteIndex + " dauer: " + tabuList[candidate.node1OldRouteIndex][candidate.node2.getIndex()]);
                            //ASPIRATIONSKRITERIUM
                            if (candidate.s.getSolutionValue() < bestSolution.getSolutionValue()) {
                                neighborSolution = candidate.getSolution();
                                tabuList[candidate.node2OldRouteIndex][candidate.node2.getIndex()] = tabuDuration;

                                tabuList[candidate.node1OldRouteIndex][candidate.node1.getIndex()] = tabuDuration;
                                bestSolution = neighborSolution;
                                frequency[candidate.node1.getIndex()]++;
                                frequency[candidate.node2.getIndex()]++;
                                flag = false;
                            } else {
                                options.remove(candidateID);
                            }
                        }
                        if (bestSolution.getSolutionValue() > neighborSolution.getSolutionValue()) {
                            bestSolution = neighborSolution;
                            System.out.println(it + " in ZUG 1 1 ");
                        }
                    }


                    //FALLS DER AUSGEWÄHLTE TAUSCHZUG AUS EINEM 0 1 MOVE RESULTIERT
                    else if (candidate.type.equals("0-1")) {
                        //falls kandidat nicht tabu ist --> Nachbarschaftslösung aktualisieren
                        if (tabuList[candidate.node2OldRouteIndex][candidate.node1.getIndex()] >= 0) {
                            neighborSolution = candidate.getSolution();
                            tabuList[candidate.node1OldRouteIndex][candidate.node1.getIndex()] = tabuDuration;
                            frequency[candidate.node1.getIndex()]++;
//                            System.out.println("TAUSCH: Kunde " + candidate.node1.getIndex() +" aus Route " + candidate.node1OldRouteIndex);
                            flag = false;
                        } else {
//                            System.out.println("TABUZUG: Kunde  " + candidate.node1.getIndex()+ " zurück in Route " + candidate.node2OldRouteIndex + " dauer: " + tabuList[candidate.node2OldRouteIndex][candidate.node1.getIndex()]);
                            //ASPIRATIONSKRITERIUM
                            if (candidate.s.getSolutionValue() < bestSolution.getSolutionValue()) {
                                neighborSolution = candidate.getSolution();
                                tabuList[candidate.node1OldRouteIndex][candidate.node1.getIndex()] = tabuDuration;
                                bestSolution = neighborSolution;
                                frequency[candidate.node1.getIndex()]++;
                                flag = false;
                            } else {
                                options.remove(candidateID);
                            }
                        }
                        if (bestSolution.getSolutionValue() > neighborSolution.getSolutionValue()) {
                            bestSolution = neighborSolution;
                            System.out.println(it + " in ZUG 0 1 ");

                        }
                    }
                } else {
                    flag = false;
                }

            }
//            Draw.drawRoutes(neighborSolution.getRoutes(),"TS"+it, neighborSolution.getSolutionValue());
            //nachdem neue NBL erzeugt wurde: Tabuliste aktualisieren
            for (int i = 0; i < tabuList.length; i++) {
                for (int j = 0; j < tabuList.length; j++) {
                    tabuList[i][j] = tabuList[i][j] + 1;
                }
            }
        }
        return bestSolution;
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

            for (int miehleIteration = 0; miehleIteration < 10; miehleIteration++) {
                double zähler = 0;
                double xnenner = 0;
                double ynenner = 0;
                for (Node n : route.getRoute()) {
                    xnenner += (n.getDemand() * n.getxPos()) / (Math.pow(Math.pow(xpos - n.getxPos(), 2) + Math.pow(ypos - n.getxPos(), 2), 0.5));
                    ynenner += (n.getDemand() * n.getyPos()) / (Math.pow(Math.pow(xpos - n.getxPos(), 2) + Math.pow(ypos - n.getxPos(), 2), 0.5));
                    zähler += (n.getDemand()) / (Math.pow(Math.pow(xpos - n.getxPos(), 2) + Math.pow(ypos - n.getxPos(), 2), 0.5));
                }
                xpos = xnenner / zähler;
                ypos = ynenner / zähler;
            }


            Node dummy = new Node(counter, (int) xpos, (int) ypos, 0);
            centersOfGravity.add(dummy);
            counter++;
        }
        Draw.drawRoutesWithCOG(solution, "COGS", centersOfGravity);
        return centersOfGravity;
    }

    public class Subproblems extends ArrayList<Node> implements Comparable<Subproblems> {
        private ArrayList<Node> customers = new ArrayList<>();
        private double angleToZero;
        private int index;

        public Subproblems(ArrayList<Node> customers, double angleToZero) {
            this.customers.addAll(customers);
            this.angleToZero = angleToZero;
        }

        public Subproblems(double angleToZero) {
            this.angleToZero = angleToZero;
        }

        @Override
        public int compareTo(Subproblems o) {
            if (this.angleToZero > o.getAngleToZero()) {
                return 1;
            } else if (this.angleToZero < o.getAngleToZero()) {
                return -1;
            }
            return 0;
        }

        @Override
        public boolean add(Node n) {
            return customers.add(n);
        }

        @Override
        public boolean addAll(Collection<? extends Node> c) {
            return customers.addAll(c);
        }

        public boolean addAllexceptDepot(ArrayList<Node> c) {
            for (int i = 1; i < c.size() - 1; i++) {
                customers.add(c.get(i));
            }
            return true;
        }

        public double getAngleToZero() {
            return angleToZero;
        }

        public ArrayList<Node> getRoutes() {
            return customers;
        }

        public void setIndex(int i) {
            this.index = i;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public int size() {
            return customers.size();
        }

        @Override
        public String toString() {
            return "" + angleToZero + customers.toString();
        }

        @Override
        public Node get(int index) {
            return customers.get(index);
        }

        @Override
        public void add(int index, Node element) {
            customers.add(index, element);
        }

    }


    private ArrayList<Subproblems> sweep(int numOfSubProblems, ArrayList<Node> cog, Solution solution, double start) {
        ArrayList<Subproblems> subProblems = new ArrayList<>();
        ArrayList<Subproblems> cogs = new ArrayList<>();


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
            alpha = alpha + start;
            //Hilfsklasse mit Winkel zw sweep Start und Center of gravity + den zugehörigen Kunden zum CoG
            cogs.add(new Subproblems(solution.getRoutes().get(counter).getRoute(), alpha));
            counter++;

        }
        Collections.sort(cogs);

        for (int i = 0; i < cogs.size(); i++) {
            cogs.get(i).setIndex(i);
        }

        int routesPerSubProblem = (cogs.size() / numOfSubProblems);

        //wenn zu viele Routen pro subproblem --> gesamtproblem ausgeben
        if (routesPerSubProblem == 0) {
            for (Subproblems s : cogs) {
                subProblems.add(s);
            }
            return subProblems;
        }

        int n_extra = cogs.size() - numOfSubProblems * routesPerSubProblem;

        int noOfSubset = 1;
        int cogsInSubset = 0;
        Subproblems sP = new Subproblems(0);
        for (Subproblems s : cogs) {
            int cogsToAdd = routesPerSubProblem;
            if (noOfSubset <= n_extra) {
                cogsToAdd = routesPerSubProblem + 1;
            }
            if (cogsInSubset < cogsToAdd) {
                sP.addAll(s.getRoutes());
                cogsInSubset++;
            }
            if (cogsInSubset == cogsToAdd) {
                for (int i = 1; i < sP.customers.size(); i++) {
                    Node n = sP.customers.get(i);
                    if (n.getIndex() == 0) {
                        sP.customers.remove(i);
                        i--;
                    }
                }
                sP.angleToZero = s.angleToZero;
                subProblems.add(sP);
                sP = new Subproblems(s.angleToZero);
                noOfSubset++;
                cogsInSubset = 0;
            }
        }
        Collections.sort(subProblems);
        return subProblems;

    }
}
