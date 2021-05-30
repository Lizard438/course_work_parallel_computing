package Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Index {

    ConcurrentHashMap<String, ArrayList<Position>>  dictionary;
    HashMap <Integer,String> docTable;

    public Index(){
        dictionary = new ConcurrentHashMap<>();
        docTable = new HashMap<>();
    }

    void addToken(String token, int docId, int lineStart){
        //
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
