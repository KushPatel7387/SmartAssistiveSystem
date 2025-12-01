package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class WalkingAssistLogic {

    // Demo landmark locations
    private final LatLng CROSSWALK = new LatLng(43.7325, -79.6086);
    private final LatLng BUS_STOP  = new LatLng(43.7330, -79.6075);

    public enum AssistEvent {
        CROSSWALK_AHEAD,
        BUS_STOP_REACHED,
        NO_LANDMARK
    }

    // ✅ Main business decision method
    public AssistEvent processLocation(Location location) {

        LatLng current = new LatLng(
                location.getLatitude(),
                location.getLongitude()
        );

        double dCrosswalk = distance(current, CROSSWALK);
        double dBusStop   = distance(current, BUS_STOP);

        if (dCrosswalk < 20) {
            return AssistEvent.CROSSWALK_AHEAD;
        }

        if (dBusStop < 20) {
            return AssistEvent.BUS_STOP_REACHED;
        }

        return AssistEvent.NO_LANDMARK;
    }

    // ✅ Pure distance calculation (no Android UI dependency)
    private double distance(LatLng a, LatLng b) {
        float[] result = new float[1];
        Location.distanceBetween(
                a.latitude, a.longitude,
                b.latitude, b.longitude,
                result
        );
        return result[0];
    }
}
