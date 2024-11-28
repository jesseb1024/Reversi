import java.util.Comparator;

public class GreedyAI extends AIPlayer {
    private final boolean player;

    public GreedyAI(boolean isPlayerOne) {
        super(isPlayerOne);
        player = isPlayerOne;
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        Move move;
        if (player) {
            move = new Move(getBestPosition(gameStatus), new SimpleDisc(gameStatus.getSecondPlayer()));
        } else
            move = new Move(getBestPosition(gameStatus), new SimpleDisc(gameStatus.getSecondPlayer()));
        return move;
    }

    private Position getBestPosition(PlayableLogic gameStatus) {
        Position bestPosition = gameStatus.ValidMoves().get(0);
        colComparator colComparator = new colComparator();
        rowComparator rowComparator = new rowComparator();
        for (Position validMove : gameStatus.ValidMoves()) {
            if (gameStatus.countFlips(validMove) > gameStatus.countFlips(bestPosition)) {
                bestPosition = validMove;
            } else if (gameStatus.countFlips(validMove) == gameStatus.countFlips(bestPosition)) {
                if (colComparator.compare(validMove, bestPosition) > 0) {
                    bestPosition = validMove;
                } else if (colComparator.compare(validMove, bestPosition) == 0) {
                    if (rowComparator.compare(validMove, bestPosition) > 0) {
                        bestPosition = validMove;
                    }
                }
            }
        }
        return bestPosition;
    }
}
class rowComparator implements Comparator<Position>{
    public int compare(Position p1, Position p2){
        return Integer.compare(p1.row(), p2.row());
    }
}
class colComparator implements Comparator<Position>{
    public int compare(Position p1, Position p2){
        return Integer.compare(p1.col(), p2.col());
    }
}