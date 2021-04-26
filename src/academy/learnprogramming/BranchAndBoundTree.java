package academy.learnprogramming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BranchAndBoundTree extends Heuristiken {

    //Upperbound von Konstruktionsheuristik
    private double upperBound;
    private BranchAndBoundNode root;
    double bestSolutionValue = Integer.MAX_VALUE;
    BranchAndBoundNode bestNode;

    public BranchAndBoundTree(Problem problem, double upperBound) {
        super(problem);
        this.upperBound = upperBound;
        this.root = createRoot();
    }

    private BranchAndBoundNode createRoot(){
        ArrayList<Integer> refArray = new ArrayList<>();
        //lieferstatus der Kunden wieder auf false setzen, weil dieser von eröffnungsheuristik noch auf true gestellt sein kann
        problem.resetNodes();
        for (int i=0;i<super.problem.getNodes().length;i++){
            refArray.add(i);
        }
        //Instanzieren der Baumwurzel: erstellen einer ersten Route, die im Depot startet
        Route firstRoute = new Route(0, super.problem.getVehicleCapacity());
        firstRoute.addCustomer(Problem.nodes[0]);
        //Node mit index=0 kann mehrfach in Baum eingefügt werden
        return new BranchAndBoundNode(refArray,0,Problem.nodes[0].getxPos(),Problem.nodes[0].getyPos(),firstRoute,Problem.nodes[0].getDemand(),null);
    }


    //äquivalent zu traverse
    @Override
    public Solution solve() {
        Solution solution = new Solution("BranchAndBound");
        solution.routes = this.bestNode.finishedRoutes;
        solution.setSolutionValue(this.bestSolutionValue);
        return solution;
    }

    public void traverse(BranchAndBoundNode root){

        BranchAndBoundNode bestNode;
        if (root == null){
            root = this.root;
        }
        if (!root.getChildren().isEmpty()){
            for (Integer key:root.getChildren().keySet()){
                traverse(root.getChildren().get(key));
            }
        }
        //nur noch depot als Nachfolger bedeutet alle Kunden zugeordnet

        if (root.isFinished()){
            if (root.calculateSolutionValue() < bestSolutionValue){
                bestSolutionValue = root.getSolutionValue();
                bestNode = root;
                this.bestNode =bestNode;
            }
        }

    }

    //rekursive Funktion, die den gesamten Baum aufstellt und wurzel ausgibt
    public BranchAndBoundNode createChildren(BranchAndBoundNode root){
        //layer 1 erstellen
        if (root == this.root){
            root.createSubNodes();
            Map<Integer,BranchAndBoundNode> layer1 = root.getChildren();
            for (Integer key:layer1.keySet()){
                createChildren(layer1.get(key));
            }
        } else if (root != null){
            Map<Integer,BranchAndBoundNode> subNodes = root.createSubNodes();
            for(Integer key: subNodes.keySet()) {
                createChildren(subNodes.get(key));
            }
        }
        return this.root;
    }

    public BranchAndBoundNode getRoot() {
        return root;
    }

    public BranchAndBoundNode getBestNode() {
        return bestNode;
    }
}
