package Index;

import static java.nio.file.StandardOpenOption.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Index implements Serializable{

    ConcurrentHashMap<String, ArrayList<Position>>  dictionary;
    HashMap <Integer,String> docTable;

    public Index(){
        dictionary = new ConcurrentHashMap<>();
        docTable = new HashMap<>();
    }

    public ArrayList<String> find(String query){

        ArrayList<String> lines = new ArrayList<>(); //result lines

        //normalize, tokenize query
        query = query.toLowerCase();
        ArrayList<String> tokens = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(query);

        while(st.hasMoreTokens()){
            tokens.add(st.nextToken());
        }

        if(!tokens.isEmpty()){
            //for each token make set of doc ids for intersection
            Set<Integer> docs = dictionary.getOrDefault(tokens.get(0),new ArrayList<>())
                    .stream().map(Position::getID).collect(Collectors.toSet());

            for(int i = 1; i<tokens.size(); i++){
                docs.retainAll(dictionary.getOrDefault(tokens.get(i),new ArrayList<>())
                        .stream().map(Position::getID).collect(Collectors.toSet()));
            }

            if(!docs.isEmpty()){  //getting results from intersection
                Map<Integer,Set<Integer>> result = new HashMap<>(); // - map(docId, set of lines positions)

                for( String token : tokens){

                    result.putAll( dictionary.get(token).stream().filter(pos -> docs.contains(pos.docID))
                            .collect(Collectors.groupingBy(Position::getID,
                                    Collectors.mapping(Position::getLineStart, Collectors.toSet()) )));
                }


                result.forEach((docId, list) ->{ //getting lines containing query

                    for( int pos: list){
                        try{
                            lines.add(Files.lines(Paths.get(docTable.get(docId))).skip(pos-1).findFirst().get());
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });

               return lines;

            }

        }
        lines.add("Your search did not match any documents");
        return lines;

    }

    public static Index loadIndex(String filePath) throws IOException, ClassNotFoundException{
        ObjectInputStream in = new ObjectInputStream(
                Files.newInputStream(Paths.get(filePath)));
        return (Index) in.readObject();
    }

    void addToken(String token, int docId, int lineNumber){

        Position pos = new Position(docId, lineNumber);

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
        int lineNumber;

        public Position(int docID, int lineNumber){
            this.docID = docID;
            this.lineNumber = lineNumber;
        }

        int getID(){
            return this.docID;
        }
        int getLineStart(){
            return this.lineNumber;
        }
    }

}
