package Index;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;


public class IndexGenerator {

    ArrayList<CompletableFuture<Void>> tasks = new ArrayList<>();
    Index index = new Index();
    public static ForkJoinPool pool;



    public void processFile( Path file, int fileId) {


        try(LineNumberReader fileReader = new LineNumberReader(new InputStreamReader(new FileInputStream(file.toFile()))))
        {
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
    }


    class IndexFileVisitor extends SimpleFileVisitor<Path>{
       int numerator = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            int id = numerator;

            numerator +=1;
            index.docTable.put(id,file.toString());

            tasks.add(CompletableFuture.runAsync(()->processFile(file, id), pool));

            return FileVisitResult.CONTINUE;

        }
    }

    public static void timeAnalysis(int minThreads, int maxThreads, int step){

        ArrayList<String> result = new ArrayList<>();

        for (int i = minThreads; i < maxThreads; i+=step){

            IndexGenerator gen = new IndexGenerator();
            pool = new ForkJoinPool(i);

            long startTime = System.currentTimeMillis();

            try{
                gen.generateIndex("c:/Users/liza/IdeaProjects/filesCourse");
            }catch (IOException e){
                e.printStackTrace();
            }

            long endTime = System.currentTimeMillis();
            pool.shutdown();
            String line = i+","+(endTime - startTime);
            result.add(line);
        }

        try (PrintWriter pw = new PrintWriter("c:/Users/liza/IdeaProjects/CourseWork/time.csv")) {
            result.forEach(pw::println);
        }catch(FileNotFoundException e){
            System.out.println("file not found");
        }

    }

    public static void main(String []args){

        if(args.length != 0){
            int minThreads = Integer.parseInt(args[0]);
            int maxThreads = Integer.parseInt(args[1]);
            int step = Integer.parseInt(args[2]);
            System.out.println("Time Analysis mode");
            timeAnalysis(minThreads, maxThreads, step);

        }else{

            IndexGenerator i = new IndexGenerator();
            pool = ForkJoinPool.commonPool();
            try(BufferedReader console = new BufferedReader( new InputStreamReader(System.in)))
            {
                System.out.println("Enter path to directory to generate index: ");
                String path = console.readLine();

                if(path.equals("default")){
                    path = "c:/Users/liza/IdeaProjects/filesCourse";
                }

                i.generateIndex(path);
                System.out.println("Index ready.");

            }catch(IOException e){
                e.printStackTrace();
            }
        }

    }

}
