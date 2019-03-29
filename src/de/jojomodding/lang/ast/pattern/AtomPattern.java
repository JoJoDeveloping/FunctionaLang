package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.AtomValue;
import de.jojomodding.lang.value.Value;

import java.util.Map;
import java.util.Optional;

public class AtomPattern extends Pattern{

    private Datatype.DatatypeDef def;
    private String name;

    public AtomPattern(Datatype.DatatypeDef def, String name){
        this.def = def;
        this.name = name;
    }

    @Override
    public Type getType(ElabEnvironment env) throws ElaborationException {
        return env.dequantizeType(def.getAtomType());
    }

    @Override
    public Map<String, Type> getTypes(ElabEnvironment env) throws ElaborationException {
        return Map.of();
    }

    @Override
    public Optional<Map<String, Value>> matchValue(Environment<Value> env, Value v) throws EvaluationException {
        if(v instanceof AtomValue){
            if(((AtomValue) v).getTypedef() == def && ((AtomValue) v).getName().equals(name)){
                return Optional.of(Map.of());
            }
        }
        return Optional.empty();
    }
}
