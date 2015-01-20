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
		for (int i = 1; i <= nPlayers; i++)   {
			for (int j = 1; j <= N_CATEGORIES; j++)   {
				scoreArray[i][j] = -1;
			}
		}
		for (int i = 1; i <= nPlayers; i++)	{
			scoreArray[i][UPPER_SCORE] = 0;
			scoreArray[i][LOWER_SCORE] = 0;
			scoreArray[i][UPPER_BONUS] = 0;
		}
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
		display.printMessage(playerNames[i-1] + "'s turn, click the \"Roll Dice\" button to roll the dice");
		display.waitForPlayerToClickRoll(i);
		
		diceArray = rollAllDice(N_DICE);
		display.displayDice(diceArray);
		display.printMessage("Select the dice you wish to re-roll and click \"Roll Again\".");
		
		// Second roll - reroll selected dice - leave unchanged if none-selected
		
		display.waitForPlayerToSelectDice();
		diceArray = checkSelectedDice(diceArray);
		display.displayDice(diceArray);
		display.printMessage("Last roll. Select the dice you wish to re-roll and click \"Roll Again\".");
		
		// Third roll repeat second roll sequence
		display.waitForPlayerToSelectDice();
		diceArray = checkSelectedDice(diceArray);
		display.displayDice(diceArray);
		display.printMessage("Select a category for this roll");
		
		int category = display.waitForPlayerToSelectCategory();
		while (scoreArray[i][category] != -1) { 
			display.printMessage("This category is already used. Select another.");
			category = display.waitForPlayerToSelectCategory(); 	
		}
		
		// select category and update score
		playerScoring(i, category, diceArray);

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
	
	private void playerScoring(int i, int category, int[] dice)	{
				
		// check with magicstub if dice match category and give score is so. Zero if not matching category
		if (YahtzeeMagicStub.checkCategory(dice, category) == true) {
			int score = getCategoryScore(category);
			scoreArray[i][category] = score;
			display.updateScorecard(category, i, score); 
		} else  { 
			display.updateScorecard(category, i, 0);
			scoreArray[i][category] = 0;
		}	
		
		calculateUpperCategoryScore(i);
		calculateLowerCategoryScore(i);
		calculateTotalScore(i);
	
	}

	private int getCategoryScore(int category) {
		// This calculates the score for each category
		
		switch (category) {
		case ONES: case TWOS: case THREES: case FOURS: case FIVES: case SIXES:
			return countNumber(category);
		case THREE_OF_A_KIND: case FOUR_OF_A_KIND: case CHANCE:
			return sumAllDice();
		case SMALL_STRAIGHT:
			return 30;
		case LARGE_STRAIGHT:
			return 40;
		case FULL_HOUSE:
			return 25;
		case YAHTZEE:
			return 50;
		default:
			return 42;	
		}
	}

	/**
	 * @return
	 */
	private int sumAllDice() {
		int score = 0;
		for (int i=0; i < N_DICE; i++)  {
			score = score + diceArray[i];
		}
		return score;
	}

	private int countNumber(int Number) {
		// this method determines the number of times a number appears in an array sums that number;
		int score = 0;
		for (int i=0; i < N_DICE; i++)  {
			if (diceArray[i] == Number)  { score = score + Number;}
		}
		return score;
	}

	private void calculateTotalScore(int i) {
		// Adds the upper category total, lower category total, and the upper category bonus and displays sends message to display the answer for player i
		scoreArray[i][TOTAL] = scoreArray[i][LOWER_SCORE] + scoreArray[i][UPPER_SCORE] + scoreArray[i][UPPER_BONUS];
		display.updateScorecard(N_CATEGORIES, i, scoreArray[i][TOTAL]);
	}

	private void calculateLowerCategoryScore(int i) {
		// this method sums the lower category and updates the players array
		int counter = 0;
		
		for (int k = THREE_OF_A_KIND; k < LOWER_SCORE; k++)   {
			if (scoreArray[i][k] == -1) { counter = counter + 1; }   // Sentinel in array is -1 needs to be negated before calculating the score
			counter = scoreArray[i][k] + counter;
		}
		
		scoreArray[i][LOWER_SCORE] = counter;
		
	}

	private void calculateUpperCategoryBonus(int i)	{
		if (scoreArray[i][UPPER_SCORE] >= BONUS_TOTAL) {
			scoreArray[i][UPPER_BONUS] = BONUS_SCORE;	
		}  else  {
			scoreArray[i][UPPER_BONUS] = 0;
		}
		
	}
	
	private void calculateUpperCategoryScore(int i) {
		// this method sums the upper category and updates the players array
		int counter = 0;
		
		for (int k = ONES; k < UPPER_SCORE; k++)   {
			if (scoreArray[i][k] == -1) { counter = counter + 1; }   // Sentinel in array is -1 needs to be negated before calculating the score
			counter = scoreArray[i][k] + counter;
		}
		
		scoreArray[i][UPPER_SCORE] = counter;

	}
	
	private void endGame()   {
		
		// iterate through players and display their upper, lower, and bonus scores
		for (int i = 1; i < nPlayers + 1; i++)  {
			display.updateScorecard(LOWER_SCORE, i, scoreArray[i][LOWER_SCORE]);
			display.updateScorecard(UPPER_SCORE, i, scoreArray[i][UPPER_SCORE]);
			calculateUpperCategoryBonus(i);
			display.updateScorecard(UPPER_BONUS, i, BONUS_SCORE);
			calculateTotalScore(i);
		}
		display.printMessage("Game Over");  // TODO: update with winner and their score
	}
		
/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int[][] scoreArray;
	private int[] diceArray = new int[N_DICE];  
	
// Constants	
	private static final int BONUS_SCORE = 35;
	private static final int BONUS_TOTAL = 63;
	
}