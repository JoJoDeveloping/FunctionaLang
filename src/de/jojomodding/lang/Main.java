package de.jojomodding.lang;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            StringBuilder bl = new StringBuilder();

            boolean lle = false;
            int lc = 0;
            System.out.println("Your code please:");
            while (true) {
                String l = br.readLine();
                if (lle) {
                    if (l.equals("END"))
                        return;
                    System.out.println("Read " + lc + " lines, interpreting...");
                    Interpreter i = new Interpreter(bl.toString());
                    System.out.println(i.run());
                    System.out.println();
                    System.out.println("Your code please:");
                    bl = new StringBuilder();
                    lc = 0;
                    lle = false;
                    continue;
                }
                lc++;
                if (l.equals("")) lle = true;
                bl.append(l).append('\n');
            }
        }catch (Throwable t){
            System.err.println("Error: "+t.toString());
            t.printStackTrace();
        }
    }

}
