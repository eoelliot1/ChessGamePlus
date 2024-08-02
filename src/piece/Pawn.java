package piece;
import main.Game_Panel;

public class Pawn extends Unit {

    public Pawn(int colour, int row, int column) {
        super(colour, row, column);
        if (colour == Game_Panel.WHITE) {
            image = getImage("/Pieces/UnitImages/WhiteClassic(Pawn)");
        } else {
            image = getImage("/Pieces/b-pawn");
        }
    }

    //Can't we just create a 1 turn movement boolean here?
    //If it moves once then we just turn the Boolean to false.

    public boolean canMove (int targetCol, int targetRow) {

        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {

            //Movement value is determined by unit's colour...

            int moveValue;
            if (colour == Game_Panel.WHITE) {
                moveValue = -1; //Unit is White.
            } else {
                moveValue = 1; //Unit is Black.
            }

            //
            hittingUnit = getHittingU(targetCol, targetRow);

            //Controls movement. Column must be the same and target_Row must be pre_Row + moveValue.
            if(targetCol == pre_Column && targetRow == pre_Row + moveValue && hittingUnit == null) {
                return true;
            }

            //Allows the pawn to move 2 squares for the the first turn
            if(targetCol == pre_Column && targetRow == pre_Row + moveValue*2 && hittingUnit == null && movedOnce == false && unitIsOnStraightLine(targetCol, targetRow) == false) {
                return true;
            }

            //Capture diagonal opponents.
            // Moves to left or Right by 1 square so colum difference needs to be 1
            if(Math.abs(targetCol - pre_Column) == 1 && targetRow == pre_Row + moveValue && hittingUnit != null && hittingUnit.colour != colour) {
                return true;
            }
        }

        return false;
    }


}
