package academy.learnprogramming;

public class Main {

    public static void main(String[] args) {
		int numberOfCustomers=200;
		int numberOfVehicles = 10;
		int vehicleCapacity =  90;
	    int depotXPos = 50;
		int depotYPos = 50;


		//Create Problem Instance
		Problem CVRP = new Problem(numberOfCustomers,numberOfVehicles,vehicleCapacity,depotXPos,depotYPos);
		NearestNeighborHeuristik NNH = new NearestNeighborHeuristik(CVRP);
		Solution solution =  NNH.solve();

		System.out.println("\n\n" + "\t Routen: ");
		for (Route route: solution.routes){
			System.out.println("\n");
			for(Node node: route.getRoute()){
				System.out.print(node.getIndex() + " - ");
			}
		}

		System.out.println("\n \t Zielfunktionswert: " + solution.getSolutionValue());

		Draw.drawRoutes(solution.routes, "Nearest Neighbor Heuristik",solution.getSolutionValue());


		//SAVINGS

		Problem CVRP2 = new Problem(numberOfCustomers,numberOfVehicles,vehicleCapacity,depotXPos,depotYPos);
		ClarkeWrightSavings savings = new ClarkeWrightSavings(CVRP2);
		Solution savingsSolution = savings.solve();
		Draw.drawRoutes(savingsSolution.routes, "Clarke Wright Savings",savingsSolution.getSolutionValue() );

		//SBAS
//		SbAS sbAS = new SbAS(CVRP2,(double) 1/savingsSolution.getRoutes().size(),savingsSolution.getSolutionValue());
//		Solution sbasS = sbAS.solve();
//		Draw.drawRoutes(sbasS.getRoutes(), "SbAS",sbasS.getSolutionValue() );

		//TABU SEARCH
		TabuSearchApproach ts = new TabuSearchApproach(CVRP2,savingsSolution);
		Solution tsSolution = ts.solve();
		Draw.drawRoutes(tsSolution.getRoutes(), "TS", tsSolution.getSolutionValue());

		//LOCAL IMPROVEMENT
		LocalImprovement LI = new LocalImprovement(solution,1000,CVRP2);
		Draw.drawRoutes(LI.impmrove().routes, "LI",LI.impmrove().calcSolutionValue());



		//BRANCH AND BOUND
		//Tree mit Root erstellen
//		BranchAndBoundTree searchTree = new BranchAndBoundTree(CVRP,solution.getSolutionValue());
//		BranchAndBoundNode root =  searchTree.createChildren(searchTree.getRoot());
//		searchTree.traverse(null);
//		Solution bnb = searchTree.solve();

//		Draw.drawRoutes(searchTree.getBestNode().getFinishedRoutes(), "Branch and Bound",searchTree.bestSolutionValue);





//
//		for (Node node: l2.fixatedRoute.getRoute()){
//			System.out.println(node.getIndex());
//		}



    }
}
