package main;

import piece.*;

import javax.swing.JPanel;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Game_Panel extends JPanel implements Runnable {

    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread; // https://www.w3schools.com/java/java_threads.asp
    Board board = new Board();
    Mouse mouse = new Mouse();

    //List of all the pieces
    public static ArrayList<Unit> units = new ArrayList<>();
    public static ArrayList<Unit> simUnits = new ArrayList<>();
    ArrayList<Unit> promoUnit = new ArrayList<Unit>();
    public static Unit castlingUnit;
    Unit activeUnit; //Stores what unit the player is holding.
    Unit checkingUnit;

    // Colours
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currColour = WHITE;
    int player1Region, player2Region;

    // Boolean conditions...
    boolean infect = false;
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver = false;

    boolean staleMate = false;

    public Game_Panel(int player1, int player2) {

        setBackground(Color.black);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        /*
        For now the game is called using the Classic format.
        In future, we can create different types of game formats relating to the stage.
         */


        //setUnitsClassic(); // - Old version
        setUnitsRegional(player1, player2);
        copyUnits(units, simUnits);
    }




    /*
Handles all the updates per turn.
*/
    private void update() {
        //System.out.println(activeUnit); //BugFixing

        if(promotion) {
            promoting();

        } // YOU CAN ADD STUFF HERE IF U WANT IT TO PROCESS BEFORE THE SIMULATION PHASE STARTS. PROMOTION IS A GOOD EXAMPLE
        else if(gameOver == false && staleMate == false) {
            //Mouse Button pressed
            if(mouse.pressed) {

                //True if player is not holding a unit
                if(activeUnit == null) {

                    for (Unit unit : simUnits) {
                        //If the player's mouse has the same colour, same row and, same column then the player's mouse is on this piece.
                        if(unit.colour == currColour &&
                                unit.column == mouse.x/Board.SQUARE_SIZE && unit.row == mouse.y/Board.SQUARE_SIZE) {
                            activeUnit = unit;
                            System.out.println(activeUnit + "is selected to move.");
                        }
                    }

                } else {//True if a player is already holding a unit.
                    simulate(); //Once the player picks up a unit, we will then simulate their next move. Any actions taken before this is hypothetical
                }
            }

            //IF MOUSE IS NOT BEING PRESSED.
            if(!mouse.pressed) {

                //System.out.println("Mouse released");
                if(activeUnit != null) {


                    if(validSquare) { //It's a valid position so update the position.
                        //Movement has been confirmed...
                        System.out.println("URow: " + activeUnit.row + " UColumn: " + activeUnit.column);
                        System.out.println("UPRow: " + activeUnit.pre_Row + " UPColumn: " + activeUnit.pre_Column);


                        copyUnits(simUnits, units); //If a piece has been removed then we apply to the backup list.
                        activeUnit.updatePosition();

                        if(castlingUnit != null) {
                            castlingUnit.updatePosition();
                        }


                        if (isKingInCheck() && isCheckmate()) {
                            gameOver = true; //GG
                            System.out.println("GameOver");
                        } else if (isStaleMate() && isKingInCheck() == false) {
                            //True if its stalemate. King needs to be in check for stalemate

                            staleMate = true;
                        }
                        else { //No gg
                            if(canPromote()) {
                                promotion = true;
                            } else {
                                infect = false;
                                changePlayer();
                            }
                        }
                        //WRITE CODE OVER HERE TO DESELECT THE UNIT IN THE SIMULATION PHASE.........................................................................

                        //activeUnit = null; //MY CODE: If u let go of your mouse when holding a unit, it will deselect unit.
                        //CANCELLED(BECAUSE THIS BROKE PROMOTION. IF ENABLED THEN U MUST DISABLE THIS CODE AT THE CHANGEPLAYER PART.
                    } else { //It's not a valid position so reset the position
                        copyUnits(units, simUnits); //A piece has been moved but the changes will be reverted for the original list
                        activeUnit.resetPosition();
                        activeUnit = null;

                        //WRITE CODE OVER HERE TO DESELECT THE UNIT IN THE SIMULATION PHASE.........................................................................

                    }

                    //activeUnit.updatePosition();
                    //activeUnit = null;
                }
            }

        }
    }

    /*
    - In turn based strategy games, they often have a waiting phase unlike real-time based strategy games.
    - This Chess game will have a waiting phase that we will simulate.
     */
    private void simulate() {
        canMove = false;
        validSquare = false;

        //Units get removed per loop.
        copyUnits(units, simUnits); //Restores removed units during simulation.
        //Remember that units are the actual units and simulation is how the units are when u making changes,
        // that could still get reverted cuz it might be invalid (p sure.)


        //CASTLING ------------------------------------------------
        //Fail-safe: reset castling unit's position per loop.
        if(castlingUnit != null) {
            castlingUnit.column = castlingUnit.pre_Column;
            castlingUnit.x = castlingUnit.getX(castlingUnit.column);
            castlingUnit = null;
        }
        //CASTLING ------------------------------------------------


        activeUnit.x = mouse.x -Board.HALF_SQUARE_SIZE;
        activeUnit.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activeUnit.column = activeUnit.getColumn(activeUnit.x);
        activeUnit.row = activeUnit.getRow(activeUnit.y);

        //System.out.println("1Row: " + activeUnit.pre_Row + " 1Column: " + activeUnit.pre_Column);

        //If statemenent to check if movement is valid or not.
        if(activeUnit.canMove(activeUnit.column, activeUnit.row)) {
            canMove = true;
            //System.out.println("2Row: " + activeUnit.pre_Row  + " 2Column: " + activeUnit.pre_Column);

            // Remove the unit that was hit from the list for simulation phase.
            if(activeUnit.hittingUnit != null) {
                    simUnits.remove(activeUnit.hittingUnit.getIndex());

                //Infect the unit instead of simply removing it if it is infected.
                if(isInfected(activeUnit) == true) {
                    //Infect the unit instead of simply removing it if it is infected.
                    infect = true;
                    int c = activeUnit.pre_Column;
                    int r = activeUnit.pre_Row;
                    simUnits.add(new Zombie(activeUnit.colour, c ,r));
                }
            }

            checkCastling();

            if(isIllegal(activeUnit) == false && opponentCanCaptureKing() == false) {
                validSquare = true;
            }
        }
    }

    /*
    Returns true if the unit is an infected type unit.
     */
    private boolean isInfected(Unit infected) {

        if(infected.getClass() == Zombie.class) {
            return true;
        }

        return false;
    }

    private boolean isIllegal(Unit protectedUnit) { //We use Unit instead of king so other units can use it.

        //His implementation:
         if(protectedUnit.getClass() == King.class) { //Here it asks for King but we can update this to more classes.
             for(Unit unit : simUnits) {
                 if(unit.colour != protectedUnit.colour && unit.canMove(protectedUnit.column, protectedUnit.row)) { //Add onto this if statement other units with protection alongside King.
                     return true;
                 }
             }
         }

         return false;
    }

    private boolean opponentCanCaptureKing() {
        Unit King = getKing(false);

        for(Unit unit : simUnits) {
            if(unit.colour != King.colour && unit.canMove(King.column, King.row)) {
                return true;
            }
        }

        return false;
    }

    /*
    Checks the king.
    If the King is in check then, return true.
    If the king is not in check, return false.
     */
    private boolean isKingInCheck() {
        Unit King = getKing(true);

        if(activeUnit.canMove(King.column, King.row)) {
            checkingUnit = activeUnit;
            return true;
        } else {
            checkingUnit = null;
        }

        return false;
    }

    /*
        Finds the king in simulated pieces and returns it.
        True if it returns opponents king
        False if it returns your king.
     */
    private Unit getKing(boolean opponent) {
        Unit king = null;

        for(Unit unit : simUnits) {
            if(opponent) {
                if(unit.getClass() == King.class && unit.colour != currColour) {
                    king = unit;
                }
            }
            else {
                if(unit.getClass() == King.class && unit.colour == currColour) {
                    king = unit;
                }
            }
        }
        return king;
    }


    private void checkCastling() {
        if (castlingUnit != null) {
            if(castlingUnit.column == 0) { //True if Left Rook
                castlingUnit.column += 3; //Move 3 squares to right
            } else if(castlingUnit.column == 7) { //True if Right Rook
                castlingUnit.column -= 2; //Move 2 squares to the left
            }
            castlingUnit.x = castlingUnit.getX(castlingUnit.column);
        }
    }

    private boolean isCheckmate() {

        Unit King25 = getKing(true);

        if(kingCanMove(King25)) {
            return false; //False if the king can move.
        } else { //King can't move, but we can still check if other Units can save the King.

            //Check the position of the unit that is checking and the King in check
            int columDiff = Math.abs(checkingUnit.column - King25.column);
            int rowDiff = Math.abs(checkingUnit.row - King25.row);

            if(columDiff == 0) {
                // The checking Unit is attacking vertically
                if(checkingUnit.row < King25.row) { //True if above the king.
                    for(int row = checkingUnit.row; row < King25.row; row++) { //See if a piece can fit inbetween King and Checking unit.
                        for(Unit unit : simUnits) {
                            if(unit != King25 && unit.colour != currColour && unit.canMove(checkingUnit.column, row)) {
                                return false; // An ally unit can attack/block the path so it is not checkMate.
                            }
                        }
                    }
                }
                if(checkingUnit.row > King25.row) { //True if below the king.
                    for(int row = checkingUnit.row; row > King25.row; row--) { //See if a piece can fit inbetween King and Checking unit.
                        for(Unit unit : simUnits) {
                            if(unit != King25 && unit.colour != currColour && unit.canMove(checkingUnit.column, row)) {
                                return false; // An ally unit can attack/block the path so it is not checkMate.
                            }
                        }
                    }
                    //System.out.println("No check Saving units available. ");
                }

            } else if(rowDiff == 0) {
                // The checking Unit is attacking horizontally
                if(checkingUnit.column < King25.column) { //Checking piece is towards the left
                    for(int column = checkingUnit.column; column < King25.column; column++) { //See if a piece can fit inbetween King and Checking unit.
                        for(Unit unit : simUnits) {
                            if(unit != King25 && unit.colour != currColour && unit.canMove(column, checkingUnit.row)) {
                                return false; // An ally unit can attack/block the path so it is not checkMate.
                            }
                        }
                    }
                }

                if(checkingUnit.column > King25.column) {
                    for(int column = checkingUnit.column; column > King25.column; column--) { //See if a piece can fit inbetween King and Checking unit.
                        for(Unit unit : simUnits) {
                            if(unit != King25 && unit.colour != currColour && unit.canMove(column, checkingUnit.row)) {
                                return false; // An ally unit can attack/block the path so it is not checkMate.
                            }
                        }
                    }
                }


            } else if(columDiff == rowDiff) {
                // The checking unit is attacking diagonally
                if(checkingUnit.row < King25.row) { //Checking unit is above King

                    if(checkingUnit.column < King25.column) { //Checking unit is at upperLeft
                        for(int column = checkingUnit.column, row = checkingUnit.row; column < King25.column; column++, row++) {
                            for(Unit unit : simUnits) {
                                if(unit != King25 && unit.colour != currColour && unit.canMove(column, row)) {
                                    return false;
                                }
                            }
                        }
                    }

                    if(checkingUnit.column > King25.column) { //Checking unit is at upperRight
                        for(int column = checkingUnit.column, row = checkingUnit.row; column > King25.column; column--, row++) {
                            for(Unit unit : simUnits) {
                                if(unit != King25 && unit.colour != currColour && unit.canMove(column, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(checkingUnit.row > King25.row) { //Checking unit is below King.
                    if(checkingUnit.column < King25.column) { //Checking unit is at lowerLeft
                        for(int column = checkingUnit.column, row = checkingUnit.row; column < King25.column; column++, row--) {
                            for(Unit unit : simUnits) {
                                if(unit != King25 && unit.colour != currColour && unit.canMove(column, row)) {
                                    return false;
                                }
                            }
                        }
                    }

                    if(checkingUnit.column > King25.column) { //Checking unit is at lowerRight
                        for(int column = checkingUnit.column, row = checkingUnit.row; column > King25.column; column--, row--) { //CORRECT
                            for(Unit unit : simUnits) {
                                if(unit != King25 && unit.colour != currColour && unit.canMove(column, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }

            } else {
                // Checking unit must be a knight since they are weird like that.
                //Turns out u cant block Knights attacks <3
                return false;
            }
        }

        return true; //True if the king is in check and no allies units can help.
    }

    /*
    Simulate if there is any squares where the king can move to.
     */
    private boolean kingCanMove(Unit king) {
        if(isValidMove(king, -1, -1))
        {return true;} //1st
        if(isValidMove(king, 0, -1))
        {return true;} //2nd
        if(isValidMove(king, 1, -1))
        {return true;} //3rd
        if(isValidMove(king, -1, 0))
        {return true;} //4th
        if(isValidMove(king, 1, 0))
        {return true;} //5th
        if(isValidMove(king, -1, 1))
        {return true;} //6th
        if(isValidMove(king, 0, 1))
        {return true;} //7th
        if(isValidMove(king, 1, 1))
        {return true;} //8th

//        -1 -1  (1st)
//        0, -1 (2nd)
//        1, -1 (3rd)
//        -1, 0 (4th)
//        1, 0 (5th)
//        -1, 1 (6th)
//        0,1 (7th)
//        1,1 (8th)

        return false; //There is no square that the king can move to.
    }

    /*
    Temporarily update the King's position for a second to see if the king can move.

     So we're checking what the king can do with its usua
     */
    private boolean isValidMove(Unit King, int columnPlus, int rowPlus) {
        boolean isValidMove = false;

        //Temporarily update the kings position
        King.column += columnPlus;
        King.row += rowPlus;

        if(King.canMove(King.column, King.row)) {

            if(King.hittingUnit != null) {
                simUnits.remove(King.hittingUnit.getIndex());
            }
            if(isIllegal(King) == false) { //If the move can move to the square then its a safe position.
                isValidMove = true;
            }
        }

        // Reset the King's position and restore the removed units. (Simulation is over)
        King.resetPosition();
        copyUnits(units, simUnits);

        return isValidMove;
    }

    private boolean isStaleMate() {

        int count = 0;
        // Count number of units
        for(Unit unit : simUnits) {
            if(unit.colour != currColour) {
                count++;
            }
        }

        if(count == 1) {
            if(kingCanMove(getKing(true)) == false) { //True if the king can't move.
                return true; //Stalemate...
            }
        }

        return false; // King is not in Stalemate
    }



    /*
        Responsible for changing the turns of each player.
     */
    private void changePlayer() {

        if(currColour == WHITE) {
            currColour = BLACK;
        } else { currColour = WHITE; }
        activeUnit = null; // So we can let go of the unit last being carried.
    //In my version activeUnit is outside this method. (CANCELLED BECAUSE THIS BROKE PROMOTIONS)
    }

    /*
    UPDATE HERE, MAKE IT SO THAT THERE IS ADDITINAL IF STATEMENTS CHECKING YOUR THE PLAYER'S REGION.
     */
    private boolean  canPromote() {

        if(activeUnit.getClass() == Pawn.class) {
            if(currColour == WHITE && activeUnit.row == 0 || currColour == BLACK && activeUnit.row == 7) {

                promoUnit.clear();
                promoUnit.add(new Rook(currColour, 9,2));
                promoUnit.add(new Knight(currColour, 9,3));
                promoUnit.add(new Bishop(currColour, 9,4));
                promoUnit.add(new Queen(currColour, 9,5));
                return true;

            }
        }

        return false;
    }


    /*
    UPDATE ME WHEN CREATING OTHER REGIONS.
     */
    private void promoting() { //This part 1:42:52 might be how we do UI. We need images tho but maybe text will work

        System.out.println(activeUnit);
        if(mouse.pressed) {
            for(Unit unit : promoUnit) {
                if(unit.column == mouse.x/Board.SQUARE_SIZE && unit.row == mouse.y/ Board.SQUARE_SIZE) { //True if the mouse is in one of these pieces.

                    if(unit.getClass() == Rook.class) {
                        simUnits.add(new Rook(currColour, activeUnit.column, activeUnit.row));
                        System.out.println("Pawn evolved to Rook!");
 
                    } else if (unit.getClass() == Knight.class) {
                        simUnits.add(new Knight(currColour, activeUnit.column, activeUnit.row));
                        System.out.println("Pawn evolved to Knight!");

                    } else if (unit.getClass() == Bishop.class) {
                        simUnits.add(new Bishop(currColour, activeUnit.column, activeUnit.row));
                        System.out.println("Pawn evolved to Bishop!");

                    } else if (unit.getClass() == Queen.class) {
                        simUnits.add(new Queen(currColour, activeUnit.column, activeUnit.row));
                        System.out.println("A new Queen has been appointed!");

                    }

                    simUnits.remove(activeUnit.getIndex());
                    copyUnits(simUnits, units);
                    activeUnit = null;
                    promotion = false;
                    changePlayer();;
                }
            }
        }

    }


    /*
    paintComponent a JComponent that JPanel inherits and is used to draw
    objects on the panel.

    Edit this to make graphic changes to the chess board.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g; //casting..

        // Board...
        board.draw(g2);

        // Units...
        for(Unit p : simUnits) {
            p.draw(g2);
        }

        //This is responsible for colouring the piece u want to move the piece to.
        if(activeUnit != null) {
            if(canMove) {

                if(isIllegal(activeUnit) || opponentCanCaptureKing()) {
                    g2.setColor(Color.red);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activeUnit.column*Board.SQUARE_SIZE, activeUnit.row*Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                } else {
                        g2.setColor(Color.CYAN);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                        g2.fillRect(activeUnit.column * Board.SQUARE_SIZE, activeUnit.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                    if(infect) { //Show spawn position for new Zombie units...
                        g2.setColor(Color.MAGENTA);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                        g2.fillRect(activeUnit.pre_Column*Board.SQUARE_SIZE, activeUnit.pre_Row*Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                        g2.setColor(Color.MAGENTA);
                        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
                        g2.drawString("INFECT UNIT???", 840, 700);
                    }
                }

            }

            //Draw the active piece in the end so it wont be hidden by the board or coloured square.
            activeUnit.draw(g2);
        }

        //Menu bar status messages (Change this to a proper UI later)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
        g2.setColor(Color.PINK);

        if(promotion) {
            g2.drawString("Promote to:,", 840, 1);
            for (Unit unit : promoUnit) { //Scan promoUnit list and draw the image 1 by 1.
                g2.drawImage(unit.image, unit.getX(unit.column), unit.getY(unit.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        } else {
            if(currColour == WHITE) {
                g2.drawString("Player White's turn", 840, 550);

                if(checkingUnit != null && checkingUnit.colour == BLACK) {
                    g2.setColor(Color.red);
                    g2.drawString("Save your king!", 840, 650);
                    g2.drawString("He is in check.", 840, 700);
                }

                g2.setColor(Color.WHITE);
            } else {
                g2.fillOval(800, 2000, 30, 200);
                g2.drawString("Player Black's turn", 840, 250);
                if(checkingUnit != null && checkingUnit.colour == WHITE) {
                    g2.setColor(Color.red);
                    g2.drawString("Save your king!", 840, 100);
                    g2.drawString("He is in check.", 840, 150);
                }

                g2.setColor(Color.BLACK);

            }
        }

        if(gameOver) {
            String WinString = "";
            if (currColour == WHITE) {
                WinString = "Player 1 Wins!!!";
                g2.setFont(new Font("Arial", Font.PLAIN, 90));
                g2.setColor(Color.WHITE);
            } else {
                WinString = "Player 2 Wins!!!";
                g2.setFont(new Font("Calibri", Font.BOLD, 90));
                g2.setColor(Color.BLACK);
            }
            g2.drawString(WinString, 200, 420);
        }
        if(staleMate) {
            g2.setFont(new Font("Arial", Font.PLAIN, 90));
            g2.setColor(Color.ORANGE);
            g2.drawString("Stalemate...", 200, 420);
        }
    }


    //1 = Classic, 2 = Coven
    public void startGame() {
        gameThread = new Thread(this);

        gameThread.start(); // So we create a thread and call the run method

    }

    private void setUnitsRegional(int player1, int player2) {

        //Set units for player1
        switch (player1) {
            case 1:
                //Team White Classic
                units.add(new Pawn(WHITE, 0,6));
                units.add(new Pawn(WHITE, 1,6));
                units.add(new Pawn(WHITE, 2,6));
                units.add(new Pawn(WHITE, 3,6));
                units.add(new Pawn(WHITE, 4,6));
                units.add(new Pawn(WHITE, 5,6));
                units.add(new Pawn(WHITE, 6,6));
                units.add(new Pawn(WHITE, 7,6));

                units.add(new Rook(WHITE, 0,7));
                units.add(new Rook(WHITE, 7,7));

                units.add(new Knight(WHITE, 1,7));
                units.add(new Knight(WHITE, 6,7));

                units.add(new Bishop(WHITE, 2,7));
                units.add(new Bishop(WHITE, 5,7));

                units.add(new Queen(WHITE, 3,7));

                units.add(new King(WHITE, 4,7));
            break;

            case 2:
                //Team White Coven
                units.add(new Zombie(WHITE, 0,6));
                units.add(new Zombie(WHITE, 1,6));
                units.add(new Zombie(WHITE, 2,6));
                units.add(new Zombie(WHITE, 3,6));
                units.add(new Zombie(WHITE, 4,6));
                units.add(new Zombie(WHITE, 5,6));
                units.add(new Zombie(WHITE, 6,6));
                units.add(new Zombie(WHITE, 7,6));

                units.add(new Queen(WHITE, 0,7));
                units.add(new Queen(WHITE, 7,7));

                units.add(new Queen(WHITE, 1,7));
                units.add(new Queen(WHITE, 6,7));

                units.add(new Queen(WHITE, 2,7));
                units.add(new Queen(WHITE, 5,7));

                units.add(new Queen(WHITE, 3,7));

                units.add(new King(WHITE, 4,7));

        }

        //Set Units for player2
        switch (player2) {
            case 1:
                //Team Black Classic
                units.add(new Pawn(BLACK, 0,1));
                units.add(new Pawn(BLACK, 1,1));
                units.add(new Pawn(BLACK, 2,1));
                units.add(new Pawn(BLACK, 3,1));
                units.add(new Pawn(BLACK, 4,1));
                units.add(new Pawn(BLACK, 5,1));
                units.add(new Pawn(BLACK, 6,1));
                units.add(new Pawn(BLACK, 7,1));

                units.add(new Rook(BLACK, 0,0));
                units.add(new Rook(BLACK, 7,0));

                units.add(new Knight(BLACK, 1,0));
                units.add(new Knight(BLACK, 6,0));

                units.add(new Bishop(BLACK, 2,0));
                units.add(new Bishop(BLACK, 5,0));

                units.add(new Queen(BLACK, 3,0));

                units.add(new King(BLACK, 4,0));
                break;

            case 2:
                //Team Black Coven
                units.add(new Zombie(BLACK, 0,1));
                units.add(new Zombie(BLACK, 1,1));
                units.add(new Zombie(BLACK, 2,1));
                units.add(new Zombie(BLACK, 3,1));
                units.add(new Zombie(BLACK, 4,1));
                units.add(new Zombie(BLACK, 5,1));
                units.add(new Zombie(BLACK, 6,1));
                units.add(new Zombie(BLACK, 7,1));

                units.add(new Queen(BLACK, 0,0));
                units.add(new Queen(BLACK, 7,0));

                units.add(new Queen(BLACK, 1,0));
                units.add(new Queen(BLACK, 6,0));

                units.add(new Queen(BLACK, 2,0));
                units.add(new Queen(BLACK, 5,0));

                units.add(new Queen(BLACK, 3,0));

                units.add(new King(BLACK, 4,0));

        }
    }
    public void setUnitsClassic() {

        //Test White Positions:
//        units.add(new Queen(WHITE, 0,6));
//        units.add(new Queen(WHITE, 1,6));
//        units.add(new Queen(WHITE, 2,6));
//        units.add(new Queen(WHITE, 3,3));
//        units.add(new Queen(WHITE, 4,6));
//        units.add(new Queen(WHITE, 5,6));
//        units.add(new Queen(WHITE, 6,6));
//        units.add(new Queen(WHITE, 7,6));
//
//        units.add(new Rook(WHITE, 0,7));
//        units.add(new Rook(WHITE, 7,7));

//        units.add(new Knight(WHITE, 1,7));
//        units.add(new Knight(WHITE, 6,7));
//
//        units.add(new Bishop(WHITE, 2,5));
//        units.add(new Bishop(WHITE, 5,7));
//
//        units.add(new Queen(WHITE, 3,5));

//        units.add(new King(WHITE, 4,7));

        //Team White
        units.add(new Pawn(WHITE, 0,6));
        units.add(new Pawn(WHITE, 1,6));
        units.add(new Pawn(WHITE, 2,6));
        units.add(new Pawn(WHITE, 3,6));
        units.add(new Pawn(WHITE, 4,6));
        units.add(new Pawn(WHITE, 5,6));
        units.add(new Pawn(WHITE, 6,6));
        units.add(new Pawn(WHITE, 7,6));

        units.add(new Rook(WHITE, 0,7));
        units.add(new Rook(WHITE, 7,7));

        units.add(new Knight(WHITE, 1,7));
        units.add(new Knight(WHITE, 6,7));

        units.add(new Bishop(WHITE, 2,7));
        units.add(new Bishop(WHITE, 5,7));

        units.add(new Queen(WHITE, 3,7));

        units.add(new King(WHITE, 4,7));

//        Team Black
        units.add(new Pawn(BLACK, 0,1));
        units.add(new Pawn(BLACK, 1,1));
        units.add(new Pawn(BLACK, 2,1));
        units.add(new Pawn(BLACK, 3,1));
        units.add(new Pawn(BLACK, 4,1));
        units.add(new Pawn(BLACK, 5,1));
        units.add(new Pawn(BLACK, 6,1));
        units.add(new Pawn(BLACK, 7,1));

        units.add(new Rook(BLACK, 0,0));
        units.add(new Rook(BLACK, 7,0));

        units.add(new Knight(BLACK, 1,0));
        units.add(new Knight(BLACK, 6,0));

        units.add(new Bishop(BLACK, 2,0));
        units.add(new Bishop(BLACK, 5,0));

        units.add(new Queen(BLACK, 3,0));

        units.add(new King(BLACK, 4,0));

    }

    private void copyUnits(ArrayList<Unit> source, ArrayList<Unit> target) {

        target.clear();
//        for (int i = 0; i < source.size(); i++) {
//            target.add(source.get(i));
//        }

        target.addAll(source);
    }

    /*
    Process necessary for running the game.
     */
    @Override
    public void run()  {

        // GAME LOOP#
        /*/
        A sequence of processes that run continously as long as the game is running
        System.nanoTime() measures the elapsed time and call update and repaint methods once every 1/60 of a second.
         */
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while(gameThread != null) {

            currentTime = System.nanoTime();

            delta += (currentTime - lastTime)/drawInterval;
            lastTime = currentTime;

            if(delta >= 1) {
                update();
                repaint();
                //delta = delta - 1;
                delta--;
            }

        }

    }
}
