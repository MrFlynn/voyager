# voyager
A lucene-based web search engine for CS172: Information Retrieval

### Running the Application
The recommended method for running the application is with Docker and Docker 
Compose. Before continuing you need to install both of these applications.

```bash
$ # Replace this with the location of the scraped HTML files.
$ export DATA_DIR=/path/to/scrape/dir
$ # If you are on Windows, run `setx DATA_DIR C:\path\to\scrape\dir` instead.
$ docker-compose up -d
```

Once compose tells you all containers are running, go to 
[http://localhost](http://localhost) and start browsing! 

To stop the application, simply run `docker-compose down`.