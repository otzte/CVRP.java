package academy.learnprogramming;

public abstract class Heuristiken {
    public final Problem problem;
    public double[][] XLData = new double[30][2];

    public Heuristiken(Problem problem) {
        this.problem = problem;
    }

    //Solution erzeugen
    public abstract Solution solve();

}
