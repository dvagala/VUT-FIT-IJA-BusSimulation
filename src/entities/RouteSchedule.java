package entities;

import java.time.LocalTime;
import java.util.List;

public class RouteSchedule {

//   private List<LocalTime> departures;
   private List<RouteScheduleEntry> entries;

   public RouteSchedule() {
   }

   public RouteSchedule(List<RouteScheduleEntry> entries) {
      this.entries = entries;
   }

   public List<RouteScheduleEntry> getEntries() {
      return entries;
   }

   public LocalTime getFirstStopDepartureTime(){
      return entries.get(0).getDepartureTime();
   }

   public LocalTime getLastStopDepartureTime(){
      return entries.get(entries.size() - 1).getDepartureTime();
   }

}
