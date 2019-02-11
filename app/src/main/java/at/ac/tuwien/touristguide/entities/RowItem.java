package at.ac.tuwien.touristguide.entities;


/**
 * @author Manu Weilharter
 *         <p/>
 *         Represents the items in the Nearby Fragment
 */
public class RowItem {
    private int imageId;
    private String title;
    private String desc;
    private int distance;
    private int visited;

    public RowItem(int imageId, String title, String desc, int distance, int visited) {
        this.imageId = imageId;
        this.title = title;
        this.desc = desc;
        this.distance = distance;
        this.visited = visited;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getVisited() {
        return visited;
    }

    public void setVisited(int visited) {
        this.visited = visited;
    }

    @Override
    public String toString() {
        return title + "\n" + desc;
    }
}
