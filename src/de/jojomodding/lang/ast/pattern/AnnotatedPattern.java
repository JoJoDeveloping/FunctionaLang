package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.Map;
import java.util.Optional;

public class AnnotatedPattern extends Pattern {

    private Pattern p;
    private Type type;

    public AnnotatedPattern(Pattern p, Type ft){
        this.p= p;
        this.type = ft;
    }

    @Override
    public Type getType(ElabEnvironment env) throws ElaborationException {
        Type t = p.getType(env);
        env.forcetype(this, t, type);
        return type;
    }

    @Override
    public Map<String, Type> getTypes(ElabEnvironment env) throws ElaborationException {
        return p.getTypes(env);
    }

    @Override
    public Optional<Map<String, Value>> matchValue(Environment<Value> env, Value v) throws EvaluationException {
        return p.matchValue(env, v);
    }
}
