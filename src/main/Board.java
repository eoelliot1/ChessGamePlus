package main;

import java.awt.*;

public class Board {

    final int MAX_COL = 8;
    final int MAX_ROW = 8;
    public static final int SQUARE_SIZE = 100; //MAKE SURE THAT YOUR CHEST PIECE IMAGES ARE ALSO 100 by 100 pixels each.
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE/2;

    /*
    Responsible for drawing the main.Board.
     */
    public void draw(Graphics2D g2) {

        // White = yes. Blurple = false.
        boolean checkeredColours = false;

        for(int row = 0; row < MAX_ROW; row++) {
            for (int colum = 0; colum < MAX_COL; colum++) {

                // Functionality to set up Checkered colours
                if (!checkeredColours) {
                    g2.setColor(new Color(143,50,62));
                    checkeredColours = true;
                } else {
                    g2.setColor(new Color(228,212,255));
                    checkeredColours = false;
                }
                g2.fillRect(colum*SQUARE_SIZE, row*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
            checkeredColours = !checkeredColours;

    }
}
}
