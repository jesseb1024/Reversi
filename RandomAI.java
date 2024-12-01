import java.util.List;

public class RandomAI extends AIPlayer{
    // Field to track whether this player is Player One or not
    private final boolean player;

    // Constructor to initialize the AI player, indicating whether it's Player One
    public RandomAI(boolean isPlayerOne){
        super(isPlayerOne);
        player = isPlayerOne;
    }

    // Method to make a move based on the current game state
    @Override
    public Move makeMove(PlayableLogic gameStatus){
        Move move;
        if (player){
            // If it's Player One, make a move with a random position and a random disc
            move = new Move(getRandomPosition(gameStatus.ValidMoves()),getRandomDisc(gameStatus.getFirstPlayer()));
        }
        else
            // If it's Player Two, similarly make a move with a random position and disc
            move = new Move(getRandomPosition(gameStatus.ValidMoves()),getRandomDisc(gameStatus.getFirstPlayer()));
        return move;
    }

    // Method to get a random disc, choosing from SimpleDisc, BombDisc, or UnflippableDisc
    private Disc getRandomDisc(Player player){
        int random = (int)(Math.random()*3); // Generate a random number between 0 and 2
        Disc disc;
        if (random == 1)
            disc = new SimpleDisc(player); // If random is 1, create a SimpleDisc
        else if (random == 2)
            disc = new BombDisc(player); // If random is 2, create a BombDisc
        else
            disc = new UnflippableDisc(player); // If random is 0, create an UnflippableDisc
        return disc;
    }

    // Method to get a random position from the list of valid positions
    private Position getRandomPosition(List<Position> positions){
        int randomIndex = (int)(Math.random() * positions.size()); // Generate a random index within bounds
        return positions.get(randomIndex); // Return the position at the random index
    }
}
