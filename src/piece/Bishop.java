package piece;

import main.Game_Panel;

public class Bishop extends Unit {
    public Bishop(int colour, int row, int column) {
        super(colour, row, column);
        if (colour == Game_Panel.WHITE) {
            image = getImage("/Pieces/UnitImages/WhiteClassic(Bishop)");

        } else {
            image = getImage("/Pieces/UnitImages/BlackClassic(Bishop)");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        //True if target is within board boundaries.
        if (isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {

            if(Math.abs(targetCol - pre_Column) == Math.abs(targetRow - pre_Row)) {
                if (isValidSquare(targetCol, targetRow) && unitIsOnDiagonalLine(targetCol, targetRow) == false) {
                    return true;
                }
            }
        }
        return false;
    }
}
