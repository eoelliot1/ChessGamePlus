package main;

import javax.swing.JFrame;


public class Main {


    public static void main(String[] args) {


        JFrame window = new JFrame("Elliot's Chess");

        //Allows the program to stop running when u close the Window
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Makes the program unResizable
        window.setResizable(false);

        //Puts the game of the middle of the screen
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        Game_Panel GP = new Game_Panel();
        window.add(GP);
        window.pack(); //Window adjusts the size to the main.Game_Panel.

        GP.startGame();
    }
}