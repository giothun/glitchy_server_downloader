# Glitchy Server Data Downloader

This command-line tool downloads binary data from a glitchy server that returns truncated data for each request. It uses HTTP Range requests to piece together the complete file and then verifies its integrity by computing the SHA‑256 hash of the assembled file and comparing it with an expected hash.


#  Usage
## With Gradle (Recommended)
```bash
./gradlew run --args="<url> <expected-hash>"
```
## Using the JAR
To build and package the application as a runnable JAR, run:

```bash
./gradlew shadowJar
```
After building, you can run the application from the command line:

```bash
java -jar build/libs/glitchy_server_downloader-1.0-all.jar <url> <expected-hash>
```
For example:
```bash
java -jar build/libs/glitchy_server_downloader-1.0-all.jar http://127.0.0.1:8080/ aee994b63057c59307150b27da7a2c34b0e626eec2f8a4130ceafd2fb0ba0642
```

* **url**: The URL of the glitchy server.
* **expected-hash**: The expected SHA‑256 hash of the complete file. (This is printed by the server on startup.)

# Testing
**NB: I changed host to 0.0.0.0 to be able to test server which running in docker outside of docker**

Unit and integration tests have been provided. To run tests using Gradle, execute:
```bash
./gradlew test
```

CI for Github Actions also provided.