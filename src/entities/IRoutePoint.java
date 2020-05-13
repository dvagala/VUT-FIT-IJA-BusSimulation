package entities;

//
public interface IRoutePoint{
    int getX();
    int getY();
    void setX(int x);
    void setY(int y);
    Street getStreetAfter();
    void setStreetAfter(Street streetAfter);
}
