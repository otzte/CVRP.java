package academy.learnprogramming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClarkeWrightSavings extends Heuristiken{

    public ClarkeWrightSavings(Problem problem) {
        super(problem);
    }

    @Override
    public Solution solve() {
        // für jeden Kunden i [0,i,0] erstellen
        ArrayList<Route> routes = new ArrayList<>();
        for (int i = 1; i<super.problem.getNodes().length; i++){
            Route singleTrip = new Route(i-1,super.problem.getVehicleCapacity());
            singleTrip.addCustomer(super.problem.getNodes()[0]);
            Node node = super.problem.getNodes()[i];
            singleTrip.addCustomer(node);
            singleTrip.addCustomer(super.problem.getNodes()[0]);
            routes.add(singleTrip);
        }

        while (true) {
            //Savings kalkulieren
            ArrayList<Route.Saving> candidates = new ArrayList<>();
            for (int i = 0; i < routes.size() - 1; i++) {
                Route thisRoute = routes.get(i);
                for (int j = i + 1; j < routes.size(); j++) {
                    Route concatPartner = routes.get(j);
                    Route.Saving savingRoute = thisRoute.calculateSaving(super.problem.getDistances(), concatPartner);


                    if (savingRoute != null) {
                        candidates.add(savingRoute);
                    }
                }
            }
            Collections.sort(candidates);
            //besten Kandidaten hinzufügen
            if (!candidates.isEmpty()) {
                routes.add(candidates.get(0).getRoute());
//                System.out.println("Saving: " + candidates.get(0).getSaving());
            } else {
                break;
            }
            //dafür die zwei Teilkomponenten herausnehmen
            for (Node nodesInNewRoute : candidates.get(0).getRoute().getRoute()) {
                Route newRoute = candidates.get(0).getRoute();
                if (nodesInNewRoute.getIndex() == 0) {
                    continue;
                }
                for (int i = 0; i < routes.size(); i++) {
                    Route oldRoute = routes.get(i);
                    if (oldRoute.contains(nodesInNewRoute.getIndex())) {
                        // Neu entstandene Route soll nicht wieder entfernt werden
                        if (oldRoute.equals(newRoute)) {
                            continue;
                        }
                        routes.remove(i);
                        i--;
                    }
                }
            }
        }

        return new Solution("ClarkeWrightSavings",routes);

    }



    public Solution solveForTabuSearch(ArrayList<Node> customers) {
        // für jeden Kunden i [0,i,0] erstellen
        ArrayList<Route> routes = new ArrayList<>();
        for (int i = 1; i<customers.size(); i++){
            Route singleTrip = new Route(i-1,super.problem.getVehicleCapacity());
            singleTrip.addCustomer(customers.get(0));
            Node node = customers.get(i);
            singleTrip.addCustomer(node);
            singleTrip.addCustomer(customers.get(0));
            routes.add(singleTrip);
        }

        while (true) {
            //Savings kalkulieren
            ArrayList<Route.Saving> candidates = new ArrayList<>();
            for (int i = 0; i < routes.size() - 1; i++) {
                Route thisRoute = routes.get(i);
                for (int j = i + 1; j < routes.size(); j++) {
                    Route concatPartner = routes.get(j);
                    Route.Saving savingRoute = thisRoute.calculateSaving(super.problem.getDistances(), concatPartner);
                    if (savingRoute != null) {
                        candidates.add(savingRoute);
                    }
                }
            }
            Collections.sort(candidates);
            //besten Kandidaten hinzufügen
            if (!candidates.isEmpty()) {
                routes.add(candidates.get(0).getRoute());
//                System.out.println("Saving: " + candidates.get(0).getSaving());
            } else {
                break;
            }
            //dafür die zwei Teilkomponenten herausnehmen
            for (Node nodesInNewRoute : candidates.get(0).getRoute().getRoute()) {
                Route newRoute = candidates.get(0).getRoute();
                if (nodesInNewRoute.getIndex() == 0) {
                    continue;
                }
                for (int i = 0; i < routes.size(); i++) {
                    Route oldRoute = routes.get(i);
                    if (oldRoute.contains(nodesInNewRoute.getIndex())) {
                        // Neu entstandene Route soll nicht wieder entfernt werden
                        if (oldRoute.equals(newRoute)) {
                            continue;
                        }
                        routes.remove(i);
                        i--;
                    }
                }
            }
        }

        return new Solution("ClarkeWrightSavings",routes);

    }


}
