package Index;

import static java.nio.file.StandardOpenOption.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class Index implements Serializable{

    ConcurrentHashMap<String, ArrayList<Position>>  dictionary;
    HashMap <Integer,String> docTable;

    public Index(){
        dictionary = new ConcurrentHashMap<>();
        docTable = new HashMap<>();
    }

    void find(String query){
        //tokenize query

        /*StringTokenizer st = new StringTokenizer(query);

        while(st.hasMoreTokens()){
            //st.nextToken(),
        }*/


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
    }
}
