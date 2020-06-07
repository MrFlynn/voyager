package voyager;

public class Result {
    public String url;
    public String title;
    public float score;

    public Result(String URL, String title, float score) {
        this.url = URL;
        this.title = title;
        this.score = score;
    }
}
