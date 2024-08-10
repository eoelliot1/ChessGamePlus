package piece;
import main.Game_Panel;

public class Skeleton extends Unit {

    public Skeleton(int colour, int row, int column) {
        super(colour, row, column);
        if (colour == Game_Panel.WHITE) {
            image = getImage("/Pieces/UnitImages/WhiteCoven(Skeleton)");
        } else {
            image = getImage("/Pieces/UnitImages/WhiteCoven(Skeleton)");
        }
    }

    //Can't we just create a 1 turn movement boolean here?
    //If it moves once then we just turn the Boolean to false.

    public boolean canMove (int targetCol, int targetRow) {

        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {

            //Movement value is determined by unit's colour...

            int moveValue;
            int moveValue2;
            if (colour == Game_Panel.WHITE) {
                moveValue = -1; //Unit is White.
                moveValue2 = -2;
            } else {
                moveValue = 1; //Unit is Black.
                moveValue2 = 2;
            }

            //
            hittingUnit = getHittingU(targetCol, targetRow);
            //Controls movement. Column must be the same and target_Row must be pre_Row + moveValue.
//            System.out.println(pre_Column);
//            System.out.println("/" + targetCol);

            //Enables units to move 1 or 2 rows forwards
            if(unitIsOnStraightLine(targetCol, targetRow) == false && targetCol == pre_Column) {
                if(Math.abs(targetRow) == Math.abs(pre_Row + moveValue) || Math.abs(targetRow) == Math.abs(pre_Row + moveValue2)) {
                    return true;
                }


            }

//            //Capture diagonal opponents.
//            // Moves to left or Right by 2 square so colum difference needs to be 2.
//            //Cannot move diagonally if there is any unit in the way however.
            if(unitIsOnStraightUpDownLine(targetCol, targetRow) == false) {
                if(Math.abs(targetCol - pre_Column) == 1 && Math.abs(targetRow) == Math.abs(pre_Row + moveValue2) && hittingUnit == null) {
                    return true;
                }
            }
            /* To do:
            Try see if u can make Skeletons be able to move diagonally by 2 squares
            even if there is a unit blocking the way...
            This is so skeletons can slip pass other units at least.
             */

        }

        //If the skeleton hits a unit it also kills itself.
        return false;
    }


}
