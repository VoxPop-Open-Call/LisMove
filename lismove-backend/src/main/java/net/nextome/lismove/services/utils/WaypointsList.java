package net.nextome.lismove.services.utils;

import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.LatLng;

import java.util.ArrayList;

public class WaypointsList extends ArrayList<LatLng> {
    private static final int WAYPOINTS_ARRAY_SIZE = 25;

    /**
     * Subdivides the super-ArrayList into arrays of size = WAYPOINTS_ARRAY_SIZE
     *
     * @return Returns the subdivided list
     */
    public ArrayList<LatLng[]> getTrimmedList() {
        ArrayList<LatLng[]> result = new ArrayList<>();
        int numArrays = (int) Math.ceil((double) (this.size() - 1) / (WAYPOINTS_ARRAY_SIZE - 1)), end, start;
        for (int i = 0; i < numArrays; i++) {
            start = WAYPOINTS_ARRAY_SIZE * i - i;
            end = WAYPOINTS_ARRAY_SIZE * (i + 1) - i;
            if (i == numArrays - 1) {
                end = this.size();
            }
            result.add(this.subList(start, end).toArray(new LatLng[0]));
        }
        return result;
    }

    public static class WaypointsListResult {
        private final ArrayList<DirectionsLeg> directionsLegs;
        private final String polyline;

        public WaypointsListResult(ArrayList<DirectionsLeg> directionsLegs, String polyline) {
            this.directionsLegs = directionsLegs;
            this.polyline = polyline;
        }

        public ArrayList<DirectionsLeg> getDirectionsLegs() {
            return directionsLegs;
        }

        public String getPolyline() {
            return polyline;
        }
    }
}
