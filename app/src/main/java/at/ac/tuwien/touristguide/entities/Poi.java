package at.ac.tuwien.touristguide.entities;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Manu Weilharter
 *         <p/>
 *         Represents a point of interest
 */
public class Poi implements Comparable<Poi>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int id;
    private String wikiId;
    private String name;
    private double latitude;
    private double longitude;
    private double distance;
    private List<Section> sections = new ArrayList<Section>();
    private String language;
    private int visited;

    public Poi() {

    }

    public Poi(String name, String wikiId, double latitude, double longitude, String language, int visited) {
        super();
        this.wikiId = wikiId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.language = language;
        this.visited = visited;
    }

    public Poi(int id, String wikiId, String name, double latitude, double longitude, String language, int visited, double distance) {
        super();
        this.id = id;
        this.wikiId = wikiId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.language = language;
        this.visited = visited;
        this.distance = distance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name.replace("''", "'");
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getWikiId() {
        return wikiId;
    }

    public void setWikiId(String wikiId) {
        this.wikiId = wikiId;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public void addSection(Section sec) {
        sections.add(sec);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getVisited() {
        return visited;
    }

    public void setVisited(int visited) {
        this.visited = visited;
    }


    @Override
    public String toString() {
        return "Poi [wikiId=" + wikiId + ", name=" + name + ", latitude="
                + latitude + ", longitude=" + longitude + ", sections="
                + sections + "]";
    }

    @Override
    public int compareTo(Poi o) {
        return ((Integer) (this.id)).compareTo(o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Poi) {
            String toCompare = ((Poi) o).name;
            return name.equals(toCompare);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


}
