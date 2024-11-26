public class Move {
    private final Position position;
    private final Disc disc;

    // Constructor
    public Move(Position position, Disc disc) {
        this.position = position;
        this.disc = disc;
    }

    // Getters
    public Position position() {
        return position;
    }

    public Disc disc() {
        return disc;
    }

    // Optional: toString for debugging
    @Override
    public String toString() {
        return "Move{" +
                "position=" + position +
                ", disc=" + disc +
                '}';
    }
}
