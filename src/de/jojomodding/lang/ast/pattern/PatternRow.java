package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.ASTElement;
import de.jojomodding.lang.ast.expression.Expression;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PatternRow extends ASTElement {

    private List<Map.Entry<Pattern, Expression>> rows;

    public PatternRow(List<Map.Entry<Pattern, Expression>> rows){
        this.rows = rows;
        rows.forEach(e -> asChildren(e.getKey(), e.getValue()));
    }

    public Map.Entry<Type, Type> elaborate(ElabEnvironment env) throws ElaborationException {
        Type pt = env.newType(), et = env.newType();
        for(Map.Entry<Pattern, Expression> e : rows){
            env.forcetype(this, e.getKey().getType(env), pt);
            Map<String, Type> vt = e.getKey().getTypes(env);
            Type rt = e.getValue().elaborate(env.replaceAllIn(vt));
            env.forcetype(this, rt, et);
        }
        return Map.entry(pt, et);
    }

    public Value evaluate(Environment<Value> env, Value v) throws EvaluationException {
        for(Map.Entry<Pattern, Expression> e : rows){
            Optional<Map<String, Value>> vm = e.getKey().matchValue(env, v);
            if(vm.isPresent()){
                return e.getValue().evaluate(env.replaceAllIn(vm.get()));
            }else continue;
        }
        throw new EvaluationException(this, "Could not match value to pattern");
    }

}
