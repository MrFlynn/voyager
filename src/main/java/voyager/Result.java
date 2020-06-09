package voyager;

public class Result {
    public String url;
    public String title;
    public float score;
    public String snippet;

    public Result(String URL, String title, float score, String snippet) {
        this.url = URL;
        this.title = title;
        this.score = score;
        this.snippet = snippet;
    }
}
