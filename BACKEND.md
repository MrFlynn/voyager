# Backend
This document describes how to run the backend by itself on your own machine.

## Running the Backend

### Using Docker
```bash
$ docker build -t voyager-backend:latest .
$ docker run -p 5000:5000 \ 
    -v /path/to/html/files:/data \
    voyager-backend:latest
```

### Using Gradle
```bash
$ gradle run --args="/path/to/html/files/ --port=5000"
```

Backend service is now running at [localhost:5000/api/](http://localhost/api/). 
An example request for query "creative writing" with offset of 10 is:
```bash
$ curl localhost:5000/api/search?query=creative%20writing&after=10
```