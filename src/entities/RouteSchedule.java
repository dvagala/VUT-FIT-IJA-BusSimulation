package entities;

import java.time.LocalTime;
import java.util.List;

public class RouteSchedule {

   private List<LocalTime> departures;

   public RouteSchedule() {
   }

   public RouteSchedule(List<LocalTime> departures) {
      this.departures = departures;
   }

   public List<LocalTime> getDepartures() {
      return departures;
   }

   public LocalTime getFirstStopDepartureTime(){
      return departures.get(0);
   }

   public LocalTime getLastStopDepartureTime(){
      return departures.get(departures.size() - 1);
   }

   public void setDepartures(List<LocalTime> departures) {
      this.departures = departures;
   }
}
