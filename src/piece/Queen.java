package piece;

import main.Game_Panel;

public class Queen extends Unit {
    public Queen(int colour, int row, int column) {
        super(colour, row, column);
        if (colour == Game_Panel.WHITE) {
            image = getImage("/Pieces/UnitImages/WhiteClassic(Queen)");

        } else {
            image = getImage("/Pieces/UnitImages/BlackClassic(Queen)");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if(isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {

            //Vertical movements:
            if(targetCol == pre_Column || targetRow == pre_Row) //Knight can only move in a ratio of 1:2 or 2:1. So the only way to get 2 is to multiply 1 by 2.
            {
                if (isValidSquare(targetCol, targetRow) && unitIsOnStraightLine(targetCol, targetRow) == false) {
                    return true;
                }
            }


            //Diagonal movements:
            if(Math.abs(targetCol - pre_Column) == Math.abs(targetRow - pre_Row)) {
                if (isValidSquare(targetCol, targetRow) && unitIsOnDiagonalLine(targetCol, targetRow) == false) {
                    return true;
                }
            }
        }

        return false;
    }
}
