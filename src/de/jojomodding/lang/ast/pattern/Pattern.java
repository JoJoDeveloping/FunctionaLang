package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.ASTElement;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.parsing.CodePosition;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Pattern extends ASTElement {

    public abstract Type getType(ElabEnvironment env) throws ElaborationException;
    public abstract Map<String, Type> getTypes(ElabEnvironment env) throws ElaborationException;

    public abstract Optional<Map<String, Value>> matchValue(Environment<Value> env, Value v) throws EvaluationException;

    public static <B> Map<String, B> mergeMaps(Map<String,B> m1, Map<String,B> m2) throws DuplicateEntryException{
        Map<String, B> m = new HashMap<>(m1);
        for(Map.Entry<String, B> e : m2.entrySet()){
            if(m.put(e.getKey(), e.getValue())!= null) throw new DuplicateEntryException(e.getKey());
        }
        return m;
    }

    @Override
    public Pattern at(CodePosition p) {
        return (Pattern) super.at(p);
    }

    public static class DuplicateEntryException extends Exception{

        private String dup;

        public DuplicateEntryException(String s){
            this.dup = s;
        }

        public String getDuplicateEntry(){
            return dup;
        }

    };

}
