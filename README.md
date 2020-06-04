# voyager
A lucene-based web search engine for CS172: Information Retrieval

## Running the Backend
```bash
$ docker build -t voyager-backend:latest .
$ docker run -p 5000:5000 \ 
    -v /path/to/html/files:/data \
    voyager-backend:latest
```

Backend service is now running at <localhost:8080/api/search>. 
An example request for query "creative writing" with offset of 10 is: 
```bash
$ curl localhost:8080/api/search?query=creative%20writing&after=10
```