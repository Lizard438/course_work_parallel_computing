package Index;

import java.io.Serializable;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Index {

    ConcurrentHashMap<String, HashSet<Position>>  dictionary;
    ConcurrentHashMap<Integer,String> docTable;

    public static class Position implements Serializable{
        int docID;
        int offset;
        int lineSize;
    }
}
