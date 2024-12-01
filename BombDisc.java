public class BombDisc implements Disc {
    private Player owner;


    public BombDisc(Player owner) {
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
        return "\uD83D\uDCA3";
    }
}
