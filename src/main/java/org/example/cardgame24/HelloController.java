package org.example.cardgame24;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class HelloController {

    // These ImageViews display the four cards in the game.
    @FXML
    private ImageView card1Image;

    @FXML
    private ImageView card2Image;

    @FXML
    private ImageView card3Image;

    @FXML
    private ImageView card4Image;

    // Lets the player enter an expression to check.
    @FXML
    private TextField expressionField;

    // Stores the values of the four cards currently displayed.
    private int[] cardValues = new int[4];

    // Deals four cards.
    @FXML
    private void initialize() {
        dealCards();
    }

    // Creates a new shuffled deck, deals and displays four.
    private void dealCards() {

        // Creates a list of the full deck
        List<String> deck = new ArrayList<>();

        String[] suits = {"clubs", "diamonds", "hearts", "spades"};

        String[] ranks = {"ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king"};

        // Combines each rank and suit to create the card filenames.
        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(rank + "_of_" + suit + ".png");
            }
        }

        // Shuffles the deck so the cards are random.
        Collections.shuffle(deck);

        // Keeps the ImageViews together so each card can be updated.
        ImageView[] cardViews = {card1Image, card2Image, card3Image, card4Image};

        // Uses the first four cards from the shuffled deck.
        for (int i = 0; i < cardViews.length; i++) {

            String cardFileName = deck.get(i);

            // Extracts the rank from the filename.
            String rank = cardFileName.substring(0, cardFileName.indexOf("_of_"));

            // Converts face cards into their value.
            if (rank.equals("ace")) {
                cardValues[i] = 1;
            } else if (rank.equals("jack")) {
                cardValues[i] = 11;
            } else if (rank.equals("queen")) {
                cardValues[i] = 12;
            } else if (rank.equals("king")) {
                cardValues[i] = 13;
            } else {
                // Number cards use their face value.
                cardValues[i] = Integer.parseInt(rank);
            }

            // Loads the card image from the resources folder.
            String imagePath = "/cards/" + cardFileName;

            Image card = new Image(getClass().getResource(imagePath).toExternalForm());

            // Displays the card in its own imageView.
            cardViews[i].setImage(card);
        }

        // Checks that the cards are printed correctly.
        System.out.println("Card values: " + java.util.Arrays.toString(cardValues));
    }

    // This button is currently a placeholder; no instructions from client.
    @FXML
    private void onFindSolution() {
        showMessage("The Find a Solution feature is coming soon!");
    }

    // Deals four new cards when the player clicks Refresh.
    @FXML
    private void onRefresh() {
        dealCards();
    }

    // Checks the player's expression when Verify is clicked.
    @FXML
    private void onVerify() {

        // Gets the expression and remove extra spaces at the beginning and end.
        String expression = expressionField.getText().trim();

        // Makes sure the player entered something.
        if (expression.isEmpty()) {
            showMessage("Please enter an expression.");
            return;
        }

        try {
            // Checks that the expression uses the four displayed card values.
            if (!usesAllCards(expression)) {
                showMessage("Your expression must use each card value once.");
                return;
            }

            // Calculates the result of the expression.
            double result = evaluateExpression(expression);

            // Allows a small difference to account for decimal calculations.
            if (Math.abs(result - 24.0) < 0.000001) {
                showMessage("Correct! Your expression equals 24!");
            } else {
                showMessage("Your expression equals " + result + ", not 24.");
            }

        } catch (IllegalArgumentException e) {
            // Displays an error if the expression cannot be evaluated.
            showMessage("Invalid expression. Check your numbers and operators.");
        }
    }

    // Checks whether the expression contains all four card values exactly once.
    private boolean usesAllCards(String expression) {

        List<Integer> enteredNumbers = new ArrayList<>();

        // Splits the expression wherever an operator or parenthesis appears.
        String[] tokens = expression.split("[+*/()\\s-]+");

        // Converts the numbers in the expression into integers.
        for (String token : tokens) {
            if (!token.isEmpty()) {
                try {
                    enteredNumbers.add(Integer.parseInt(token));
                } catch (NumberFormatException e) {
                    // Reject the expression if a number cannot be read.
                    return false;
                }
            }
        }

        // The expression must contain exactly four numbers.
        if (enteredNumbers.size() != 4) {
            return false;
        }

        // Copies the card values so we can compare them with the entered numbers.
        List<Integer> remainingCards = new ArrayList<>();

        for (int value : cardValues) {
            remainingCards.add(value);
        }

        // Removes each entered number from the list of card values.
        // If a number is missing or used too many times, rejects the expression.
        for (int number : enteredNumbers) {
            if (!remainingCards.remove(Integer.valueOf(number))) {
                return false;
            }
        }

        // If all card values were matched, the list should now be empty.
        return remainingCards.isEmpty();
    }

    // Evaluates an arithmetic expression containing numbers and operators.
    private double evaluateExpression(String expression) {

        // One stack stores numbers and the other stores operators.
        Deque<Double> numbers = new ArrayDeque<>();
        Deque<Character> operators = new ArrayDeque<>();

        int i = 0;

        // Reads the expression one character at a time.
        while (i < expression.length()) {

            char current = expression.charAt(i);

            // Ignores spaces between numbers and operators.
            if (Character.isWhitespace(current)) {
                i++;
                continue;
            }

            // Reads a complete number, including a decimal if present.
            if (Character.isDigit(current) || current == '.') {

                int start = i;

                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    i++;
                }

                // Converts the number from text to a double and store it.
                numbers.push(Double.parseDouble(expression.substring(start, i)));

                continue;
            }

            // Stores an opening parenthesis until its matching closing parenthesis.
            if (current == '(') {
                operators.push(current);

            } else if (current == ')') {

                // Finishes the operations inside the parentheses first.
                while (!operators.isEmpty() && operators.peek() != '(') {
                    applyOperation(numbers, operators.pop());
                }

                // A closing parenthesis must have a matching opening parenthesis.
                if (operators.isEmpty()) {
                    throw new IllegalArgumentException();
                }

                // Removes the opening parenthesis after evaluating its contents.
                operators.pop();

            } else if (current == '+' || current == '-' || current == '*' || current == '/') {

                // Evaluates higher or equal precedence operations first.
                while (!operators.isEmpty() && operators.peek() != '(' && precedence(operators.peek()) >= precedence(current)) {
                    applyOperation(numbers, operators.pop());
                }

                // Stores the current operator for later evaluation.
                operators.push(current);

            } else {
                // Rejects characters that are not supported by the game.
                throw new IllegalArgumentException();
            }

            i++;
        }

        // Evaluates any operations that are still waiting in the stack.
        while (!operators.isEmpty()) {

            // An opening parenthesis left over means the expression is incomplete.
            if (operators.peek() == '(') {
                throw new IllegalArgumentException();
            }

            applyOperation(numbers, operators.pop());
        }

        // A valid expression should have exactly one result.
        if (numbers.size() != 1) {
            throw new IllegalArgumentException();
        }

        return numbers.pop();
    }

    // Follows PEMDAS order of operations.
    private int precedence(char operator) {

        if (operator == '+' || operator == '-') {
            return 1;
        }

        if (operator == '*' || operator == '/') {
            return 2;
        }

        return 0;
    }

    // Performs one arithmetic operation using the top two numbers in the stack.
    private void applyOperation(Deque<Double> numbers, char operator) {

        // An operation needs two numbers.
        if (numbers.size() < 2) {
            throw new IllegalArgumentException();
        }

        // The second number is removed first because it is the right operand.
        double right = numbers.pop();
        double left = numbers.pop();

        double result;

        // Performs the operation using the two numbers.
        switch (operator) {

            case '+':
                result = left + right;
                break;

            case '-':
                result = left - right;
                break;

            case '*':
                result = left * right;
                break;

            case '/':
                // Division by zero is not allowed.
                if (right == 0) {
                    throw new IllegalArgumentException();
                }

                result = left / right;
                break;

            default:
                // Rejects any operator that is not supported.
                throw new IllegalArgumentException();
        }

        // Stores the result so it can be used in the next operation.
        numbers.push(result);
    }

    // Displays a dialog box with a message for the player.
    private void showMessage(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Card Game 24");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}