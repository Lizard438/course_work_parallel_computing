package Index;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;


public class IndexGenerator {

    ArrayList<CompletableFuture<Void>> tasks = new ArrayList<>();
    Index index = new Index();



    CompletableFuture<Void> lineToDictionary(String line, int lineStart, int fileId){
        //preprocess
        return CompletableFuture.runAsync(()->{
            StringTokenizer st = new StringTokenizer(line);
            while(st.hasMoreTokens()){
                index.addToken(st.nextToken(), fileId, lineStart);
            }
        });
    }

    public void generateIndex(String path)throws IOException{

        Files.walkFileTree(Paths.get(path), new IndexFileVisitor());

        //wait for tasks
            tasks.forEach(t->{
                try{
                    t.get();
                }catch(InterruptedException|ExecutionException e){
                    e.printStackTrace();
                }
            });


        //merge?
    }
    public void processFile( Path file, int fileId) {
        System.out.println("processing file");

        try{

            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file.toFile())));

            int offset = 0;
            String line;

            while((line = fileReader.readLine())!= null){
                //temp is temporary solution, I will fix this later
                CompletableFuture<Void> temp = lineToDictionary(line, offset, fileId);
                offset += line.length();
            }


        }catch (IOException e){
            e.printStackTrace();
        }

    }

    class IndexFileVisitor extends SimpleFileVisitor<Path>{
       int numerator = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            int id = numerator;
            numerator +=1;
            index.docTable.put(id,file.toString());

            tasks.add(CompletableFuture.runAsync(()->processFile(file, id)));

            return FileVisitResult.CONTINUE;

        }
    }

    public static void main(String []args){
        IndexGenerator i = new IndexGenerator();
        try{
            i.generateIndex("c:/Users/liza/IdeaProjects/filesCourse");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
