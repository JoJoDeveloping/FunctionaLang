package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.type.FunctionType;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.ConstructorValue;
import de.jojomodding.lang.value.Value;

import java.util.Map;
import java.util.Optional;

public class ConstructorPattern extends Pattern{

    private Datatype.DatatypeDef.DatatypeConstr c;
    private Pattern op;

    public ConstructorPattern(Datatype.DatatypeDef.DatatypeConstr c, Pattern op){
        this.c = c;
        this.op = op;
        asChildren(op);
    }

    @Override
    public Type getType(ElabEnvironment env) throws ElaborationException {
        Type ot = op.getType(env);
        FunctionType ft = (FunctionType) env.dequantizeType(c.getQuantizedType());
        env.forcetype(this, ot, ft.getArgumentType());
        return ft.getResultType();
    }

    @Override
    public Map<String, Type> getTypes(ElabEnvironment env) throws ElaborationException {
        return op.getTypes(env);
    }

    @Override
    public Optional<Map<String, Value>> matchValue(Environment<Value> env, Value v) throws EvaluationException {
        if(v instanceof ConstructorValue){
            ConstructorValue cv = (ConstructorValue) v;
            if(cv.isApplied()){
                return op.matchValue(env, cv.getParam());
            }else throw new EvaluationException(this, "Unapplied constructor value!");
        }else return Optional.empty();
    }
}
