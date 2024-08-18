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
    ArrayList<Unit> summonUnit = new ArrayList<Unit>();
    ArrayList<Necromancer> summonList = new ArrayList<Necromancer>();
    public static Unit castlingUnit;
    Unit activeUnit; //Stores what unit the player is holding.
    Unit checkingUnit;

    // Colours
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currColour = WHITE;

    /*
    This is a number value that represents what region is being played.7
    0 == None chosen
    1 == Classic
    2 == Coven
     */
    int whiteRegion, blackRegion; // 0 = Classic Region,

    int maxSummonCount;
    int summonCount = 0;

    int whiteTurnCount = 0, blackTurnCount = 0;

    int whiteLichLives = 7, blackLichLives = 7;

    // Boolean conditions...
    boolean infect = false;

    boolean selfpunch = false;
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean summon = false;
    boolean gameOver = false;

    boolean staleMate = false;

    public Game_Panel(int player1, int player2) {

        setBackground(Color.black);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        //setUnitsClassic(); // - Old version

        whiteRegion = player1;
        blackRegion = player2;
        setUnitsRegional(player1, player2);
        copyUnits(units, simUnits);
    }




    /*
Handles all the updates per turn.
*/
    private void update() {
        //System.out.println(activeUnit); //BugFixing
        //------------------------ THIS IMPELEMNTATION CONSIDERS DEAD NECROMANCERS BUT DOESN'T ITERATE THROUGH SIMULATION BEFORE COMMEINCING OTHER SUMMONS.
        if(summon == true) {
            for (int i = 0; i < simUnits.size(); i++) {
                if (simUnits.get(i).getClass() == Necromancer.class && simUnits.get(i).colour == currColour) {
//                    if(getLichKing(false) != null) {
//                        necroSummoningv2((Necromancer) simUnits.get(i), true);
//                        System.out.println("Summoning Lich King");
//                    } else {
//                        necroSummoningv2((Necromancer) simUnits.get(i), false);
//                        System.out.println("Summoned");
//                    }
//
                    //2nd Argument true if the player's lich king alive and false if otherwise.
                    necroSummoningv2((Necromancer) simUnits.get(i), (getLichKing(false) != null));
                }
            }
//            if(currColour == WHITE) {
//                    whiteTurnCount++;
//                } else {
//                    blackTurnCount++;
//            }

//------------------------ THIS IMPELEMENTATION DOESN'T COUNT FOR WHEN NECROMANCERS DIES BUT IT ITERATES THROUGH SIMULATION THE CORRECT AMOUNT OF TIMES.

//            if(summonCount >= maxSummonCount == true) {
//                if(currColour == WHITE) {
//                    whiteTurnCount++;
//                } else {
//                    blackTurnCount++;
//                }
//
            // ------------------------ THIS IMPELEMENTATION is compuslory
                summon = false;
                System.out.println(summonCount);
                System.out.println(summon);
                summonCount = 0;
                endTurn();
                //changePlayer();
                // ------------------------ THIS IMPELEMENTATION is compuslory
//            } else {
//                necroSummoningv2(summonList.get(summonCount));
//                summonCount++;
//            };
//  ------------------------ THIS IMPELEMENTATION DOESN'T COUNT FOR WHEN NECROMANCERS DIES BUT IT ITERATES THROUGH SIMULATION THE CORRECT AMOUNT OF TIMES.

//            //Record the max number of units that can be summon, this will determine how many times this section of the code gets run.
//            //Find all the necromancers that can summon and add them to an ArrayList.
//            for (int i = 0; i < simUnits.size(); i++) {
//                if (simUnits.get(i).getClass() == Necromancer.class && simUnits.get(i).colour == currColour) {
//                    summonList.add((Necromancer) simUnits.get(i));
//                }
//            }
//
//            summonCount++;
//            int summonMaxCount = summonList.size();
//            necroSummoningv2(summonList.get(summonCount));
//
//            if(summonCount == summonMaxCount) {//True if all Necro units have summoned.
//                summon = false;
//            }

        } else if(promotion) {
            promoting();
        }

        // YOU CAN ADD STUFF HERE IF U WANT IT TO PROCESS BEFORE THE SIMULATION PHASE STARTS. PROMOTION IS A GOOD EXAMPLE
        else if (gameOver == false && staleMate == false) {
            //Mouse Button pressed
            if (mouse.pressed) {

                //True if player is not holding a unit
                if (activeUnit == null) {

                    for (Unit unit : simUnits) {
                        //If the player's mouse has the same colour, same row and, same column then the player's mouse is on this piece.
                        if (unit.colour == currColour &&
                                unit.column == mouse.x / Board.SQUARE_SIZE && unit.row == mouse.y / Board.SQUARE_SIZE) {
                            activeUnit = unit;
                            System.out.println(activeUnit + "is selected to move.");
                        }
                    }

                } else {//True if a player is already holding a unit.
                    simulate(); //Once the player picks up a unit, we will then simulate their next move. Any actions taken before this is hypothetical
                }
            }

            //IF MOUSE IS NOT BEING PRESSED.
            if (!mouse.pressed) {

                //System.out.println("Mouse released");
                if (activeUnit != null) {


                    if (validSquare) { //It's a valid position so update the position.

                        copyUnits(simUnits, units); //If a piece has been removed then we apply to the backup list.
                        activeUnit.updatePosition();
                        boolean endTheTurn = false; //End the turn if this boolean is true.

                        if (castlingUnit != null) {
                                castlingUnit.updatePosition();
                            }

                        if (lichHasLives(currColour)) {

                        } else {
                            System.out.println(currColour + " has loss the fight!");
                            gameOver = true;
                        }

                        if (isKingInCheck() && isCheckmate()) {
                            gameOver = true; //GG
                            System.out.println("GameOver");
                        } else if (isStaleMate() && isKingInCheck() == false) { //True if its stalemate. King needs to be in check for stalemate
                            staleMate = true;
                        } else { //Not the end of the game
                            if (canPromote()) {
                                promotion = true;
                            } else if (necroSummonTime()) {
                                summon = true;

                                //All the units that will summon next are stored in an array.
                                if (summonList.size() == 0) { //True if the summonList is empty. we only need to run this once.
                                    for (int i = 0; i < simUnits.size(); i++) {
                                        if (simUnits.get(i).getClass() == Necromancer.class && simUnits.get(i).colour == currColour) {
                                                summonList.add((Necromancer) simUnits.get(i));
                                        }
                                    }
                                    maxSummonCount = summonList.size();
                                }
                            } else {
                                endTurn();
                            }
                        }

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

                if(selfHit(activeUnit) == true) {
                    //This unit will also hit itself
                    System.out.println(activeUnit);
                    selfpunch = true;
                    simUnits.remove(activeUnit.getIndex());

                }

            }

            checkCastling();

                if(isIllegal(activeUnit) == false && opponentCanCaptureProtectedUnit() == false) {
                    validSquare = true;
                }



        }
    }

    /*
        Runs all the code required to end the turn under normal circumstances.
     */
    private void endTurn() {
        //Reset values
        infect = false;
        selfpunch = false;

        //Update player Count
        if (currColour == WHITE) {
            whiteTurnCount++;
        } else {
            blackTurnCount++;
        }
        changePlayer();
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

    /*
        Returns true if this type of unit also hits itself
     */
    private boolean selfHit(Unit unit) {

        if(unit.getClass() == Skeleton.class) {
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
        // TODO: Write line of code for LichKing so that it ignores protectedUnit Protection.

         return false; //No King or possibly a lichKing.
    }

    /*
    Returns true on whether a protected unit can be captured.
    However, if the unit is a Lich King then always return false since they are special units but aren't protected.
     */
    private boolean opponentCanCaptureProtectedUnit() {
        Unit King = getKing(false);

        if(King != null) { //True if there is a King in the player's team.
            for(Unit unit : simUnits) {
                if(unit.colour != King.colour && unit.canMove(King.column, King.row)) {
                    return true;
                }
            }
            return false; //There is king but it can't be captured.
        } else {
            Unit LichKing = getLichKing(false);
            if(LichKing != null) { //True if there is a Lich King
                return false; // Lich King has no protection...
            }
        }

        return false; //No protected units found
    }

    /*
    Checks the king.
    If the King is in check then, return true.
    If the king is not in check, return false.
     */
    private boolean isKingInCheck() {

        //BASE CASE
        if (currColour == 0) {
            if(blackRegion == 2) {
                return false; //Opponent has no King.
            }
        } else if (currColour == 1) {
            if(whiteRegion == 2) {
                return false; //Opponent has no King.
            }
        }

            Unit King = getKing(true);

            if(activeUnit.canMove(King.column, King.row)) {
                checkingUnit = activeUnit;
                return true;
            } else {
                checkingUnit = null;
            }

            return false;//There is no king
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


/*
    Try see if you can write functionality for a universal method for all units. So u can put what unit u want as parameters
 */
    private Unit getLichKing(boolean opponent) {
        Unit LichKing = null;

        for(Unit unit : simUnits) {
            if(opponent) {
                if(unit.getClass() == LichKing.class && unit.colour != currColour) {
                    LichKing = unit;
                }
            }
            else {
                if(unit.getClass() == LichKing.class && unit.colour == currColour) {
                    LichKing = unit;
                }
            }
        }
        return LichKing;
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
        if(whiteRegion == 1 && currColour == 0 || blackRegion == 1 && currColour == 1) { //BASE CASE
            return false; // There is no king
        }

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
                    changePlayer();
                }
            }
        }
    }

    private void necroSummoningv2(Necromancer summoningUnit, boolean lichKingAlive) {
            System.out.println(summoningUnit + " is summoning!");
            boolean startSummon = true;


            if(lichKingAlive == false) { //True if the lichKing is dead.
                    if(summoningUnit.canSummon()) { //Emergency LichKing summon. The player has no choice.
                        simUnits.add(new LichKing(currColour, summoningUnit.column, (summoningUnit.row + summoningUnit.summonDistance)));
                        System.out.println("No LichKing found. Necromancer has revived the Lich King!!!");
                    }
            } else {

                    boolean hasChosen = false;

                    while(hasChosen == false) {
                        if (summoningUnit.canSummon() == true) {

                            if (mouse.pressed) {
                                for (Unit summonedUnit : summonUnit) {
                                    if (summonedUnit.column == mouse.x / Board.SQUARE_SIZE && summonedUnit.row == mouse.y / Board.SQUARE_SIZE) { //True if the mouse is in one of these pieces.

                                        if (summonedUnit.getClass() == Skeleton.class) {
                                            simUnits.add(new Skeleton(summonedUnit.colour, summoningUnit.column, (summoningUnit.row + summoningUnit.summonDistance)));
                                            System.out.println("Necromancer has summoned Skeleton!");

                                        } else if (summonedUnit.getClass() == Pawn.class) {
                                            simUnits.add(new Pawn(summonedUnit.colour, summoningUnit.column, (summoningUnit.row + summoningUnit.summonDistance)));
                                            System.out.println("Necromancer has summoned Pawn!");
                                        }
                                        hasChosen = true;
                                    }
                                }
                            }
                        } else {
                            System.out.println(summoningUnit + " Has no space to summon");
                            hasChosen = true;
                        }
                    }
            }
        copyUnits(simUnits, units);
        activeUnit = null;
    }

    private void necroSummoning( ) {

        //Get all the Necromancers
        ArrayList<Necromancer> foundNecromancers = new ArrayList<Necromancer>();

        for (int i = 0; i < simUnits.size(); i++) {
            if (simUnits.get(i).getClass() == Necromancer.class && simUnits.get(i).colour == currColour) {
                foundNecromancers.add((Necromancer) simUnits.get(i)); //CASTING since we know it must be a necromancer.
            }
        }

        //Go through the list of foundNecromancers and activate summmoning

        for (Necromancer unit : foundNecromancers) {
            System.out.println(unit + " is summoning!");

            if (currColour == WHITE && (unit.canSummon() == true)) {
                if (mouse.pressed) {
                    for (Unit summonedUnit : summonUnit) {
                        if (summonedUnit.column == mouse.x / Board.SQUARE_SIZE && summonedUnit.row == mouse.y / Board.SQUARE_SIZE) { //True if the mouse is in one of these pieces.

                            if (summonedUnit.getClass() == Skeleton.class) {
                                simUnits.add(new Skeleton(currColour, unit.column, (unit.row + unit.summonDistance)));
                                System.out.println("Necromancer has summoned Skeleton!");

                            } else if (summonedUnit.getClass() == Pawn.class) {
                                simUnits.add(new Pawn(currColour, unit.column, (unit.row + unit.summonDistance)));
                                System.out.println("Necromancer has summoned Pawn!");
                            }
                            copyUnits(simUnits, units);
                            activeUnit = null;
                        }
                    }
                }
            } else {
                System.out.println(unit + " has no space to summon...");
            }
        }
        //simUnits.remove(activeUnit.getIndex()); Dont think we need this
        //copyUnits(simUnits, units);
        activeUnit = null;
        summon = false;
        changePlayer();
    }

    /*/
        Returns a list of all the playable necromancer units that are ready for summoning.
     */
    private ArrayList<Necromancer> getNecromancers() {
        ArrayList<Necromancer> foundNecromancers = new ArrayList<Necromancer>();
        //boolean containsNecromancers = false;

//        for (Unit unit : simUnits) {
//            if (unit.getClass() == Necromancer.class && unit.colour == currColour) {
//                foundNecromancers.add(unit);
//                }
//            }

        for (int i = 0; i < simUnits.size(); i++) {
            if (simUnits.get(i).getClass() == Necromancer.class) {
                foundNecromancers.add((Necromancer) simUnits.get(i)); //CASTING since we know it must be a necromancer.
            }
        }
        return foundNecromancers;
    }

    /*
        Checks if the current player can summon Necromancers on this turn.
        Base case: this method will automatically return false if there are no necromancers.
     */
    private boolean necroSummonTime() {
        int turncount;
        int summonTime = 6;

        if(currColour == WHITE) { //TRUE IF WHITE SUMMONING, FALSE IS BLACK
            turncount = whiteTurnCount;
        } else {
            turncount = blackTurnCount;
        }

        if(turncount % summonTime == 0 && turncount != 0) {//So every "summonTime" turns, summoning will commence.
            System.out.println("SUMMONING IS SUPPOSED TO COMMENCE?:     " + (turncount % summonTime));

            summonUnit.clear();
            summonUnit.add(new Skeleton(currColour, 9,2));
            summonUnit.add(new Pawn(currColour, 9,3));

            boolean necromancerExist = false;

            //Check if there is any necromancers with the same colour.
            for(int i = 0; i < simUnits.size(); i++) {
                if(simUnits.get(i).getClass() == Necromancer.class && simUnits.get(i).colour == currColour) {

                    necromancerExist = true;
                }
            }
            if (necromancerExist && whiteTurnCount >= summonTime || necromancerExist && blackTurnCount >= summonTime) { //Check to see if time is right for that turn
                return true;
            }
        }
        return false;
    }

    /*
        Checks if the Lich king for the corresponding colour is alive.
        If the Lich King is dead, then reduce the liveCount by 1.
        If the Lives count reaches 0, then return false. The game is over.
        If there still are lives then return true;
     */
    private boolean lichHasLives(int Colour) {

        if(Colour == WHITE) {
            if(whiteRegion == 2) { //True if white region is playing Coven region
                if (getLichKing(false) == null) { //True if you don't have a lichKing
                    if(whiteLichLives <= 0) {

                        System.out.print(" Furthermore " + currColour + " has no lives!");
                        return false; //WHITE LICH IS OUT OF LIVES. GAMEOVER!!
                    } else {

                        System.out.print("However, " + currColour + " still has lives!");
                        whiteLichLives--;
                        return true; //WHITE LICH STILL HAS LIVES.
                    }
                } else {
                    System.out.println("White lich king still lives No lives has been taken");
                    return true;
                }
            }
        } else if (Colour == BLACK) {
            if(blackRegion == 2) { //True if black region is playing Coven region
                if (getLichKing(false) == null) { //True if you don't have a lichKing

                    System.out.println(Colour + " is dead.");
                    if(blackLichLives <= 0) {

                        System.out.print(" Furthermore " + currColour + " has no lives!");
                        return false; //BLACK LICH IS OUT OF LIVES. GAMEOVER!!
                    } else {

                        System.out.print("However, " + currColour + " still has lives!");
                        blackLichLives--;
                        return true; //BLACK LICH STILL HAS LIVES.
                    }
                } else {
                    System.out.println("Black LichKing still lives! No lives has been taken");
                    return true;
                }
            }
        }
        return true;
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

        //Drawing turn count.
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
        g2.drawString(String.valueOf(whiteTurnCount), 900, 50);
        g2.drawString(String.valueOf(blackTurnCount), 900, 100);

        //COVEN UNIT PROPERTIES
        if (whiteRegion == 2) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
            g2.drawString(String.valueOf(whiteLichLives), 800, 750);
        }

        //COVEN UNIT PROPERTIES
        if (blackRegion == 2) {
            g2.setColor(Color.GREEN);
            g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
            g2.drawString(String.valueOf(blackLichLives), 900, 750);

            int summonCommenceCountdown = 6 - (blackTurnCount % 6);

            //g2.drawString(String.valueOf(summonCommenceCountdown+1), 900, 800);
        }

        //This is responsible for colouring the piece u want to move the piece to.
        if(activeUnit != null) {

//            for(int row = 0; row < 8; row++) { //idk this doesn't work
//                for (int colum = 0; colum < 8; colum++) {
//                    if(activeUnit.canMove(row, colum)) {
//                        g2.setColor(Color.green);
//                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
//                        g2.fillRect(colum*Board.SQUARE_SIZE, row*Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
//                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
//
//                        //g2.fillRect(activeUnit.column*Board.SQUARE_SIZE, activeUnit.row*Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
//                    }
//                }
//            }

            if(canMove) {

                if(isIllegal(activeUnit) || opponentCanCaptureProtectedUnit()) {
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

                    if(summon) { //Show spawn position for new Zombie units...
                        g2.setColor(Color.MAGENTA);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
                        g2.drawString("Summoning commence???", 900, 640);
                    }

                    if(selfpunch) {
                        g2.setColor(Color.RED);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                        g2.fillRect(activeUnit.pre_Column*Board.SQUARE_SIZE, activeUnit.pre_Row*Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                        g2.setColor(Color.RED);
                        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
                        g2.drawString("WARNING!!! THIS WILL", 840, 300);
                        g2.drawString("DESTROY ACTIVE UNIT", 840, 500);
                    }
                }

            } else {
                g2.setColor(Color.red);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.fillRect(activeUnit.column*Board.SQUARE_SIZE, activeUnit.row*Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
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
        } else if(summon) {
            g2.drawString("Summon:,", 840, 1);
            for (Unit unit : summonUnit) { //Scan promoUnit list and draw the image 1 by 1.
                g2.drawImage(unit.image, unit.getX(unit.column), unit.getY(unit.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        }

        else {
            if(currColour == WHITE) {

                //Universal white functionality:
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
//                units.add(new Skeleton(WHITE, 0,6));
                units.add(new Zombie(WHITE, 1,6));
                units.add(new Zombie(WHITE, 2,6));
                units.add(new Zombie(WHITE, 3,6));
                units.add(new Zombie(WHITE, 4,6));
                units.add(new Zombie(WHITE, 5,6));
                units.add(new Zombie(WHITE, 6,6));
//                units.add(new Skeleton(WHITE, 7,6));

                units.add(new Necromancer(WHITE, 0,7));
                units.add(new Necromancer(WHITE, 7,7));


                units.add(new Knight(WHITE, 1,7));
                units.add(new Knight(WHITE, 6,7));

                units.add(new Knight(WHITE, 2,7));
                units.add(new Knight(WHITE, 5,7));

                units.add(new Queen(WHITE, 3,7));

//                units.add(new LichKing(WHITE, 4,7));
                //Team WHITE Coven (TEST)
                units.add(new LichKing(WHITE, 4,7));

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
//                units.add(new Skeleton(BLACK, 0,1));
                units.add(new Zombie(BLACK, 1,1));
                units.add(new Zombie(BLACK, 2,1));
                units.add(new Zombie(BLACK, 3,1));
                units.add(new Zombie(BLACK, 4,1));
                units.add(new Zombie(BLACK, 5,1));
                units.add(new Zombie(BLACK, 6,1));
//                units.add(new Skeleton(BLACK, 7,1));

                units.add(new Necromancer(BLACK, 0,0));
                units.add(new Necromancer(BLACK, 7,0));


                units.add(new Knight(BLACK, 1,0));
                units.add(new Knight(BLACK, 6,0));

                units.add(new Knight(BLACK, 2,0));
                units.add(new Knight(BLACK, 5,0));

                units.add(new Knight(BLACK, 3,0));
//
//                units.add(new LichKing(BLACK, 4,0));

                //Team Black Coven (TEST)
                units.add(new LichKing(BLACK, 4,7));
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
