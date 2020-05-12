package misc;

import java.time.LocalTime;

public class SimulationSettings {

    // Simulation speed is same as normal time, if this is set to one
    public static double speedRatio = 60;
    public static int updateIntervalMs = 200;
    public static LocalTime startTime = LocalTime.of(10, 40, 0, 0);
}
