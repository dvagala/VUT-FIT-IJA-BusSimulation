package misc;

import java.time.LocalTime;

/**
 * Tato trieda sluzi na nastavanie hlavych simulacnych parametrov.
 * @author Dominik Vagala (xvagal00)
 * @author Jakub Vinš (xvinsj00)
 */
public class SimulationSettings {

    // Simulation speed is same as normal time, if this is set to 1
    public static double speedRatio = 30;
    public static int updateIntervalMs = 400;
    public static LocalTime startTime = LocalTime.of(10, 29, 0, 0);
}
