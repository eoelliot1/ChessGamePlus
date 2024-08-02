package piece;

import main.Game_Panel;

public class Knight extends Unit {
    public Knight(int colour, int row, int column) {
        super(colour, row, column);
        if (colour == Game_Panel.WHITE) {
            image = getImage("/Pieces/UnitImages/WhiteClassic(Knight)");

        } else {
            image = getImage("/Pieces/b-knight");
        }
    }

    /*
    Consider refactoring this into the unit class.
     */
    public boolean canMove(int targetCol, int targetRow) {

        //True if target is within board boundaries.
        if(isWithinBoard(targetCol, targetRow)) {
            if(Math.abs(targetCol - pre_Column) * Math.abs(targetRow - pre_Row) == 2) //Knight can only move in a ratio of 1:2 or 2:1. So the only way to get 2 is to multiply 1 by 2.
                {return isValidSquare(targetCol, targetRow); }
        }

        return false;
    }
}
