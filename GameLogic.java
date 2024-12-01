import java.util.*;

public class GameLogic implements PlayableLogic {

    private final int BOARD_SIZE = 8;
    final private Disc[][] BOARD;
    private Player PLAYER1;
    private Player PLAYER2;
    private Player CURRENT_PLAYER;
    final private Stack<Move> moveHistory = new Stack<>();
    final private Stack<List<Position>> flipedDiscs = new Stack<>();
    private boolean isFirstPlayerTurn;
    final private List<Position> bombed = new ArrayList<>();
    final private List<Position> bombedCount = new ArrayList<>();
    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, // Up, down, left, right
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // Diagonals
    };


    public GameLogic() {
        BOARD = new Disc[BOARD_SIZE][BOARD_SIZE];
    }

    /** Clears the game board by setting each position to null **/
    public void clearBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                BOARD[row][col] = null;
            }
        }
    }

    /** Initializes the board by placing initial discs and setting the current player **/
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

    @Override
    public boolean locate_disc(Position a, Disc disc) {
        int r = a.row();
        int c = a.col();

        // Check if the position is within bounds and is unoccupied
        if (!isWithinBounds(r, c) || BOARD[r][c] != null) {
            return false;
        }

        // Check and reduce the unflippable disc count if the disc type is "⭕" //implement instanceof
        if ("⭕".equals(disc.getType())) {
            if (disc.getOwner().number_of_unflippedable == 0) {
                return false; // No unflippable discs left
            } else {
                disc.getOwner().reduce_unflippedable();
            }
        }

        // Check and reduce the bomb count if the disc type is "💣"
        if ("\uD83D\uDCA3".equals(disc.getType())) {
            if (disc.getOwner().number_of_bombs == 0) {
                return false; // No bombs left
            } else {
                disc.getOwner().reduce_bomb();
            }
        }

        BOARD[r][c] = disc;

        System.out.println("Player " + (disc.getOwner().isPlayerOne ? "1" : "2")
                + " placed a " + disc.getType()
                + " in (" + r + ", " + c + ")");

        // Count total flips to determine if the move is valid
        int totalFlips = countFlips(a);
        if (totalFlips > 0) {

            // Flip the discs as the move is valid
            flipDiscs(a);

            // Save the current valid move
            Move lastMove = new Move(a, disc);
            moveHistory.add(lastMove);
            //printGameHistory();

            // Toggle turn to the next player
            isFirstPlayerTurn = !isFirstPlayerTurn;
            CURRENT_PLAYER = isFirstPlayerTurn ? PLAYER1 : PLAYER2;
            return true;
        } else {
            // Remove the disc as no valid flips were found
            BOARD[r][c] = null;
            return false;
        }
    }


    /**
     * Flips the discs on the board based on the position where the current player places a disc.
     * Parameters:
     * - a: Position where the disc is placed.
     **/
    private void flipDiscs(Position a) {
        int r = a.row();
        int c = a.col();

        List<Position> discsToFlip = new ArrayList<>(); // Keep track of potential discs to flip

        // Iterate through each direction to check for discs to flip
        for (int[] dir : DIRECTIONS) {
            int newRow = r + dir[0];
            int newCol = c + dir[1];

            List<Position> temp = new ArrayList<>();

            while (isWithinBounds(newRow, newCol) && BOARD[newRow][newCol] != null
                    && BOARD[newRow][newCol].getOwner() != CURRENT_PLAYER) {

                if (!(BOARD[newRow][newCol].getType().equals("⭕"))) {
                    temp.add(new Position(newRow, newCol));
                }

                if (BOARD[newRow][newCol].getType().equals("\uD83D\uDCA3")) { // Bomb handling
                    Position pos = new Position(newRow, newCol);
                    temp = handleBombEffect(pos, temp);
                    bombed.clear();
                }

                newRow += dir[0];
                newCol += dir[1];
            }

            // Check if the end of the sequence has a disc owned by the current player
            if (!temp.isEmpty() && isWithinBounds(newRow, newCol)
                    && BOARD[newRow][newCol] != null
                    && BOARD[newRow][newCol].getOwner() == CURRENT_PLAYER) {

                addUniquePositions(temp, discsToFlip);
            }
        }

        flipedDiscs.add(discsToFlip);

        // Flip all collected discs to the current player's
        for (Position pos : discsToFlip) {
            BOARD[pos.row()][pos.col()].setOwner(CURRENT_PLAYER);

            System.out.println("Player " + (CURRENT_PLAYER.isPlayerOne ? "1" : "2")
                    + " flipped the " + BOARD[pos.row()][pos.col()].getType()
                    + " in (" + pos.row() + ", " + pos.col() + ")");
        }
        System.out.println();
    }


    @Override
    public Disc getDiscAtPosition(Position position) {
        return BOARD[position.row()][position.col()];
    }

    @Override
    public int getBoardSize() {
        return BOARD_SIZE;
    }

    @Override
    public List<Position> ValidMoves() {
        List<Position> validMoves = new ArrayList<>(); // List to store all valid positions for the current player.

        // Loop through each row of the board.
        for (int row = 0; row < BOARD_SIZE; row++) {
            // Loop through each column of the board.
            for (int col = 0; col < BOARD_SIZE; col++) {
                Position position = new Position(row, col); // Create a position object for the current cell.

                // Check if the position is empty and the move flips at least one opponent's disc.
                if (BOARD[row][col] == null && countFlips(position) > 0) {
                    validMoves.add(position); // Add position to the list of valid moves.
                }
            }
        }
        return validMoves; // Return the list of valid moves.
    }


    @Override
    public int countFlips(Position a) {
        int r = a.row(); // Row index of the given position.
        int c = a.col(); // Column index of the given position.

        List<Position> count = new ArrayList<>(); // List to store all positions that can be flipped.

        // Iterate through each direction.
        for (int[] dir : DIRECTIONS) {
            List<Position> temp = new ArrayList<>(); // Temporary list to store positions in the current direction.
            int newRow = r + dir[0]; // Calculate the next row in the direction.
            int newCol = c + dir[1]; // Calculate the next column in the direction.

            // Traverse in the current direction until reaching the boundary or an invalid disc.
            while (isWithinBounds(newRow, newCol) && BOARD[newRow][newCol] != null
                    && BOARD[newRow][newCol].getOwner() != CURRENT_PLAYER) {

                if (!(BOARD[newRow][newCol].getType().equals("⭕"))) {
                    temp.add(new Position(newRow, newCol)); // Add positions of opponent's discs.
                }

                // Handle bomb effect and collect affected positions.
                if (BOARD[newRow][newCol].getType().equals("\uD83D\uDCA3")) {
                    Position pos = new Position(newRow, newCol);
                    List<Position> bombList = handleBombEffect(pos, new ArrayList<>());
                    addUniquePositions(bombList, temp); // Add bomb-affected positions.
                }

                // Move further in the current direction.
                newRow += dir[0];
                newCol += dir[1];
            }

            // Check if the current sequence ends with a disc owned by the current player.
            if ((!temp.isEmpty()) && isWithinBounds(newRow, newCol)
                    && BOARD[newRow][newCol] != null
                    && BOARD[newRow][newCol].getOwner() == CURRENT_PLAYER) {
                addUniquePositions(temp, count); // Add all valid positions from temp to the main count list.
            }
        }

        return count.size();
    }


    /**
     * Adds unique positions from the source list to the target list, ensuring no duplicates.
     * Parameters:
     * - source: List<Position> containing new positions to add.
     * - target: List<Position> to which unique positions are added.
     **/
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

    /**
     * Checks if a given row and column are within the board boundaries.
     * Parameters:
     * - row: int ow index.
     * - col: int column index.
     * Returns: boolean indicating if the position is within bounds.
     **/
    private boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    @Override
    public Player getFirstPlayer() {
        return PLAYER1;
    }

    @Override
    public Player getSecondPlayer() {
        return PLAYER2;
    }

    @Override
    public void setPlayers(Player player1, Player player2) {
        this.PLAYER1 = player1;
        this.PLAYER2 = player2;
        this.CURRENT_PLAYER = player1;
    }

    @Override
    public boolean isFirstPlayerTurn() {
        return isFirstPlayerTurn;
    }

    @Override
    public boolean isGameFinished() {
        if (ValidMoves().isEmpty()) {
            int player1Count = countPlayerDiscs(PLAYER1);
            int player2Count = countPlayerDiscs(PLAYER2);

            // Determine the winner based on the number of discs
            if (player1Count > player2Count) {
                PLAYER1.addWin();
                System.out.println("Player 1 wins with " + player1Count + " discs! Player 2 had " + player2Count + " discs.");
            } else if (player2Count > player1Count) {
                PLAYER2.addWin();
                System.out.println("Player 2 wins with " + player2Count + " discs! Player 1 had " + player1Count + " discs.");
            } else {
                System.out.println("It's a tie! Both players have " + player1Count + " discs.");
            }

            return true; // Game is finished
        }

        return false; // Game is not finished
    }

    /**
     * Counts the number of discs belonging to a specific player on the board.
     * Parameters:
     * - player: Player for whom the disc count is calculated.
     * Returns: the number of discs owned by the player.
     **/
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
    private void processBomb(Position pos, List<Position> discsToFlip) {
        if (!isDuplicate(pos)) {
            handleBombEffect(pos, discsToFlip); // Recursive call for chained bombs.
        }
    }


    /**
     * Handles the bomb effect by flipping discs around a bomb position.
     * Parameters:
     * - a: Position of the bomb on the board.
     * - discsToFlip: List<Position> of discs affected by the bomb.
     * Returns: List<Position> of all positions flipped due to the bomb effect.
     **/
    private List<Position> handleBombEffect(Position initialBomb, List<Position> discsToFlip) {
        Queue<Position> bombQueue = new LinkedList<>();
        bombQueue.add(initialBomb);
        bombed.add(initialBomb); // Mark the initial bomb

        while (!bombQueue.isEmpty()) {
            Position bombPosition = bombQueue.poll();
            int r = bombPosition.row();
            int c = bombPosition.col();

            for (int[] dir : DIRECTIONS) {
                int newRow = r + dir[0];
                int newCol = c + dir[1];

                if (isWithinBounds(newRow, newCol)) {
                    Disc currentDisc = BOARD[newRow][newCol];
                    if (currentDisc != null && currentDisc.getOwner() != CURRENT_PLAYER) {
                        Position newPos = new Position(newRow, newCol);
                        if ("\uD83D\uDCA3".equals(currentDisc.getType()) && !isDuplicate(newPos)) {
                            bombQueue.add(newPos); // Add new bomb to the queue
                            bombed.add(newPos);
                        } else if (!"⭕".equals(currentDisc.getType())) {
                            discsToFlip.add(newPos);
                        }
                    }
                }
            }
        }
        return discsToFlip;
    }



    /**
     * Checks if a position already exists in the bombed list.
     * Parameters:
     * - newPosition: Position to check for duplication.
     * Returns: boolean indicating if the position is a duplicate.
     **/
    public boolean isDuplicate(Position newPosition) {
        for (Position pos : bombed) {
            // Check if the row and col of the newPosition match any existing position in the bomb list
            if (pos.row() == newPosition.row() && pos.col() == newPosition.col()) {
                return true; // Duplicate found
            }
        }
        return false;
    }

    @Override
    public void reset() {
        initializeBoard();
        CURRENT_PLAYER = PLAYER1;
        isFirstPlayerTurn = true;
        PLAYER1.reset_bombs_and_unflippedable();
        PLAYER2.reset_bombs_and_unflippedable();
        flipedDiscs.clear();
        moveHistory.clear();
        System.out.println("Game has been reset");
    }

    @Override
    public void undoLastMove() {
        if (PLAYER1.isHuman() && PLAYER2.isHuman()) {
            if (!moveHistory.isEmpty()) {
                System.out.println("Undoing last move:");

                // Retrieve and remove the last move from the stack
                Move lastMove = moveHistory.pop();
                Position pos = lastMove.position();

                System.out.println("\tUndo: removing " + lastMove.disc().getType()
                        + " from (" + pos.row() + ", " + pos.col() + ")");

                // Remove the disc placed during the last move
                BOARD[pos.row()][pos.col()] = null;

                // Check if any flipped discs were recorded for the last move
                if (!flipedDiscs.isEmpty()) {
                    List<Position> lastPosition = flipedDiscs.pop();
                    for (Position posToFlip : lastPosition) {
                        // Ensure the position has a disc before reverting ownership
                        if (BOARD[posToFlip.row()][posToFlip.col()] != null) {
                            // Revert ownership to the opposite player
                            Player previousOwner = (BOARD[posToFlip.row()][posToFlip.col()].getOwner() == PLAYER1) ? PLAYER2 : PLAYER1;
                            BOARD[posToFlip.row()][posToFlip.col()].setOwner(previousOwner);

                            System.out.println("\tUndo: flipping back " + BOARD[posToFlip.row()][posToFlip.col()].getType()
                                    + " in (" + posToFlip.row() + ", " + posToFlip.col() + ")");
                        }
                    }
                } else {
                    System.out.println("\tNo previous move available to undo.");
                }

                // Restore bomb and unflippable counts if the last move involved special discs
                if ("\uD83D\uDCA3".equals(lastMove.disc().getType())) {
                    lastMove.disc().getOwner().number_of_bombs++;
                } else if ("⭕".equals(lastMove.disc().getType())) {
                    lastMove.disc().getOwner().number_of_unflippedable++;
                }

                // Toggle the turn back to the previous player
                isFirstPlayerTurn = !isFirstPlayerTurn;
                CURRENT_PLAYER = isFirstPlayerTurn ? PLAYER1 : PLAYER2;

                // Clear any temporary lists related to bomb effects
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