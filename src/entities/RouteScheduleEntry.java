package entities;

import java.time.LocalTime;

/**
 * Predstavuje jednu polozku v trase autobusu. Obsahuje ulicu a cas kedy z nej autobus oddide.
 * @author Dominik Vagala (xvagal00)
 * @author Jakub Vinš (xvinsj00)
 */
public class RouteScheduleEntry {
    private BusStop busStop;
    private LocalTime departureTime;
    private LocalTime nonDelayedDepartureTime;

    private boolean isDelayed = false;

    public RouteScheduleEntry(BusStop busStop, LocalTime departureTime, LocalTime nonDelayedDepartureTime) {
        this.busStop = busStop;
        this.departureTime = departureTime;
        this.nonDelayedDepartureTime = nonDelayedDepartureTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public BusStop getBusStop() {
        return busStop;
    }

    public boolean isDelayed() {
        return isDelayed;
    }

    public void setDelayed(boolean delayed) {
        isDelayed = delayed;
    }

    public LocalTime getNonDelayedDepartureTime() {
        return nonDelayedDepartureTime;
    }
}
