package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.Map;
import java.util.Optional;

public class WildcardPattern extends Pattern{

    private Type t;

    public WildcardPattern(Type t){
        this.t = t;
    }

    @Override
    public Type getType(ElabEnvironment env) throws ElaborationException {
        return t;
    }

    @Override
    public Map<String, Type> getTypes(ElabEnvironment env) throws ElaborationException {
        return Map.of();
    }

    @Override
    public Optional<Map<String, Value>> matchValue(Environment<Value> env, Value v) throws EvaluationException {
        return Optional.of(Map.of());
    }
}
