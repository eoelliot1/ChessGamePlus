package piece;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import main.Board;
import main.Game_Panel;

import javax.imageio.ImageIO;

public class Unit {

    public BufferedImage image;
    public int x, y;
    public int row, column, pre_Column, pre_Row;
    public int  colour;
    public Unit hittingUnit;
    public boolean movedOnce;

    public Unit(int colour, int column, int row) {
       this.colour = colour;
       this.column = column;
       this.row = row;
       x = getX(column);
       y = getY(row);
       pre_Column = column;
       pre_Row = row;
    }


    /*
    Getter methods.
     */

    public BufferedImage getImage(String imagePath) {
        try {
            image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
        } catch(IOException e) {
            e.printStackTrace();
        }

        return image;
    }
    public int getX(int column) {
        return column * Board.SQUARE_SIZE;
    }
    public int getY(int row) {
        return row * Board.SQUARE_SIZE;
    }

    public int getColumn (int x) {
        //We added Half_square_size to detect its col/row based on the centre of the piece.
        return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public int getRow (int y) {
        return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    //Updates the position and places the Unit at the centre of the square.
    public void updatePosition() {

        //We update the previous positions since a new move has been confirmed.
        pre_Column = getColumn(x = getX(column));
        pre_Row = getColumn(y = getY(row));
        movedOnce = true; //True if the piece has moved at least once.
    }

    //So we just resetting the positions.
    public void resetPosition() {
        column = pre_Column;
        row = pre_Row;
        x = getX(column);
        y = getY(row);
    }

    // MOVEMENT METHODS //

    /*
    This method is built to be overriden in each unit's class.
    The overidden method will dictate how each piece can move
     */
    public boolean canMove(int targetCol, int targetRow) {
        return false;
    }

    /*
    This method is built to be overidden in each unit's class.
    This method is used to check whether the targetted position is within the chess board's boundaries.
    This may need to be updated if we're to do more levels.
     */
    public boolean isWithinBoard(int targetCol, int targetRow) {
        if(targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7) {
            return true;
        }
        return false;
    }

    /*/
    True only if the given parameters are equal to it's previous parameters.
     */
    public boolean isSameSquare(int targetCol, int targetRow) {
        return targetCol == pre_Column && targetRow == pre_Row;
    }

    /*
        Returns the unit that is interacting/colliding with the current unit that is being moved.
     */
    public Unit getHittingU(int targetCol, int targetRow) {
        for (Unit unit : Game_Panel.simUnits) {
            //So if the units column and row values align but the unit isn't the target.
            if(unit.column == targetCol && unit.row == targetRow && unit != this) {
                return unit;
            }
        }
        return null;
    }

    /*
    Simple method to scan a pieces index. If it is the same piece then scan the index.
     */
    public int getIndex() {
        for(int i = 0; i < Game_Panel.simUnits.size(); i++) {
            if(Game_Panel.simUnits.get(i) == this) {
                return i;
            }
        }
        return 0;
    }


    public boolean isValidSquare(int targetCol, int targetRow) {

        if((hittingUnit = getHittingU(targetCol, targetRow)) == null) {
            //Unit cannot be captured.
            return true; //Square is not unoccupied.
        } else { //Square is occupied so check the colour.

            if(hittingUnit.colour != this.colour) { //True if enemy unit & false if ally unit.
                //The unit can be captured.
                return true;
            } else {
                //The unit cannot be captured.
                hittingUnit = null;
            }
        }

        return false;
    }

    public boolean unitIsOnStraightUpDownLine(int targetCol, int targetRow) {

        //When unit is moving Up
        for(int r = pre_Row-1; r > targetRow; r--) {
            for(Unit unit : Game_Panel.simUnits) { //Check gamePanel units to see if there are any units that could be on the up.
                if(unit.row == r && unit.column == targetCol) { //True if the unit found is adjacently on the up to the current unit direction.
                    hittingUnit = unit;
                    return true;
                }
            }
        }

        //When unit is moving Down
        for(int r = pre_Row+1; r < targetRow; r++) {
            for(Unit unit : Game_Panel.simUnits) { //Check gamePanel units to see if there are any units that could be on the down.
                if(unit.row == r && unit.column == targetCol) { //True if the unit found is adjacently on the down to the current unit direction.
                    hittingUnit = unit;
                    return true;
                }
            }
        }

        return false;

    }
    public boolean unitIsOnStraightLine(int targetCol, int targetRow) {
        //When unit is moving left
        for(int c = pre_Column-1; c > targetCol; c--) {
            for(Unit unit : Game_Panel.simUnits) { //Check gamePanel units to see if there are any units that could be on the left.
                if(unit.column == c && unit.row == targetRow) { //True if the unit found is adjacently on the left to the current unit direction.
                    hittingUnit = unit;
                    return true;
                }
            }
        }

        //When unit is moving Right
        for(int c = pre_Column+1; c < targetCol; c++) {
            for(Unit unit : Game_Panel.simUnits) { //Check gamePanel units to see if there are any units that could be on the right.
                if(unit.column == c && unit.row == targetRow) { //True if the unit found is adjacently on the right to the current unit direction.
                    hittingUnit = unit;
                    return true;
                }
            }
        }

        //When unit is moving Up

        for(int r = pre_Row-1; r > targetRow; r--) {
            for(Unit unit : Game_Panel.simUnits) { //Check gamePanel units to see if there are any units that could be on the up.
                if(unit.row == r && unit.column == targetCol) { //True if the unit found is adjacently on the up to the current unit direction.
                    hittingUnit = unit;
                    return true;
                }
            }
        }

        //When unit is moving Down
        for(int r = pre_Row+1; r < targetRow; r++) {
            for(Unit unit : Game_Panel.simUnits) { //Check gamePanel units to see if there are any units that could be on the down.
                if(unit.row == r && unit.column == targetCol) { //True if the unit found is adjacently on the down to the current unit direction.
                    hittingUnit = unit;
                    return true;
                }
            }
        }

        return false; //No units in the way.
    }

    public boolean unitIsOnDiagonalLine(int targetCol, int targetRow) {

        if(targetRow < pre_Row) {

            //Up left ------------------------------------------------------------------------------
            for (int c = pre_Column - 1; c > targetCol; c--) {
                int diff = Math.abs(c - pre_Column);
                for (Unit unit : Game_Panel.simUnits) {
                    if (unit.column == c && unit.row == pre_Row - diff) { //
                        hittingUnit = unit;
                        return true;
                    }
                }
            }


            //Up Right ------------------------------------------------------------------------------

            for (int c = pre_Column+1; c < targetCol; c++) {
                int diff = Math.abs(c - pre_Column);
                for (Unit unit : Game_Panel.simUnits) {
                    if (unit.column == c && unit.row == pre_Row - diff) { //
                        hittingUnit = unit;
                        return true;
                    }
                }
            }
        }
        if(targetRow > pre_Row) {


            //Bottom Left -------------------------------------------------------------------------------
            for(int c = pre_Column-1; c > targetCol; c--) {
                int diff = Math.abs(c - pre_Column);
                for (Unit unit : Game_Panel.simUnits) {
                    if(unit.column == c && unit.row == pre_Row + diff) { // + because its going down now
                        hittingUnit = unit;
                        return true;
                    }
                }
            }

            //Bottom Right ------------------------------------------------------------------------------
            for(int c = pre_Column+1; c < targetCol; c++) {
                int diff = Math.abs(c - pre_Column);
                for (Unit unit : Game_Panel.simUnits) {
                    if(unit.column == c && unit.row == pre_Row + diff) { // + because its going down now
                        hittingUnit = unit;
                        return true;
                    }
                }
            }
        }
        return false;

    }

    // MOVEMENT METHODS //

    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }


}
