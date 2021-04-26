package academy.learnprogramming;

public abstract class Heuristiken {
    public final Problem problem;

    public Heuristiken(Problem problem) {
        this.problem = problem;
    }

    //Solution erzeugen
    public abstract Solution solve();
}
