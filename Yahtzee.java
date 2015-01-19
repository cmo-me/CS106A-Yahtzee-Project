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
		if (nPlayers > MAX_PLAYERS) { 
			nPlayers = dialog.readInt("No more than 4 players! Enter number of players");
		}
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		scoreArray = new int[nPlayers+1][N_CATEGORIES+1];  // using convention of matching array values to actual game values for categories ignoring 0 elements
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
		// check with magicstub if dice match category and give score is so. Zero if not matching category
		if (YahtzeeMagicStub.checkCategory(dice, category) == true) {
			scoreArray[i][category] = 50;
			display.updateScorecard(category, i, 50); 
		} else  { 
			display.updateScorecard(category, i, 0);
			scoreArray[i][category] = 0;
		}	
		
		calculateUpperCategoryScore(i);
		calculateLowerCategoryScore(i);
		calculateTotalScore(i);
	
	}


	private void calculateTotalScore(int i) {
		// Adds the upper category total, lower category total, and the upper category bonus and displays sends message to display the answer for player i
		scoreArray[i][N_CATEGORIES] = scoreArray[i][lowerCategoryTotalIndex] + scoreArray[i][upperCategoryTotalIndex] + scoreArray[i][upperCategoryTotalIndex + 1];
		display.updateScorecard(N_CATEGORIES, i, scoreArray[i][lowerCategoryTotalIndex +1]);
	}

	private void calculateLowerCategoryScore(int i) {
		// this method sums the lower category and updates the players array
		int counter = 0;
		
		for (int k = upperCategoryTotalIndex + 2; k < lowerCategoryTotalIndex; k++)   {
			counter = scoreArray[i][k] + counter;
		}
		
		scoreArray[i][lowerCategoryTotalIndex] = counter;
		
	}

	private void calculateUpperCategoryBonus(int i)	{
		if (scoreArray[i][upperCategoryTotalIndex] >= bonusTotal) {
			scoreArray[i][upperCategoryTotalIndex + 1] = bonusScore;	
		}  else  {
			scoreArray[i][upperCategoryTotalIndex + 1] = 0;
		}
		
	}
	
	
	private void calculateUpperCategoryScore(int i) {
		// this method sums the upper category and updates the players array
		int counter = 0;
		
		for (int k = 1; k < upperCategoryTotalIndex; k++)   {
			counter = scoreArray[i][k] + counter;
		}
		
		scoreArray[i][upperCategoryTotalIndex] = counter;

	}
	
	private void endGame()   {
		for (int i = 1; i < nPlayers + 1; i++)  {
			display.updateScorecard(lowerCategoryTotalIndex, i, scoreArray[i][lowerCategoryTotalIndex]);
			display.updateScorecard(upperCategoryTotalIndex, i, scoreArray[i][upperCategoryTotalIndex]);
			calculateUpperCategoryBonus(i);
			display.updateScorecard(upperCategoryTotalIndex + 1, i, bonusScore);
			calculateTotalScore(i);
			
		}
		display.printMessage("Game Over");  // TODO: update with winner and their score
	}
	
	
	
	
/* Private instance variables */
	private int upperCategoryTotalIndex = 7;  	// This is the actual index of the category total
	private int lowerCategoryTotalIndex = 16;	// This is the actual index of the lower category total
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int[][] scoreArray;
	private int bonusTotal = 63;
	private int bonusScore = 35;
	private int[] diceArray = new int[N_DICE];  
	
}