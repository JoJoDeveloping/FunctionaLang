package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.FunctionType;
import de.jojomodding.lang.type.TupleType;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.OperatorProc;
import de.jojomodding.lang.value.Value;

public class OperatorFunctionExpression extends Expression{

    private OperatorExpression.BinaryOperator opb;
    private OperatorExpression.UnaryOperator opu;

    public OperatorFunctionExpression(OperatorExpression.BinaryOperator opb){
        this.opu = null;
        this.opb = opb;
    }

    public OperatorFunctionExpression(OperatorExpression.UnaryOperator opu){
        this.opu = opu;
        this.opb = null;
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        if(opb != null){
            Type t1 = env.newType(), t2 = env.newType();
            TupleType tt = new TupleType(t1, t2);
            return new FunctionType(tt, OperatorExpression.elaborate(this, opb, t1, t2, env));
        }else if(opu != null){
            Type t = env.newType();
            return new FunctionType(t, OperatorExpression.elaborate(this, opu, t, env));
        }else throw new NullPointerException("opu and opb");
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        if(opb != null){
            return new OperatorProc(opb);
        }else if(opu != null){
            return new OperatorProc(opu);
        }else throw new NullPointerException("opu and opb");
    }
}
