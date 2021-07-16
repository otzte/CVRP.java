package academy.learnprogramming;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Main {


    //Instanzen werden bei den zwei Metaheuristiken (TS und SbAS) über die solveMultipleTimes - Funktion gelöst
    //und Werte in XL-Tabelle eingetragen
    public static void main(String[] args) throws IOException {
        int numberOfCustomers = 200;
        int numberOfVehicles = 10;
        int vehicleCapacity = 90;
        int depotXPos = 50;
        int depotYPos = 50;
        int seed = 121212;


        //Create Problem Instance
        Problem CVRP = new Problem(numberOfCustomers, numberOfVehicles, vehicleCapacity, depotXPos, depotYPos, seed);
        ArrayList<Node> customers = new ArrayList<>(Arrays.asList(CVRP.getNodes()));
        customers.remove(0);


        //NNH
        NearestNeighborHeuristik NNH = new NearestNeighborHeuristik(CVRP);

        Solution solution = NNH.solve();

        System.out.println("\n\n" + "\t Routen: ");
        for (Route route : solution.routes) {
            System.out.println("\n");
            for (Node node : route.getRoute()) {
                System.out.print(node.getIndex() + " - ");
            }
        }
        System.out.println("\n \t Zielfunktionswert: " + solution.getSolutionValue());

        Draw.drawRoutes(solution.routes, "Nearest Neighbor Heuristik", solution.getSolutionValue());


        //SAVINGS

        Problem CVRP2 = new Problem(numberOfCustomers, numberOfVehicles, vehicleCapacity, depotXPos, depotYPos, seed);
        ClarkeWrightSavings savings = new ClarkeWrightSavings(CVRP2);
        double startTime = System.currentTimeMillis();
        Solution savingsSolution = savings.solve();
        LocalImprovement localImprovement = new LocalImprovement(solution, 45, CVRP);
//		localImprovement.impmrove();
        System.out.println("Savings Heuristik Lösung:" + savingsSolution.getSolutionValue() + " nach " + (System.currentTimeMillis() - startTime));

        Draw.drawRoutes(savingsSolution.routes, "Clarke Wright Savings", savingsSolution.getSolutionValue());


        //DURCHSCHNITTLICHE KANTENLÄNGE SAVINGS HEURISTIK FÜR THRESHOLD T
        int noOfUsedEdges = 0;
        for (Route r : savingsSolution.getRoutes()) {
            for (Node n : r.getRoute()) {
                noOfUsedEdges++;
            }
            noOfUsedEdges--;
        }
        double averageDistance = 16 + savingsSolution.getSolutionValue() / noOfUsedEdges;




        //--------------------------------SBAS--------------------------------


        Solution best = savingsSolution;

        SbAS sbAS = new SbAS(CVRP2, (double) 1 / savingsSolution.getRoutes().size(), savingsSolution.getSolutionValue(), averageDistance);
        //Die Instanz werden mit dem divide and conquer framework über diese funktion mehrfach gelöst
        solveMultipleTimes("SbAS", sbAS, seed, savingsSolution, CVRP2);

        //--------------------------------SBAS END--------------------------------


        //---------------------TABU SEARCH--------------------------------
        //Parameter des TSDA
        Random random = new Random();
        double[][] data = new double[1001][10];
        for (int i = 0; i < 1; i++) {
            double t = 0.3; //0.01 + Math.random() * 2;
            data[i][0] = t;
            double divFac = 20; // Math.random() * 100;
            data[i][1] = divFac;
            int noOfTabuIterations = 20; // (int) (10 + Math.random() * 140);
            data[i][2] = noOfTabuIterations;
            int noOfGlobalACOAIterations = 5; // (int) (1 + Math.random() * 5);
            data[i][3] = noOfGlobalACOAIterations;
            int noOfSubACOAIterations = 6;//(int) (1 + Math.random() * 9);
            data[i][4] = noOfSubACOAIterations;
            int cakePieces = 4;//(int) (3 + Math.random() * 3); //bestimmt die größe der n_r,s,10 Subprobleme
            data[i][5] = cakePieces;
            double randomAnt = 1; // Math.random();
            data[i][6] = randomAnt;
            double sweepAngle = -0.15; // Math.random()*2-1; // random Sweep oder konstanter Startwinkel: <0 - random, >0 - Range zwischen 5° und 150°
            data[i][7] = sweepAngle;
            int bnbUsage = 0; // random.nextInt(3); // 0 - kein BnB, 1  - im Dekompositionsansatz, 2 - in der Tabususche
            data[i][8] = bnbUsage;

            TabuSearchApproach ts = new TabuSearchApproach(CVRP2, savingsSolution, t, divFac, noOfTabuIterations, noOfGlobalACOAIterations,
                    noOfSubACOAIterations, cakePieces, randomAnt > 0.5, sweepAngle, bnbUsage, averageDistance);


            //Die Instanz werden mit dem tsda über diese funktion mehrfach gelöst
//            solveMultipleTimes("TS", ts, seed, savingsSolution, CVRP2);
        }

//		for (int i=0;i<data.length;i++){
//			for (int j=0;j<data[i].length;j++){
//				System.out.println(data[i][j]);
//			}
//		}
//
        //Tracking parameter quality

//		XSSFWorkbook workbook = new XSSFWorkbook();
//		XSSFSheet sheet = workbook.createSheet(Integer.toString(seed));
//
//		int rows = data.length;
//		int cols = data[0].length;
//		XSSFRow header = sheet.createRow(0);
//		XSSFCell headerCell = header.createCell(0);
//		headerCell.setCellValue("tabuduration");
//		headerCell = header.createCell(1);
//		headerCell.setCellValue("divFac");
//		headerCell = header.createCell(2);
//		headerCell.setCellValue("NoOfTabuIterations");
//		headerCell = header.createCell(3);
//		headerCell.setCellValue("NoOfGlobalACOAIterations");
//		headerCell = header.createCell(4);
//		headerCell.setCellValue("NoOfSubSolACOAIterations");
//		headerCell = header.createCell(5);
//		headerCell.setCellValue("CakePieces");
//		headerCell = header.createCell(6);
//		headerCell.setCellValue("randomAnt");
//		headerCell = header.createCell(7);
//		headerCell.setCellValue("SweepAngle");
//		headerCell = header.createCell(8);
//		headerCell.setCellValue("bnbUsage");
//		headerCell = header.createCell(9);
//		headerCell.setCellValue("SolutionValue");
//
//		for (int r=1; r<rows;r++){
//			XSSFRow row = sheet.createRow(r);
//			for(int c=0;c<cols;c++){
//				XSSFCell cell = row.createCell(c);
//				double value = data[r-1][c];
//				cell.setCellValue(value);
//			}
//		}
//
//		String filePath = ".\\Datafiles\\TabuSearchData.xlsx";
//		FileOutputStream outStream = new FileOutputStream(filePath);
//		workbook.write(outStream);
//		outStream.close();
//		--------------------------------------TABUSEARCH END -------------------------


    }

    public static void solveMultipleTimes(String name, Heuristiken h, int seed, Solution savingsSolution, Problem CVRP2) throws IOException {
        XSSFWorkbook workbookSbas = new XSSFWorkbook();
        XSSFSheet sheetSbas = workbookSbas.createSheet(Integer.toString(seed));

        Solution best = savingsSolution;
        int lastRw = 0;
        for (int i = 0; i < 10; i++) {
            if (h instanceof SbAS) {
                ((SbAS) h).resetPheromone();
            }

            Solution sol = h.solve();
            if (sol.getSolutionValue() < best.getSolutionValue()) {
                best = sol;
            }

            int cols = 2;


            outerLoop:
            for (int r = 0; r < 30; r++) {
                XSSFRow row = sheetSbas.createRow(r + lastRw);
                for (int c = 0; c < cols; c++) {
                    XSSFCell cell = row.createCell(c);
                    double value = h.XLData[r][c];
                    if (value == 0) {
                        lastRw = lastRw + 1 + r;
                        break outerLoop;
                    }
                    cell.setCellValue(value);
                }
            }

            String filePath = ".\\Datafiles\\" + name + ".xlsx";
            FileOutputStream outStream = new FileOutputStream(filePath);
            workbookSbas.write(outStream);
            outStream.close();
        }
        Draw.drawRoutes(best.getRoutes(), name, best.getSolutionValue());
    }
}
