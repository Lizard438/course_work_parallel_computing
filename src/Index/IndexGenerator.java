package Index;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class IndexGenerator {

    ArrayList<CompletableFuture<Void>> tasks = new ArrayList<>();



   void lineToDictionary(String line, int offset){
       System.out.println(offset);
        System.out.println(line);
        //preprocess
        //spimi
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
    public void processFile( Path file) {
        System.out.println("processing file");

        try{

            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file.toFile())));

            int offset = 0;
            String line;

            while((line = fileReader.readLine())!= null){
                lineToDictionary(line, offset);
                offset += line.length();
            }


        }catch (IOException e){
            e.printStackTrace();
        }

    }

    class IndexFileVisitor extends SimpleFileVisitor<Path>{
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

           tasks.add(CompletableFuture.runAsync(()->processFile(file)));

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
