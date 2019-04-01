package de.jojomodding.lang.type;

import de.jojomodding.lang.env.ElabEnvironment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class Type {

    public final String deparse(ElabEnvironment env){
        return deparse(env, new DeparseHelper());
    }

    public abstract String deparse(ElabEnvironment env, DeparseHelper help);

    protected static class DeparseHelper{
        HashMap<String, String> fromfirst = new HashMap<>();
        Set<String> quantifiedVars = new HashSet<>();
        int i = 0;
    }

}
