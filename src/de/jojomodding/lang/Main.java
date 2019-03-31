package de.jojomodding.lang;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args){
        try {
            Interpreter e = new Interpreter(System.in);
            e.run();
        }catch (Throwable t){
            System.err.println("Fatal error: "+t.toString());
            t.printStackTrace();
        }
    }

}
