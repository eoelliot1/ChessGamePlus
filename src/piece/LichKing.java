package piece;

import main.Game_Panel;
public class LichKing extends Unit{
    //
    //
    // private int lives; //This governs how many turns the player can play when the Lich king .

    //Making lives Local to the unit wouldn't work... It has to be in Game_Panel.
    public LichKing(int colour, int row, int column) {
        super(colour, row, column);
        if (colour == Game_Panel.WHITE) {
            image = getImage("/Pieces/UnitImages/WhiteCoven(LichKing)");

        } else {
            image = getImage("/Pieces/UnitImages/Drawing");
        }

    }

    public boolean canMove(int targetCol, int targetRow) {

        //True if target is within board boundaries.
        if(isWithinBoard(targetCol, targetRow)) {

            //Math.abs is getting the difference between the col/row and previous col/row
            //So we add them and they must be equal to 1.
            //So to elaborate, the king can only move by 1 row/col.
            //that's why the difference from its space and the previous space can be no more than 1.


            //42mins ish explains this.
            if(Math.abs(targetCol - pre_Column) + Math.abs(targetRow - pre_Row) == 1 || //This covers, top/left/down/right.
                    Math.abs(targetCol - pre_Column) * Math.abs(targetRow - pre_Row) == 1) { //This covers, topRight/topLeft/downRight/downLeft
                //For diagonal positions they both need to be 1, otherwise its not a diagonal position. So if I went top right,
                //it would mean I would have to offset from the original position by "1" in both the col/row directions.
                return isValidSquare(targetCol, targetRow); //isValidSquare handles the logic whether the movement is valid or not.
            }
        }

        // (SPECIAL MOVEMENTS):
        // Castling:
        /*/
          How this works is that it runs a for loop that checks all the simulated pieces to see if the specified conditions are met for either left & right.
          Note that Left and Right has its own separate logic to figure out it conditions

          Right(Will check if Rook is in it's starting position and if there is any units in the way
          Left (will check position u[0](Knight's starting position) to see if it is occupied and [1](Rook's starting position) if Rook is still there.
          Both will check whether the king has moved once.
         */
//        if (movedOnce == false) {
//
//            //Right Castling (Doesn't seem to work)
//            if(targetCol == 6) {
//                System.out.println("Right castling activated:");
//            }
//            if (targetCol == pre_Column + 2 && targetRow == pre_Row && unitIsOnStraightLine(targetCol, targetRow) == false) {
//                //There must be no other pieces in the path if there is a piece interrupting the path.
//                for (Unit unit : Game_Panel.simUnits) {
//                    if (unit.column == pre_Column + 3 && unit.row == pre_Row && unit.movedOnce == false) {
//                        Game_Panel.castlingUnit = unit;
//                        return true;
//                    }
//                }
//            }
//
//            //Left Castling (Seems to work)
//            if (targetCol == pre_Column - 2 && targetRow == pre_Row && unitIsOnStraightLine(targetCol, targetRow) == false) {
//                Unit u[] = new Unit[2];
//                // Check every Unit on the board if it is blocking the position:
//                //1:25:30
//                for (Unit unit : Game_Panel.simUnits) {
//                    if (unit.column == pre_Column - 3 && unit.row == targetRow) {
//                        u[0] = unit; //This is the Knight's starting position. It needs to be null...
//                    }
//                    if (unit.column == pre_Column - 4 && unit.row == targetRow) {
//                        u[1] = unit; //This is the Rook's starting position. The Rook needs to remain in this position.
//                        System.out.println("Rook found, switching positions");
//                    }
//                }
//
//                // ERROR IN CODE, I HAD TO MOVE THIS OUT THE FOR LOOP OTHERWISE THE LOOP ENDS PREMATURELY, BEFORE IT CAN PROPERLY CHECK ALL UNITS.
//                if (u[0] == null && u[1] != null && u[1].movedOnce == false) { //IF BREAKS TRY REMOVING "u[1].getClass() == Rook.class"
//                    //True if there are no units blocking the way towards the rook.
//                    //We also check that piece u[1] is a rook.
//                    Game_Panel.castlingUnit = u[1];
//                    return true;
//                }
//            }
//        }

        return false;
    }
}
