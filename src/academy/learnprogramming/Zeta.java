package academy.learnprogramming;

public class Zeta implements Comparable<Zeta>{
    double value;
    Node customer1;
    Node customer2;
    double probability;

    public Zeta(double value, Node customer1, Node customer2) {
        this.value = value;
        this.customer1 = customer1;
        this.customer2 = customer2;
    }

    @Override
    public int compareTo(Zeta o) {
        if (this.getValue() < o.getValue()){
            return 1;
        }else if (this.getValue() > o.getValue()){
            return -1;
        }
        return 0;
    }

    public double getValue() {
        return value;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public double getProbability() {
        return probability;
    }

    public Node getCustomer1() {
        return customer1;
    }

    public Node getCustomer2() {
        return customer2;
    }

    @Override
    public String toString() {
        return "Customer " + customer1.getIndex() + " combined with Customer " + customer2.getIndex() + " is a saving of " + value;
    }

    public boolean contains (Node n){
        return (n.getIndex() == customer1.getIndex() || n.getIndex() == customer2.getIndex());
    }
}
