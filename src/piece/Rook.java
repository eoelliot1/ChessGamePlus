package piece;

import main.Game_Panel;

//KNOWN BUGS ROOKS CAN'T SEEM TO KILL UNITS ON THE TOP ROW ON THE RIGHT?

public class Rook extends Unit{
    public Rook(int colour, int row, int column) {
        super(colour, row, column);
        if (colour == Game_Panel.WHITE) {
            image = getImage("/Pieces/UnitImages/WhiteClassic(Rook)");

        } else {
            image = getImage("/Pieces/b-rook");
        }


    }

    public boolean canMove(int targetCol, int targetRow) {

        //True if target is within board boundaries.
        if(isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
            if(targetCol == pre_Column || targetRow == pre_Row) //Knight can only move in a ratio of 1:2 or 2:1. So the only way to get 2 is to multiply 1 by 2.
            {
                if(isValidSquare(targetCol, targetRow) && unitIsOnStraightLine(targetCol, targetRow) == false) {
                    return true;
                }
            }
        }

        return false;
    }
}
