package entities;

import java.time.LocalTime;

public class RouteScheduleEntry {
    private BusStop busStop;
    private LocalTime departureTime;

    public RouteScheduleEntry(BusStop busStop, LocalTime departureTime) {
        this.busStop = busStop;
        this.departureTime = departureTime;
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

    public void setBusStop(BusStop busStop) {
        this.busStop = busStop;
    }

}
