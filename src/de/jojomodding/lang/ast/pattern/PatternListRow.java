package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.ASTElement;
import de.jojomodding.lang.ast.expression.Expression;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PatternListRow extends ASTElement {

    private List<Map.Entry<PatternList, Expression>> rows;
    private int listsize = -1;


    public PatternListRow(List<Map.Entry<PatternList, Expression>> rows) throws IllegalArgumentException{
        this.rows = rows;
        rows.forEach(e -> asChildren(e.getKey(), e.getValue()));
        for (Map.Entry<PatternList, Expression> e : rows) {
            if (listsize == -1)
                listsize = e.getKey().size();
            if (listsize != e.getKey().size()) throw new IllegalArgumentException("Mismatched pattern list size");
        }

    }

    public Map.Entry<List<Type>, Type> elaborate(ElabEnvironment env) throws ElaborationException {
        Type et = env.newType();
        List<Type> pt = new ArrayList<>(listsize);
        for(int i = 0; i < listsize; i++)
            pt.add(env.newType());

        for(Map.Entry<PatternList, Expression> e : rows){
            List<Type> lt = e.getKey().getType(env);
            for(int i = 0; i < listsize; i++)
                env.forcetype(this, pt.get(i), lt.get(i));
            Map<String, Type> vt = e.getKey().getTypes(env);
            Type rt = e.getValue().elaborate(env.replaceAllIn(vt));
            env.forcetype(this, rt, et);
        }
        return Map.entry(pt, et);
    }

    public Value evaluate(Environment<Value> env, List<Value> v) throws EvaluationException {
        for(Map.Entry<PatternList, Expression> e : rows){
            Optional<Map<String, Value>> vm = e.getKey().matchValues(env, v);
            if(vm.isPresent()){
                return e.getValue().evaluate(env.replaceAllIn(vm.get()));
            }else continue;
        }
        throw new EvaluationException(this, "Could not match value to pattern list");
    }

    public int size() {
        return listsize;
    }
}
