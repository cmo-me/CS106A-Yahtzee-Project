/*
 * File: Yahtzee.java
 * ------------------
 * This is my solution to the Yahtzee project in CS106A Stanford's programming methodology
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
		scoreArray = new int[nPlayers+1][N_CATEGORIES+1];  // array handling is not consistent with either ignore zero or start with zero.
		
		/* Use - 1 as a marker for categories that haven't yet been selected
		 * this eliminates the need for another array to create and track
		 */
		for (int i = 1; i <= nPlayers; i++)   {
			for (int j = 1; j <= N_CATEGORIES; j++)   {
				scoreArray[i][j] = -1;
			}
		}
		
		/* Change the score categories to 0 from the initialized -1 value as we want score to be 0 when first calculated
		 * 
		 */
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
		
		// Third roll repeat second roll sequence - TODO this could be put in a loop to minimize code duplication
		display.waitForPlayerToSelectDice();
		diceArray = checkSelectedDice(diceArray);
		display.displayDice(diceArray);
		display.printMessage("Select a category for this roll");
		
		int category = display.waitForPlayerToSelectCategory();
		while (scoreArray[i][category] != -1) { 
			display.printMessage("This category is already used. Select another.");
			category = display.waitForPlayerToSelectCategory(); 	
		}
		
		// select category and update score  TODO remove the pass of diceArray as this is now a iVar
		playerScoring(i, category, diceArray);

	}
	
	/* This iterates over an array of length n and "rolls the die" for each element in the array
	 * takes an int an returns an array of newly rolled dice
	 */
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
				
		// check if dice match category and give score if so. Score is zero if not matching the category
		if (checkCategory(dice, category) == true) {
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
	private boolean checkCategory(int[] dice, int category) {
		/* need an array that can hold histogram of numbers 1 - 6 where the index matches the number indicates the number
		 * of die that had the number
		 */
		int[] histoArray = new int[7];   
		
		histoArray = parseDiceArray(dice);
		switch (category)  {
		case ONES: case TWOS: case THREES: case FOURS: case FIVES: case SIXES: case CHANCE:
			return true;
		case THREE_OF_A_KIND:
			return (checkParsedDiceArrayForEqualDice(histoArray, 3) || checkParsedDiceArrayForEqualDice(histoArray, 4) || checkParsedDiceArrayForEqualDice(histoArray, 5));
		case FOUR_OF_A_KIND: 
			return (checkParsedDiceArrayForEqualDice(histoArray, 4) || checkParsedDiceArrayForEqualDice(histoArray, 5));
		case SMALL_STRAIGHT:
			return checkSmallStraight(histoArray);
		case LARGE_STRAIGHT:
			// check that there are no equal dice by returning the not of checking for equal dice
			return !(checkParsedDiceArrayForEqualDice(histoArray, 2) || checkParsedDiceArrayForEqualDice(histoArray, 3) || checkParsedDiceArrayForEqualDice(histoArray, 4) || checkParsedDiceArrayForEqualDice(histoArray, 5));
		case FULL_HOUSE:
			return (checkParsedDiceArrayForEqualDice(histoArray, 2) && checkParsedDiceArrayForEqualDice(histoArray, 3));
		case YAHTZEE:
			return checkParsedDiceArrayForEqualDice(histoArray, 5);
		default:
			return false;
		}
	}

	private boolean checkSmallStraight(int[] array) {
		/* check for a small straight by looking for the pattern 1234 or 2345 or 3456 and return true
		 * as large straights are also small straights, we count these also
		 */
		if ((array[1] >= 1)  && (array[2] >= 1) && (array[3] >= 1) && (array[4] >= 1)) return true;
		if ((array[2] >= 1)  && (array[3] >= 1) && (array[4] >= 1) && (array[5] >= 1)) return true;
		if ((array[3] >= 1)  && (array[4] >= 1) && (array[5] >= 1) && (array[6] >= 1)) return true;
		return false;
	}

	private boolean checkParsedDiceArrayForEqualDice(int[] checkArray, int num) {
		/* check the histogram array for greater than the number of equal dice for the int num. If num = 3 then it is looking for 3 of a kind exactly
		 * 
		 */
		boolean answer = false;
 		for (int i = 1; i < checkArray.length; i++)  {
			if (checkArray[i] == num)  {  answer = true;  }
		}     
		return answer;
	}
	
	private int[] parseDiceArray(int[] dArray)   {
		/*  Convert the dice array to a histogram based on the number of each die
		 * so 3 3 3 2 6 would become 0 0 1 3 0 0 1.
		 * We use an array to 7 to be able to match the number 1-6 numerically. We will ignore element 0 during calculation
		 * So array[1] contains the number of 1's in the dice array. array[2] contains the number of 2's and so forth
		 */
		
		int[] histogramArray = new int[7];

		// iterate over the dice array and increment the histogram array based on the value of the dice array element dArray[i]
		for (int i = 0; i < N_DICE; i++)  {
			histogramArray[dArray[i]] = histogramArray[dArray[i]] + 1;
		}

		return histogramArray;
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
		
		/* calculate who has the most points and display to the screen
		 * 
		 */
		
		int n = 0;    // temp variable for the player with highest score
		
		for (int i = 0; i < nPlayers; i++)  {
			if (scoreArray[i][TOTAL] >= scoreArray[n][TOTAL])  {
				n = i;
			}
			
			int total = scoreArray[n+1][TOTAL];
			display.printMessage("Congratulations " + playerNames[n] + ", you won with a score of " + total);  
		}
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