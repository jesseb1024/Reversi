import java.util.List;

public class RandomAI extends AIPlayer{
    private final boolean player;

    public RandomAI(boolean isPlayerOne){
        super(isPlayerOne);
        player = isPlayerOne;
    }
    @Override
    public Move makeMove(PlayableLogic gameStatus){
        Move move;
        if (player){
            move = new Move(getRandomPosition(gameStatus.ValidMoves()),getRandomDisc(gameStatus.getFirstPlayer()));
        }
        else
            move=new Move(getRandomPosition(gameStatus.ValidMoves()),getRandomDisc(gameStatus.getFirstPlayer()));
        return move;
    }
    private Disc getRandomDisc(Player player){
        int random = (int)(Math.random()*3);
        Disc disc;
        if (random == 1) disc = new SimpleDisc(player);
        else if (random == 2) disc = new BombDisc(player);
        else disc = new UnflippableDisc(player);
        return disc;
    }
    private Position getRandomPosition(List<Position> positions){
        int randomIndex =(int)(Math.random()*positions.size());
        return positions.get(randomIndex);
    }
}