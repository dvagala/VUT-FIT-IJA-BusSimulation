package entities;


/**
 * Predstavuje bod na mape, alebo "route point" ktory predstavuje autobusovu zastavku, alebo zlom ulice.
 * @author Dominik Vagala (xvagal00)
 * @author Jakub Vinš (xvinsj00)
 */
public class Coordinate implements IRoutePoint {
    private int x;
    private int y;

    private Street streetAfter = null;

    public Coordinate() {
    }

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }

    public Street getStreetAfter() {
        return streetAfter;
    }
    public void setStreetAfter(Street streetAfter) {
        this.streetAfter = streetAfter;
    }
}
