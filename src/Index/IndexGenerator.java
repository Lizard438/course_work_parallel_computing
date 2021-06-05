package Index;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class IndexGenerator {

    ArrayList<CompletableFuture<Void>> tasks = new ArrayList<>();
    Index index = new Index();



    public void processFile( Path file, int fileId) {


        try{

            LineNumberReader fileReader = new LineNumberReader(new InputStreamReader(new FileInputStream(file.toFile())));

            String line;

            while((line = fileReader.readLine())!= null){

                //read and tokenize line, add tokens to dictionary
                line = line.toLowerCase();

                StringTokenizer st = new StringTokenizer(line);

                while(st.hasMoreTokens()){
                    index.addToken(st.nextToken(), fileId, fileReader.getLineNumber());
                }

            }


        }catch (IOException e){
            e.printStackTrace();
        }

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

        index.saveIndex("./index.txt");
        //merge?
    }


    class IndexFileVisitor extends SimpleFileVisitor<Path>{
       int numerator = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            int id = numerator;
            System.out.println(id);
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
