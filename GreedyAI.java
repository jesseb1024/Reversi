import java.util.Comparator;

public class GreedyAI extends AIPlayer {
    // Field to track whether this player is Player One or not
    private final boolean player;

    // Constructor to initialize the AI player, indicating whether it's Player One
    public GreedyAI(boolean isPlayerOne) {
        super(isPlayerOne);
        player = isPlayerOne;
    }

    // Method to make a move based on the current game state
    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        Move move;
        if (player) {
            // If it's Player One, make a move at the best position with a SimpleDisc
            move = new Move(getBestPosition(gameStatus), new SimpleDisc(gameStatus.getSecondPlayer()));
        } else {
            // If it's Player Two, similarly make a move at the best position with a SimpleDisc
            move = new Move(getBestPosition(gameStatus), new SimpleDisc(gameStatus.getSecondPlayer()));
        }
        return move;
    }

    // Method to get the best possible position based on the number of discs that can be flipped
    private Position getBestPosition(PlayableLogic gameStatus) {
        Position bestPosition = gameStatus.ValidMoves().get(0); // Start with the first valid move
        colComparator colComparator = new colComparator(); // Comparator for comparing columns
        rowComparator rowComparator = new rowComparator(); // Comparator for comparing rows

        // Iterate through all valid moves to find the best position
        for (Position validMove : gameStatus.ValidMoves()) {
            if (gameStatus.countFlips(validMove) > gameStatus.countFlips(bestPosition)) {
                // Update the best position if the current move results in more flips
                bestPosition = validMove;
            } else if (gameStatus.countFlips(validMove) == gameStatus.countFlips(bestPosition)) {
                // If two moves result in the same number of flips, compare columns
                if (colComparator.compare(validMove, bestPosition) > 0) {
                    bestPosition = validMove;
                } else if (colComparator.compare(validMove, bestPosition) == 0) {
                    // If columns are equal, compare rows
                    if (rowComparator.compare(validMove, bestPosition) > 0) {
                        bestPosition = validMove;
                    }
                }
            }
        }
        return bestPosition;
    }
}

// Comparator for comparing rows of two positions
class rowComparator implements Comparator<Position> {
    public int compare(Position p1, Position p2) {
        return Integer.compare(p1.row(), p2.row());
    }
}

// Comparator for comparing columns of two positions
class colComparator implements Comparator<Position> {
    public int compare(Position p1, Position p2) {
        return Integer.compare(p1.col(), p2.col());
    }
}
