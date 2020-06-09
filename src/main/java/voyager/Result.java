package voyager;

public class Result {
    public String url;
    public String title;
    public String snippet;
    public Result(String URL, String title, String snippet){
        this.url = URL;
        this.title = title;
        this.snippet = snippet;
    }
}
