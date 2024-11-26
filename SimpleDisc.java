public class SimpleDisc implements Disc {
    private String color;
    private Player owner;

    // Constructor to initialize a disc with a specific owner
    public SimpleDisc(Player owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        this.owner = owner;
    }

    // Default constructor
    public SimpleDisc() {
        throw new IllegalArgumentException("Owner must be specified when creating a disc");

    }

    @Override
    public Player getOwner() {
        if (owner == null) {
            throw new IllegalStateException("Owner is not set for this disc");
        }
        return owner;
    }

    @Override
    public void setOwner(Player owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        this.owner = owner;
    }

    @Override
    public String getType() {
        return "⬤"; // Regular disc representation
    }

    // Get the color of the disc
    public String getColor() {
        if (color == null) {
            return "default"; // Return a default color if none is set
        }
        return color;
    }

    // Set the color of the disc
    public void setColor(String color) {
        if (color == null || color.isEmpty()) {
            throw new IllegalArgumentException("Color cannot be null or empty");
        }
        this.color = color;
    }

    @Override
    public String toString() {
        return "SimpleDisc{" +
                "color='" + color + '\'' +
                ", owner=" + (owner != null ? (owner.isPlayerOne() ? "Player 1" : "Player 2") : "No Owner") +
                '}';
    }
}
