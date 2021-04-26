package academy.learnprogramming;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//quelle hier


    public class Draw {

        public static void  drawRoutes(ArrayList<Route> s , String fileName, double zfw) {

            int VRP_Y = 900;
            int VRP_INFO = 300;
            int X_GAP = 600;
            int margin = 30;
            int marginNode = 1;


            int width = VRP_INFO + X_GAP;
            int height = VRP_Y;


            BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = output.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.BLACK);
            g.fillRect(2, 32,6,6);
            g.drawString(fileName,10,40);


            //Grenzen festlegen
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;

            for (int k = 0; k < s.size(); k++) {
                Route route = s.get(k);
                ArrayList<Node> r = route.getRoute();
                for (int i = 0; i < r.size(); i++) {
                    Node n = r.get(i);
                    if (n.getxPos() > maxX) maxX = n.getxPos();
                    if (n.getxPos() < minX) minX = n.getxPos();
                    if (n.getyPos() > maxY) maxY = n.getyPos();
                    if (n.getyPos() < minY) minY = n.getyPos();

                }
            }



            int mX = width - 2 * margin;
            int mY = VRP_Y - 2 * margin;


            int A, B;
            if ((maxX - minX) > (maxY - minY))
            //Distanzen innerhalb verfügbarer/vordefinierten Pixelgrenzen skalieren
                // wenn x distanz gr
            {
                A = mX;
                B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
                if (B > mY)
                {
                    B = mY;
                    A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
                }
            }
            else
            {
                B = mY;
                A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
                if (A > mX)
                {
                    A = mX;
                    B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
                }
            }

            // Draw Route
            for (int i = 0; i < s.size() ; i++)
            {
                ArrayList<Node> route = s.get(i).getRoute();
                for (int j = 1; j < route.size() ; j++) {
                    Node n;
                    n = route.get(j-1);

                    int ii1 = (int) ((double) (A) * ((n.getxPos() - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                    int jj1 = (int) ((double) (B) * (0.5 - (n.getyPos() - minY) / (maxY - minY)) + (double) mY / 2) + margin;

                    n = route.get(j);
                    int ii2 = (int) ((double) (A) * ((n.getxPos() - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                    int jj2 = (int) ((double) (B) * (0.5 - (n.getyPos() - minY) / (maxY - minY)) + (double) mY / 2) + margin;


                    g.drawLine(ii1, jj1, ii2, jj2);
                }
            }

            for (int i = 0; i < s.size() ; i++)
            {
                ArrayList<Node> route = s.get(i).getRoute();
                for (int j = 0; j < route.size() ; j++) {

                    Node n = route.get(j);

                    int ii = (int) ((double) (A) * ((n.getxPos()  - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                    int jj = (int) ((double) (B) * (0.5 - (n.getyPos() - minY) / (maxY - minY)) + (double) mY / 2) + margin;
                    if (i != 0) {
                        g.fillOval(ii - 3 * marginNode, jj - 3 * marginNode, 6 * marginNode, 6 * marginNode); //2244
                        String id = Integer.toString(n.getIndex());
                        g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode); //88
                    } else {
                        g.fillRect(ii - 3 * marginNode, jj - 3 * marginNode, 6 * marginNode, 6 * marginNode);  //4488
                        String id = Integer.toString(n.getIndex());
                        g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode); //88
                    }
                }

            }
            g.setColor(Color.red);


            g.fillRect(2, 52,6,6);
            g.drawString("Zielfunktionswert: " + zfw,10,60);

            g.setColor(Color.BLACK);


            fileName = fileName + ".png";
            File f = new File(fileName);
            try
            {
                ImageIO.write(output, "PNG", f);
            } catch (IOException ex) {
                //  Logger.getLogger(s.class.getName()).log(Level.SEVERE, null, ex);
            }

        }


        public static void  drawRoutesWithCOG(ArrayList<Route> s , String fileName, ArrayList<Node> cog) {

            int VRP_Y = 900;
            int VRP_INFO = 300;
            int X_GAP = 600;
            int margin = 30;
            int marginNode = 1;


            int width = VRP_INFO + X_GAP;
            int height = VRP_Y;


            BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = output.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.BLACK);
            g.fillRect(2, 32,6,6);
            g.drawString(fileName,10,40);


            //Grenzen festlegen
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;

            for (int k = 0; k < s.size(); k++) {
                Route route = s.get(k);
                ArrayList<Node> r = route.getRoute();
                for (int i = 0; i < r.size(); i++) {
                    Node n = r.get(i);
                    if (n.getxPos() > maxX) maxX = n.getxPos();
                    if (n.getxPos() < minX) minX = n.getxPos();
                    if (n.getyPos() > maxY) maxY = n.getyPos();
                    if (n.getyPos() < minY) minY = n.getyPos();

                }
            }



            int mX = width - 2 * margin;
            int mY = VRP_Y - 2 * margin;


            int A, B;
            if ((maxX - minX) > (maxY - minY))
            //Distanzen innerhalb verfügbarer/vordefinierten Pixelgrenzen skalieren
            // wenn x distanz gr
            {
                A = mX;
                B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
                if (B > mY)
                {
                    B = mY;
                    A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
                }
            }
            else
            {
                B = mY;
                A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
                if (A > mX)
                {
                    A = mX;
                    B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
                }
            }

            // Draw Route
            for (int i = 0; i < s.size() ; i++)
            {
                ArrayList<Node> route = s.get(i).getRoute();
                for (int j = 1; j < route.size() ; j++) {
                    Node n;
                    n = route.get(j-1);

                    int ii1 = (int) ((double) (A) * ((n.getxPos() - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                    int jj1 = (int) ((double) (B) * (0.5 - (n.getyPos() - minY) / (maxY - minY)) + (double) mY / 2) + margin;

                    n = route.get(j);
                    int ii2 = (int) ((double) (A) * ((n.getxPos() - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                    int jj2 = (int) ((double) (B) * (0.5 - (n.getyPos() - minY) / (maxY - minY)) + (double) mY / 2) + margin;


                    g.drawLine(ii1, jj1, ii2, jj2);
                }
            }
            g.setColor(Color.red);
            for (int j = 0;j<cog.size();j++){

                Node n = cog.get(j);
                int ii = (int) ((double) (A) * ((n.getxPos()  - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                int jj = (int) ((double) (B) * (0.5 - (n.getyPos() - minY) / (maxY - minY)) + (double) mY / 2) + margin;


                g.fillOval(ii - 5 * marginNode, jj - 5 * marginNode, 10 * marginNode, 10 * marginNode);
                String id = "COG of route " + j;
                g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode);
            }
            g.setColor(Color.black);



            for (int i = 0; i < s.size() ; i++)
            {
                ArrayList<Node> route = s.get(i).getRoute();
                for (int j = 0; j < route.size() ; j++) {

                    Node n = route.get(j);

                    int ii = (int) ((double) (A) * ((n.getxPos()  - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                    int jj = (int) ((double) (B) * (0.5 - (n.getyPos() - minY) / (maxY - minY)) + (double) mY / 2) + margin;
                    if (i != 0) {
                        g.fillOval(ii - 3 * marginNode, jj - 3 * marginNode, 6 * marginNode, 6 * marginNode); //2244
                        String id = Integer.toString(n.getIndex());
                        g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode); //88
                    } else {
                        g.fillRect(ii - 3 * marginNode, jj - 3 * marginNode, 6 * marginNode, 6 * marginNode);  //4488
                        String id = Integer.toString(n.getIndex());
                        g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode); //88
                    }
                }

            }
            g.setColor(Color.red);


            g.fillRect(2, 52,6,6);

            g.setColor(Color.BLACK);


            fileName = fileName + ".png";
            File f = new File(fileName);
            try
            {
                ImageIO.write(output, "PNG", f);
            } catch (IOException ex) {
                //  Logger.getLogger(s.class.getName()).log(Level.SEVERE, null, ex);
            }

        }



    }

