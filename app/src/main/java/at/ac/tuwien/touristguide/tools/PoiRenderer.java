package at.ac.tuwien.touristguide.tools;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.HashMap;

import at.ac.tuwien.touristguide.entities.PoiMarker;


/**
 * @author Manu Weilharter
 */
public class PoiRenderer extends DefaultClusterRenderer<PoiMarker> {

    public static HashMap<Marker, PoiMarker> markerMap = new HashMap<>();


    public PoiRenderer(Context context, GoogleMap map, ClusterManager<PoiMarker> clusterManager) {
        super(context, map, clusterManager);
    }


    @Override
    protected void onBeforeClusterItemRendered(PoiMarker poiMarker, MarkerOptions markerOptions) {
        LatLng coords = poiMarker.getPoi().getLatLng();

        if (poiMarker.getPoi().getVisited() == 0) {
            markerOptions
                    .position(coords)
                    .title(poiMarker.getPoi().getName());
        } else {
            markerOptions
                    .position(coords)
                    .title(poiMarker.getPoi().getName())
                    .alpha(0.3f);
        }
    }

    @Override
    protected void onClusterItemRendered(PoiMarker poiMarker, Marker marker) {
        super.onClusterItemRendered(poiMarker, marker);
        markerMap.put(marker, poiMarker);
    }

}
