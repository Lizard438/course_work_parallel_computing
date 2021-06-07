# course_work_parallel_computing

Java realization of inverted index.

To build and run project using concole:
 1. Clone repository.
 2. Go to the `src` folder and run `javac -d classes Index/*.java Server/*.java Client/*.java`. All classes are now stored in `classes` folder.
 3. To generate index run `java -cp classes Index.IndexGenerator`. Specify directory for indexing.
 4. Run server by `java -cp classes Server.Server <port number>`. 
 5. Run client by `java -cp classes Client.Client <ip> <port number>`.
 6. Type request separating words by spaces.
 7. To stop client type `Q`.
