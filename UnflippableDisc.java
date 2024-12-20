public class UnflippableDisc implements Disc {
    private Player owner;


    public UnflippableDisc(Player owner) {
        this.owner = owner;
    }
    @Override
    public Player getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(Player owner) {
        this.owner = owner;
    }
    @Override
    public String getType() {
        return "⭕";
    }
}
