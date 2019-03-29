package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.ConstantValue;
import de.jojomodding.lang.value.Value;

import java.util.Map;
import java.util.Optional;

public class ConstantPattern extends Pattern {

    private ConstantValue cv;

    public ConstantPattern(ConstantValue sv){
        this.cv = sv;
    }

    @Override
    public Type getType(ElabEnvironment env) throws ElaborationException {
        return cv.getType(env);
    }

    @Override
    public Map<String, Type> getTypes(ElabEnvironment env) throws ElaborationException {
        return Map.of();
    }

    @Override
    public Optional<Map<String, Value>> matchValue(Environment<Value> env, Value v) throws EvaluationException {
        if(v instanceof ConstantValue){
            if(v.equals(cv)){
                return Optional.of(Map.of());
            }else return Optional.empty();
        }else throw new EvaluationException(this, "Expected constant value");
    }
}
