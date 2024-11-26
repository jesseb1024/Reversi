public class Position {
    private final int row;
    private final int col;

    // Constructor
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // Getters
    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    // Override equals and hashCode for comparisons and collections
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    // Optional: toString for debugging
    @Override
    public String toString() {
        return "Position(" + row + ", " + col + ")";
    }
}
