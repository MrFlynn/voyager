# voyager
A lucene-based web search engine for CS172: Information Retrieval

## Running with Docker
    ./gradlew build
    docker build -t voyager .
    docker run -p 8080:8080 voyager
Service is now running at <localhost:8080/api/search>. An example request for query "creative writing" with offset of 10 is: localhost:8080/api/search?query=creative%20writing&after=10