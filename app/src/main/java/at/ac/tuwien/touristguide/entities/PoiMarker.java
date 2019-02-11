package at.ac.tuwien.touristguide.entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


/**
 * @author Manu Weilharter
 */
public class PoiMarker implements ClusterItem {

    private Poi poi;
    private String title;

    public PoiMarker(Poi poi) {
        this.poi = poi;
        this.title = poi.getName();
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(poi.getLatitude(), poi.getLongitude());
    }

    public Poi getPoi() {
        return poi;
    }

    public void setPoi(Poi poi) {
        this.poi = poi;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
