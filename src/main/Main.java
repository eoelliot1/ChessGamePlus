package main;

import javax.swing.JFrame;
import java.util.Objects;
import java.util.Scanner;


public class Main {


    public static void main(String[] args) {
        int player1Choice = 0;
        int player2Choice = 0;

        Scanner inputs = new Scanner(System.in);  // Create a Scanner object

        // Pick region to play as.
        //Player 1
        while (player1Choice == 0) {
            System.out.println("Player 1 choose region:");
            System.out.println("Type: Classic");
            System.out.println("Type: Coven");

            String player1Input = inputs.next();
            if (Objects.equals(player1Input, "Classic") || Objects.equals(player1Input, "classic")) {
                System.out.println("Classic Region chosen");
                player1Choice = 1;
            } else if(player1Input.equals("Coven")|| player1Input.equals("coven")) {
                System.out.println("Coven Region chosen");
                player1Choice = 2;
            }
        }

        //Player 2
        while (player2Choice == 0) {
            System.out.println("Player 2 choose region:");
            System.out.println("Type: Classic");
            System.out.println("Type: Coven");

            String player2Input = inputs.next();
            if (Objects.equals(player2Input, "Classic") || Objects.equals(player2Input, "classic")) {
                System.out.println("Classic Region chosen");
                player2Choice = 1;
            } else if(player2Input.equals("Coven")|| player2Input.equals("coven")) {
                System.out.println("Coven Region chosen");
                player2Choice = 2;
            }

        }

        JFrame window = new JFrame("Elliot's Chess");

        //Allows the program to stop running when u close the Window
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Makes the program unResizable
        window.setResizable(false);

        //Puts the game of the middle of the screen
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        Game_Panel GP = new Game_Panel(player1Choice, player2Choice);
        window.add(GP);
        window.pack(); //Window adjusts the size to the main.Game_Panel.

        GP.startGame(); //REMEMBER TO IMPORT REGION NUMBERS HERE USING SYSTEM IN.
    }
}