package de.jojomodding.lang.ast.def;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.expression.Expression;
import de.jojomodding.lang.ast.pattern.Pattern;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ValueDefinition extends Definition {

    private Pattern p;
    private Expression e;

    public ValueDefinition(Pattern p, Expression e){
        this.p = p;
        this.e = e;
    }

    @Override
    public List<String> elaborate(ElabEnvironment env) throws ElaborationException {
        Type et = e.elaborate(env);
        Type t = p.getType(env);
        env.forcetype(this, et, t);
        Map<String, Type> mst = p.getTypes(env);
        env.setAll(mst);
        return new ArrayList<>(mst.keySet());
    }

    @Override
    public void evaluate(Environment<Value> env) throws EvaluationException {
        Optional<Map<String, Value>> o = p.matchValue(env, e.evaluate(env));
        if(o.isPresent()){
            env.setAll(o.get());
        }else throw new EvaluationException(this, "Mismatched pattern!");
    }
}
