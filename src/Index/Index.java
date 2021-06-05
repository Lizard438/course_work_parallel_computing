package Index;

import static java.nio.file.StandardOpenOption.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Index implements Serializable{

    ConcurrentHashMap<String, ArrayList<Position>>  dictionary;
    HashMap <Integer,String> docTable;

    public Index(){
        dictionary = new ConcurrentHashMap<>();
        docTable = new HashMap<>();
    }

    void find(String query){
        //tokenize query
        query = query.toLowerCase();
        ArrayList<String> tokens = new ArrayList<>();
        //for each token make set of doc ids
        //if set not null return string arr
        StringTokenizer st = new StringTokenizer(query);

        while(st.hasMoreTokens()){
            tokens.add(st.nextToken());
        }
        if(!tokens.isEmpty()){
            Set<Integer> docs = dictionary.getOrDefault(tokens.get(0),new ArrayList<>()).stream().map(Position::getID).collect(Collectors.toSet());
            for(int i = 1; i<tokens.size(); i++){
                docs.retainAll(dictionary.getOrDefault(tokens.get(i),new ArrayList<>()).stream().map(Position::getID).collect(Collectors.toSet()));
            }

            if(!docs.isEmpty()){
                Map<Integer,Set<Integer>> result = new HashMap<>();

                for( String token : tokens){
                    result.putAll( dictionary.get(token).stream().filter(pos -> docs.contains(pos.docID)).collect(Collectors.groupingBy(Position::getID, Collectors.mapping(Position::getLineStart, Collectors.toSet()) )));
                }
                /////////
                for(Set<Integer> i : result.values()){
                    System.out.println(i);
                }

            }
            System.out.println("empty");

        }


    }

    public static Index loadIndex(String filePath) throws IOException, ClassNotFoundException{
        ObjectInputStream in = new ObjectInputStream(
                Files.newInputStream(Paths.get(filePath)));
        return (Index) in.readObject();
    }

    void addToken(String token, int docId, int lineStart){

        Position pos = new Position(docId, lineStart);

        ArrayList<Position> oldList;
        ArrayList<Position> newList = new ArrayList<>();

        do {
            newList.add(pos);
            oldList = dictionary.putIfAbsent(token, newList);

            if (oldList == null) {
                break;
            } else {
                newList = new ArrayList<>(oldList);
                newList.add(pos);
            }

        } while (!dictionary.replace(token, oldList, newList));

    }

    void saveIndex(String filePath){

        try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(Paths.get(filePath), CREATE))) {
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Position implements Serializable{

        int docID;
        int lineStart;

        public Position(int docID, int lineStart){
            this.docID = docID;
            this.lineStart = lineStart;
        }

        int getID(){
            return this.docID;
        }
        int getLineStart(){
            return this.lineStart;
        }
    }

    public static void main(String []args){
        try{
            Index i = loadIndex("./index.txt") ;
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String msg = console.readLine();
            i.find(msg);
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

    }

}
