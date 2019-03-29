package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.Map;
import java.util.Optional;

public class VariablePattern extends Pattern {

    private String variable;
    private Type vtype;

    public VariablePattern(String name, Type t){
        this.variable = name;
        this.vtype = t;
    }

    @Override
    public Type getType(ElabEnvironment env) throws ElaborationException {
        return vtype;
    }

    @Override
    public Map<String, Type> getTypes(ElabEnvironment env) throws ElaborationException {
        return Map.of(variable, vtype);
    }

    @Override
    public Optional<Map<String, Value>> matchValue(Environment<Value> env, Value v) throws EvaluationException {
        return Optional.of(Map.of(variable, v));
    }
}
