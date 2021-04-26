package academy.learnprogramming;

public class Node {
    private final int index;
    private final int xPos;
    private final int yPos;
    private final int demand;
    private boolean served = false;
    boolean isDepot = false;

    //Constructor für Kunden
    public Node(int index, int xPos, int yPos, int demand) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.demand = demand;
        this.index = index;
    }
    //Constructor für das Depot
    public Node(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.demand = 0;
        this.isDepot = true;
        this.index = 0;
        // served auf true setzen weil keine offene Nachfrage vorhanden
        this.served = true;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public int getDemand() {
        return demand;
    }

    public boolean isServed() {
        return served;
    }

    public int getIndex() {
        return index;
    }

    public void makeDelivery(){
        this.served = true;
    }

    public void undoDelivery(){
        this.served = false;
    }

    @Override
    public String toString() {
        return "id: " + index;
    }

    public boolean sameSpot(Object obj) {
        if (!(obj instanceof Node)){
            return false;
        }
        if (this.getxPos() == ((Node) obj).getxPos() && this.getyPos() == ((Node) obj).getyPos()){
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)){
            return false;
        }
        if (((Node) obj).getIndex()==this.getIndex()){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode() + 57;
    }
}
