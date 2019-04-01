package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.FunctionType;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.*;

public class ApplicationExpression extends Expression {

    private Expression func, arg;

    public ApplicationExpression(Expression func, Expression arg){
        this.func = func;
        this.arg = arg;
        asChildren(func, arg);
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        Type funtype = func.elaborate(env);
        Type argType = env.newType(), resType = env.newType();
        env.forcetype(this, new FunctionType(argType, resType), funtype);
        Type gType = arg.elaborate(env);
        env.forcetype(this, argType, gType);
        return resType;
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        Value v = func.evaluate(env);
        if(v instanceof ProcValue){
            Value av = arg.evaluate(env);
            ProcValue pv = (ProcValue) v;
            return pv.getBody().evaluate(pv.getEvaluationEnvironment(), av);
        }else if(v instanceof DefinedFunctionValue){
            DefinedFunctionValue dfv = (DefinedFunctionValue) v;
            Value x = arg.evaluate(env);
            dfv = dfv.applyValue(x);
            if(dfv.hasAllParams()){
                return dfv.getDefinition().evaluate(dfv.getEnv(), dfv.getValues());
            }else return dfv;
        }else if(v instanceof ConstructorValue){
            Value x = arg.evaluate(env);
            ConstructorValue cv = (ConstructorValue) v;
            if(cv.isApplied()) throw new EvaluationException(this, "Constructor value already applied!");
            return cv.apply(x);
        }else if(v instanceof OperatorProc){
            OperatorProc op = (OperatorProc) v;
            Value x = arg.evaluate(env);
            if(op.isBinaryOperator()){
                if(!(x instanceof TupleValue)) throw new EvaluationException(this, "Operator proc expects tuple with two entries");
                TupleValue tv = (TupleValue) x;
                if(tv.entries().size() != 2) throw new EvaluationException(this, "Operator proc expects tuple with two entries");
                return OperatorExpression.evaluate(this, op.getBinaryOperator(), () -> tv.entries().get(0), () -> tv.entries().get(1), env);
            }else if(op.isUnaryOperator()){
                return OperatorExpression.evaluate(this, op.getUnaryOperator(), () -> x, env);
            }else throw new NullPointerException("opu and opb");
        }else
            throw new EvaluationException(this, "Unknown type of function value "+v.getClass().getName());
    }
}
