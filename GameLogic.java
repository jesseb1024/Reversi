import java.util.*;

public class GameLogic implements PlayableLogic {

    private final int BOARD_SIZE = 8;
    final private Disc[][] BOARD;
    private Player PLAYER1;
    private Player PLAYER2;
    private Player CURRENT_PLAYER;
    final private Stack<Move> moveHistory = new Stack<>();
    final private Stack<List<Position>> flippedDiscs = new Stack<>();
    private boolean isFirstPlayerTurn;
    final private List<Position> bombed = new ArrayList<>();
    final private List<Position> bombedCount = new ArrayList<>();
    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, // Up, down, left, right
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // Diagonals
    };

    // Constructor to initialize the game board
    public GameLogic() {
        BOARD = new Disc[BOARD_SIZE][BOARD_SIZE];
    }

    // Clears the game board by setting each position to null
    public void clearBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                BOARD[row][col] = null;
            }
        }
    }

    // Initializes the board with starting discs and sets the current player
    private void initializeBoard() {
        clearBoard();
        int mid = BOARD_SIZE / 2;
        BOARD[mid - 1][mid - 1] = new SimpleDisc(PLAYER1);
        BOARD[mid][mid] = new SimpleDisc(PLAYER1);
        BOARD[mid - 1][mid] = new SimpleDisc(PLAYER2);
        BOARD[mid][mid - 1] = new SimpleDisc(PLAYER2);

        isFirstPlayerTurn = true;
        CURRENT_PLAYER = PLAYER1;
    }

    // Places a disc on the board and handles flipping logic if the move is valid
    @Override
    public boolean locate_disc(Position a, Disc disc) {
        int r = a.row();
        int c = a.col();

        if (!isWithinBounds(r, c) || BOARD[r][c] != null) {
            return false;
        }

        if ("⭕".equals(disc.getType())) {
            if (disc.getOwner().number_of_unflippedable == 0) {
                return false;
            } else {
                disc.getOwner().reduce_unflippedable();
            }
        }

        if ("💣".equals(disc.getType())) {
            if (disc.getOwner().number_of_bombs == 0) {
                return false;
            } else {
                disc.getOwner().reduce_bomb();
            }
        }

        BOARD[r][c] = disc;

        System.out.println("Player " + (disc.getOwner().isPlayerOne ? "1" : "2")
                + " placed a " + disc.getType()
                + " in (" + r + ", " + c + ")");

        int totalFlips = countFlips(a);
        if (totalFlips > 0) {
            flipDiscs(a);
            Move lastMove = new Move(a, disc);
            moveHistory.add(lastMove);
            isFirstPlayerTurn = !isFirstPlayerTurn;
            CURRENT_PLAYER = isFirstPlayerTurn ? PLAYER1 : PLAYER2;
            return true;
        } else {
            BOARD[r][c] = null;
            return false;
        }
    }

    // Flips the discs based on the rules, starting from the specified position
    private void flipDiscs(Position a) {
        int r = a.row();
        int c = a.col();
        List<Position> discsToFlip = new ArrayList<>();

        for (int[] dir : DIRECTIONS) {
            List<Position> temp = new ArrayList<>();
            int newRow = r + dir[0];
            int newCol = c + dir[1];

            while (isWithinBounds(newRow, newCol) && BOARD[newRow][newCol] != null
                    && BOARD[newRow][newCol].getOwner() != CURRENT_PLAYER) {

                if (!"⭕".equals(BOARD[newRow][newCol].getType())) {
                    temp.add(new Position(newRow, newCol));
                }

                if ("💣".equals(BOARD[newRow][newCol].getType())) {
                    Position bombPos = new Position(newRow, newCol);
                    List<Position> bombFlips = handleBombEffect(bombPos, new ArrayList<>());
                    addUniquePositions(bombFlips, discsToFlip);
                    break;
                }

                newRow += dir[0];
                newCol += dir[1];
            }

            if (!temp.isEmpty() && isWithinBounds(newRow, newCol)
                    && BOARD[newRow][newCol] != null
                    && BOARD[newRow][newCol].getOwner() == CURRENT_PLAYER) {
                addUniquePositions(temp, discsToFlip);
            }
        }

        flippedDiscs.add(discsToFlip);

        for (Position pos : discsToFlip) {
            Disc disc = BOARD[pos.row()][pos.col()];
            if (disc != null) {
                disc.setOwner(CURRENT_PLAYER);
                System.out.println("Player " + (CURRENT_PLAYER.isPlayerOne ? "1" : "2")
                        + " flipped the " + disc.getType()
                        + " in (" + pos.row() + ", " + pos.col() + ")");
            }
        }
    }

    // Returns the disc at the specified position
    @Override
    public Disc getDiscAtPosition(Position position) {
        return BOARD[position.row()][position.col()];
    }

    // Returns the size of the board
    @Override
    public int getBoardSize() {
        return BOARD_SIZE;
    }

    // Returns a list of all valid moves for the current player
    @Override
    public List<Position> ValidMoves() {
        List<Position> validMoves = new ArrayList<>();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Position position = new Position(row, col);

                if (BOARD[row][col] == null && countFlips(position) > 0) {
                    validMoves.add(position);
                }
            }
        }
        return validMoves;
    }

    // Counts the number of discs that would be flipped for a given move
    @Override
    public int countFlips(Position a) {
        int r = a.row();
        int c = a.col();

        List<Position> count = new ArrayList<>();

        for (int[] dir : DIRECTIONS) {
            List<Position> temp = new ArrayList<>();
            int newRow = r + dir[0];
            int newCol = c + dir[1];

            while (isWithinBounds(newRow, newCol) && BOARD[newRow][newCol] != null
                    && BOARD[newRow][newCol].getOwner() != CURRENT_PLAYER) {

                if (!"⭕".equals(BOARD[newRow][newCol].getType())) {
                    temp.add(new Position(newRow, newCol));
                }

                if ("💣".equals(BOARD[newRow][newCol].getType())) {
                    Position bombPos = new Position(newRow, newCol);
                    List<Position> bombFlips = handleBombEffect(bombPos, new ArrayList<>());
                    addUniquePositions(bombFlips, temp);
                }

                newRow += dir[0];
                newCol += dir[1];
            }

            if (!temp.isEmpty() && isWithinBounds(newRow, newCol)
                    && BOARD[newRow][newCol] != null
                    && BOARD[newRow][newCol].getOwner() == CURRENT_PLAYER) {
                addUniquePositions(temp, count);
            }
        }

        return count.size();
    }

    // Adds unique positions from source list to target list
    public void addUniquePositions(List<Position> source, List<Position> target) {
        for (Position pos : source) {
            boolean exists = false;
            for (Position existing : target) {
                if (existing.row() == pos.row() && existing.col() == pos.col()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                target.add(pos);
            }
        }
    }

    // Checks if the given row and column are within the board boundaries
    private boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    // Returns the first player
    @Override
    public Player getFirstPlayer() {
        return PLAYER1;
    }

    // Returns the second player
    @Override
    public Player getSecondPlayer() {
        return PLAYER2;
    }

    // Sets the players and initializes the current player
    @Override
    public void setPlayers(Player player1, Player player2) {
        this.PLAYER1 = player1;
        this.PLAYER2 = player2;
        this.CURRENT_PLAYER = player1;
    }

    // Returns whether it is the first player's turn
    @Override
    public boolean isFirstPlayerTurn() {
        return isFirstPlayerTurn;
    }

    // Determines if the game is finished by checking for valid moves
    @Override
    public boolean isGameFinished() {
        if (ValidMoves().isEmpty()) {
            int player1Count = countPlayerDiscs(PLAYER1);
            int player2Count = countPlayerDiscs(PLAYER2);

            if (player1Count > player2Count) {
                PLAYER1.addWin();
                System.out.println("Player 1 wins with " + player1Count + " discs! Player 2 had " + player2Count + " discs.");
            } else if (player2Count > player1Count) {
                PLAYER2.addWin();
                System.out.println("Player 2 wins with " + player2Count + " discs! Player 1 had " + player1Count + " discs.");
            } else {
                System.out.println("It's a tie! Both players have " + player1Count + " discs.");
            }

            return true;
        }

        return false;
    }

    // Counts the number of discs belonging to the specified player
    public int countPlayerDiscs(Player player) {
        int count = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (BOARD[row][col] != null && BOARD[row][col].getOwner() == player) {
                    count++;
                }
            }
        }
        return count;
    }

    // Handles the bomb effect by flipping surrounding discs recursively
    private List<Position> handleBombEffect(Position initialBomb, List<Position> discsToFlip) {
        Queue<Position> bombQueue = new LinkedList<>();
        Set<Position> visited = new HashSet<>(); // To track processed positions
        bombQueue.add(initialBomb);
        visited.add(initialBomb); // Mark the initial bomb as processed

        while (!bombQueue.isEmpty()) {
            Position bombPosition = bombQueue.poll();
            int r = bombPosition.row();
            int c = bombPosition.col();

            // Add the current bomb position to discsToFlip if not already added
            if (!discsToFlip.contains(bombPosition)) {
                discsToFlip.add(bombPosition);
            }

            // Check all adjacent positions for further bomb effects or discs to flip
            for (int[] dir : DIRECTIONS) {
                int newRow = r + dir[0];
                int newCol = c + dir[1];
                Position adjacent = new Position(newRow, newCol);

                if (isWithinBounds(newRow, newCol) && !visited.contains(adjacent)) {
                    Disc currentDisc = BOARD[newRow][newCol];

                    // If it's a bomb, add it to the queue for further processing
                    if (currentDisc != null && "💣".equals(currentDisc.getType())) {
                        bombQueue.add(adjacent);
                        visited.add(adjacent); // Mark this bomb as visited
                    }

                    // If it's a flippable disc, add it to the discsToFlip list
                    if (currentDisc != null && !"⭕".equals(currentDisc.getType())
                            && currentDisc.getOwner() != CURRENT_PLAYER
                            && !discsToFlip.contains(adjacent)) {
                        discsToFlip.add(adjacent);
                    }
                }
            }
        }

        return discsToFlip; // Return all affected positions
    }

    // Resets the game state to start a new game
    @Override
    public void reset() {
        initializeBoard();
        CURRENT_PLAYER = PLAYER1;
        isFirstPlayerTurn = true;
        PLAYER1.reset_bombs_and_unflippedable();
        PLAYER2.reset_bombs_and_unflippedable();
        flippedDiscs.clear();
        moveHistory.clear();
        System.out.println("Game has been reset");
    }

    // Undoes the last move made, reverting the board state
    @Override
    public void undoLastMove() {
        if (PLAYER1.isHuman() && PLAYER2.isHuman()) {
            if (!moveHistory.isEmpty()) {
                System.out.println("Undoing last move:");

                Move lastMove = moveHistory.pop();
                Position pos = lastMove.position();

                System.out.println("\tUndo: removing " + lastMove.disc().getType()
                        + " from (" + pos.row() + ", " + pos.col() + ")");

                BOARD[pos.row()][pos.col()] = null;

                if (!flippedDiscs.isEmpty()) {
                    List<Position> lastPosition = flippedDiscs.pop();
                    for (Position posToFlip : lastPosition) {
                        if (BOARD[posToFlip.row()][posToFlip.col()] != null) {
                            Player previousOwner = (BOARD[posToFlip.row()][posToFlip.col()].getOwner() == PLAYER1) ? PLAYER2 : PLAYER1;
                            BOARD[posToFlip.row()][posToFlip.col()].setOwner(previousOwner);

                            System.out.println("\tUndo: flipping back " + BOARD[posToFlip.row()][posToFlip.col()].getType()
                                    + " in (" + posToFlip.row() + ", " + posToFlip.col() + ")");
                        }
                    }
                } else {
                    System.out.println("\tNo previous move available to undo.");
                }

                if ("💣".equals(lastMove.disc().getType())) {
                    lastMove.disc().getOwner().number_of_bombs++;
                } else if ("⭕".equals(lastMove.disc().getType())) {
                    lastMove.disc().getOwner().number_of_unflippedable++;
                }

                isFirstPlayerTurn = !isFirstPlayerTurn;
                CURRENT_PLAYER = isFirstPlayerTurn ? PLAYER1 : PLAYER2;

                bombed.clear();
                bombedCount.clear();

                System.out.println();

            } else {
                System.out.println("Undoing last move:");
                System.out.println("\tNo previous move available to undo.\n");
            }
        }
    }
}
