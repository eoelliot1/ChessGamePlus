package piece;

import main.Game_Panel;

public class Necromancer extends Unit {

    public int summonDistance;
    boolean summonStatus = false;

//    //j is a multiple of four if
//    j % 4 == 0

    public Necromancer(int colour, int row, int column) {
        super(colour, row, column);
        if (colour == Game_Panel.WHITE) {
            image = getImage("/Pieces/UnitImages/WhiteCoven(Necromancer)");
            summonDistance = -1;

        } else {
            image = getImage("/Pieces/UnitImages/WhiteCoven(Necromancer)");
            summonDistance = 1;
        }
    }

    public  void changeSummonStatus(boolean status) {
        summonStatus = status;
    }

    public boolean canMove (int targetCol, int targetRow) {
        //True if target is within board boundaries.
        if(isWithinBoard(targetCol, targetRow)) {


            //42mins ish explains this.
            if(Math.abs(targetCol - pre_Column) + Math.abs(targetRow - pre_Row) == 1 || //This covers, top/left/down/right.
                    Math.abs(targetCol - pre_Column) * Math.abs(targetRow - pre_Row) == 1) { //This covers, topRight/topLeft/downRight/downLeft
                return isValidSquare(targetCol, targetRow);
            }
        }


       return false;
    }

    /*
        True if there is space in front of the unit to summon.
     */
    public boolean canSummon() {
        //True if summoining target is within board boundaries.

        hittingUnit = getHittingU(column, pre_Row+summonDistance);
        if( getHittingU(column, pre_Row+summonDistance) == null) { //True if there is no unit in the way
            return true;
        }
        return false;
    }

}
