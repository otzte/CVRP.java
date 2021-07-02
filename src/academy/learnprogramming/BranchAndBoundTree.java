package academy.learnprogramming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BranchAndBoundTree extends Heuristiken {

    //Upperbound von Konstruktionsheuristik
    private double upperBound;
    ArrayList<Node> customers; // alle Kunden eine Route (alle nodes außer 0)
    private BranchAndBoundNode root;
    static double bestSolutionValue = Integer.MAX_VALUE;
    BranchAndBoundNode bestNode;
    Solution nativeSolution;

    public BranchAndBoundTree(Problem problem, double upperBound, ArrayList<Node> customers) {
        super(problem);
        this.customers = customers;
        this.upperBound = upperBound;
        this.root = createRoot();
    }

    public BranchAndBoundTree(Problem problem, double upperBound, ArrayList<Node> customers, Solution sol) {
        super(problem);
        this.customers = customers;
        this.upperBound = upperBound;
        this.root = createRoot();
        nativeSolution = sol;

    }


    private BranchAndBoundNode createRoot(){
        Route route = new Route(0, super.problem.getVehicleCapacity());
        route.addCustomer(Problem.nodes[0]);
        //Node mit index=0 kann mehrfach in Baum eingefügt werden
        BranchAndBoundNode.UB = upperBound;
        bestSolutionValue = Integer.MAX_VALUE;
        return new BranchAndBoundNode(customers, problem.depotXPos, problem.depotYPos, route);
    }


    //äquivalent zu traverse
    @Override
    public Solution solve() {
        Solution solution = new Solution("BranchAndBound");
        ArrayList<Route> rs = new ArrayList<>();
            if (this.bestNode!=null) {
                rs.add(this.bestNode.fixatedRoute);
                solution.routes = rs;
                solution.calcSolutionValue();
                return solution;
            }
        System.out.println("Branch and Bound Error");
            return nativeSolution;
    }

    public void traverse(BranchAndBoundNode root){
        BranchAndBoundNode bestNode;
        if (root == null){
            root = this.root;
        }
        if (!root.availableSuccessors.isEmpty()){
            ArrayList<BranchAndBoundNode> subNodes = root.createSubNodes();
            Collections.sort(subNodes);
            for (BranchAndBoundNode n: subNodes){
                traverse(n);
            }
        }
        //nur noch depot als Nachfolger bedeutet alle Kunden zugeordnet

        if (root.isFinished()){
            root.fixatedRoute.addCustomer(problem.getNodes()[0]);
            if (root.calculateSolutionValue() < bestSolutionValue){
                BranchAndBoundNode.UB = root.calculateSolutionValue();
                bestSolutionValue = root.getSolutionValue();
                bestNode = root;
                this.bestNode = bestNode;
            }
        }

    }



    public BranchAndBoundNode getRoot() {
        return root;
    }

    public BranchAndBoundNode getBestNode() {
        return bestNode;
    }
}
