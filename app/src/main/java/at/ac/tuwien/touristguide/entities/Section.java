package at.ac.tuwien.touristguide.entities;


/**
 * @author Manu Weilharter
 *         <p/>
 *         Represents a Section of a Wikipedia Page
 */
public class Section {

    private int id;
    private int pois_id;
    private String header;
    private String content;
    private String category;

    public Section() {

    }

    public Section(int id, int pois_id, String header, String content, String category) {
        super();
        this.id = id;
        this.pois_id = pois_id;
        this.header = header;
        this.content = content;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPois_id() {
        return pois_id;
    }

    public void setPois_id(int pois_id) {
        this.pois_id = pois_id;
    }

    public String getHeader() {
        return header.replace("§", "");
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Section [header=" + header
                + ", content=" + content + ", category=" + category + "]";
    }

}
