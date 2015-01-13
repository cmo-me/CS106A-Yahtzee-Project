/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

	private void playGame() {
		for (int i=0; i < N_SCORING_CATEGORIES;i++) {
			for (int j=1; j < nPlayers + 1; j++)  {
				playerTurn(j);
			}
		}
		endGame();
	}
		
	private int rollDie()  {
		return rgen.nextInt(1, 6);
	}
	
	private void playerTurn(int i)  {
		// do entire 3 roll sequence of player turn
		
		// first roll - roll all dice
		display.printMessage("Message 1");
		display.waitForPlayerToClickRoll(i);
		
		int[] diceArray = new int[N_DICE];
		diceArray = rollAllDice(N_DICE);
		display.displayDice(diceArray);
		display.printMessage("Message 2");
		
		// Second roll - reroll selected dice - leave unchanged if none-selected
		
		display.waitForPlayerToSelectDice();
		diceArray = checkSelectedDice(diceArray);
		display.displayDice(diceArray);
		display.printMessage("Message 3");
		
		// Third roll repeat second roll sequence
		display.waitForPlayerToSelectDice();
		diceArray = checkSelectedDice(diceArray);
		display.displayDice(diceArray);
		display.printMessage("Message 4");
		
		// select category and update score
		playerScoring(i, diceArray);

	}
	
	private int[] rollAllDice(int n) {
		int[] array = new int[n];
		for (int j = 0; j < n; j++)   {
			array[j] = rollDie();
		}
		return array;
	}

	private int[] checkSelectedDice(int[] diceArray) {
		for (int k = 0; k < 5; k++)   {
			if (display.isDieSelected(k) == true) {  
				diceArray[k] = rollDie();
			}	
		}
		return diceArray;
	}
	
	private void playerScoring(int i, int[] dice)	{
		display.printMessage("Please select valid category");
		
		int category = display.waitForPlayerToSelectCategory();
		// check with magicstub if dice match category
		if (YahtzeeMagicStub.checkCategory(dice, category) == true) {
			display.updateScorecard(category, i, 50); 
		} else  { 
			display.printMessage("This category is not valid for these dice - No score for you!");
			display.updateScorecard(category, i, 0);
		}	
		
	}
	
	private void endGame()   {
		
	}
	
/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	
}