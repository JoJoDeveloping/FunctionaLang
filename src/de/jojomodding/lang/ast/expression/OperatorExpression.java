package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.ConstantValue;
import de.jojomodding.lang.value.ConstructorValue;
import de.jojomodding.lang.value.TupleValue;
import de.jojomodding.lang.value.Value;

import static de.jojomodding.lang.type.BaseType.*;

public class OperatorExpression extends Expression{

    public enum BinaryOperator{
        ADD, SUB, MUL, LESS, GREATER, LEQ, GEQ, EQUAL, UNEQUAL, AND, OR, XOR, CONS;
    }

    public enum UnaryOperator{
        NEG, NOT
    }

    private Expression e1, e2;
    private BinaryOperator opb;
    private UnaryOperator opu;

    public OperatorExpression(Expression e1, BinaryOperator op, Expression e2){
        this.e1 = e1;
        this.e2 = e2;
        this.opu = null;
        this.opb = op;
        asChildren(e1, e2);
    }

    public OperatorExpression(UnaryOperator op, Expression e){
        this.e1 = e;
        this.e2 = null;
        this.opu = op;
        this.opb = null;
        asChildren(e1);
    }


    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        if(opb != null){
            Type t1 = e1.elaborate(env);
            Type t2 = e2.elaborate(env);
            switch (opb){
                case ADD:
                case SUB:
                case MUL:
                    env.forcetype(this, t1, INT);
                    env.forcetype(this, t2, INT);
                    return INT;
                case LESS:
                case GREATER:
                case LEQ:
                case GEQ:
                    env.forcetype(this, t1, INT);
                    env.forcetype(this, t2, INT);
                    return BOOL;
                case EQUAL:
                case UNEQUAL:
                    env.forcetype(this, t1, t2);
                    env.forceEquality(this, t1);
                    env.forceEquality(this, t2);
                    return BOOL;
                case AND:
                case OR:
                case XOR:
                    env.forcetype(this, t1, BOOL);
                    env.forcetype(this, t2, BOOL);
                    return BOOL;
                case CONS:
                    Type t = env.newType();
                    Datatype.DatatypeDef listDef = env.getDatatypeDef("list");
                    Datatype lt = new Datatype(listDef, t);
                    env.forcetype(this, t1, t);
                    env.forcetype(this, t2, lt);
                    return lt;
//                case APPEND:
//                    t = env.newType();
//                    listDef = env.getDatatypeDef("list");
//                    lt = new Datatype(listDef, t);
//                    env.forcetype(this, t1, lt);
//                    env.forcetype(this, t2, lt);
//                    return lt;
            }
        }else if(opu != null){
            Type t = e1.elaborate(env);
            switch (opu){
                case NOT:
                    env.forcetype(this, t, BOOL);
                    return BOOL;
                case NEG:
                    env.forcetype(this, t, INT);
                    return INT;
            }
        }else throw new NullPointerException("opu and opb");
        return null;
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        if(opb != null){
            switch (opb){
                case ADD: {
                    ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) e1.evaluate(env), v2 = (ConstantValue.IntegerValue) e2.evaluate(env);
                    return new ConstantValue.IntegerValue(v1.getValue().add(v2.getValue())); }
                case SUB: {
                    ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) e1.evaluate(env), v2 = (ConstantValue.IntegerValue) e2.evaluate(env);
                    return new ConstantValue.IntegerValue(v1.getValue().subtract(v2.getValue())); }
                case MUL: {
                    ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) e1.evaluate(env), v2 = (ConstantValue.IntegerValue) e2.evaluate(env);
                    return new ConstantValue.IntegerValue(v1.getValue().multiply(v2.getValue())); }
                case LESS: {
                    ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) e1.evaluate(env), v2 = (ConstantValue.IntegerValue) e2.evaluate(env);
                    return new ConstantValue.BooleanValue(v1.getValue().compareTo(v2.getValue()) < 0); }
                case GREATER: {
                    ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) e1.evaluate(env), v2 = (ConstantValue.IntegerValue) e2.evaluate(env);
                    return new ConstantValue.BooleanValue(v1.getValue().compareTo(v2.getValue()) > 0); }
                case LEQ: {
                    ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) e1.evaluate(env), v2 = (ConstantValue.IntegerValue) e2.evaluate(env);
                    return new ConstantValue.BooleanValue(v1.getValue().compareTo(v2.getValue()) <= 0); }
                case GEQ: {
                    ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) e1.evaluate(env), v2 = (ConstantValue.IntegerValue) e2.evaluate(env);
                    return new ConstantValue.BooleanValue(v1.getValue().compareTo(v2.getValue()) >= 0); }
                case EQUAL:
                    return new ConstantValue.BooleanValue(e1.evaluate(env).equals(e2.evaluate(env)));
                case UNEQUAL:
                    return new ConstantValue.BooleanValue(!e1.evaluate(env).equals(e2.evaluate(env)));
                case AND:
                    return new ConstantValue.BooleanValue(
                            ((ConstantValue.BooleanValue) e1.evaluate(env)).getValue()
                                    && ((ConstantValue.BooleanValue) e2.evaluate(env)).getValue());
                case OR:
                    return new ConstantValue.BooleanValue(
                            ((ConstantValue.BooleanValue) e1.evaluate(env)).getValue()
                                    || ((ConstantValue.BooleanValue) e2.evaluate(env)).getValue());
                case XOR:
                    return new ConstantValue.BooleanValue(
                            ((ConstantValue.BooleanValue) e1.evaluate(env)).getValue()
                                    ^ ((ConstantValue.BooleanValue) e2.evaluate(env)).getValue());
                case CONS:
                    ConstructorValue cv = (ConstructorValue) env.get("op::");
                    return cv.apply(new TupleValue(e1.evaluate(env), e2.evaluate(env)));
            }
        }else if(opu != null){
            Value v = e1.evaluate(env);
            switch (opu){
                case NOT:
                    ConstantValue.BooleanValue vb = (ConstantValue.BooleanValue) v;
                    return new ConstantValue.BooleanValue(!vb.getValue());
                case NEG:
                    ConstantValue.IntegerValue vi = (ConstantValue.IntegerValue) v;
                    return new ConstantValue.IntegerValue(vi.getValue().negate());
            }
        }else throw new NullPointerException("opu and opb");
        return null;
    }


}
