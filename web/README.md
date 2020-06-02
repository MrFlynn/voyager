# web

This is the web UI for the project. 

## Running the Web Interface
You will need [Docker](docker.com) installed to do this.

```bash
$ docker build -t voyager-web:latest .
$ docker run -p "80:80" voyager-web:latest
```