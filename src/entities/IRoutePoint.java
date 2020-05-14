package entities;

/**
 * Predstavuje predstavuje autobusovu zastavku, alebo zlom ulice. Kazdy takyto bod ma definovanu ulicu ktora
 * z neho vychadza.
 * @author Dominik Vagala (xvagal00)
 * @author Jakub Vinš (xvinsj00)
 */
public interface IRoutePoint{
    int getX();
    int getY();
    void setX(int x);
    void setY(int y);
    Street getStreetAfter();
    void setStreetAfter(Street streetAfter);
}
