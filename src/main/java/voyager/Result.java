package voyager;

public class Result {
    public String url;
    public String title;
    public float score;
    public String description;

    public Result(String URL, String title, float score, String description) {
        this.url = URL;
        this.title = title;
        this.score = score;
        this.description = description;
    }
}
