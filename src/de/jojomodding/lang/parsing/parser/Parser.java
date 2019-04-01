package de.jojomodding.lang.parsing.parser;

import de.jojomodding.lang.ast.def.DatatypeDefinition;
import de.jojomodding.lang.ast.def.Definition;
import de.jojomodding.lang.ast.def.FunctionDefinition;
import de.jojomodding.lang.ast.def.ValueDefinition;
import de.jojomodding.lang.ast.expression.*;
import de.jojomodding.lang.ast.pattern.*;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ParserException;
import de.jojomodding.lang.parsing.CodePosition;
import de.jojomodding.lang.parsing.Token;
import de.jojomodding.lang.type.*;
import de.jojomodding.lang.value.ConstantValue;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import static de.jojomodding.lang.parsing.Token.Basic.*;

public class Parser extends Thread{

    private List<Token> lastTokens = new LinkedList();
    private ConcurrentLinkedQueue<Token> pendingTokens;
    private Consumer<Throwable> exceptionHandler;
    private ElabEnvironment env;
    private final Object sync = new Object();
    private Consumer<Definition> def;

    private ParserHelper<Expression> expHelper;
    private ParserHelper<Type> tyHelper;
    private PatternHelper patternHelper;

    public Parser(Consumer<Definition> cont, Consumer<Throwable> error){
        this.pendingTokens = new ConcurrentLinkedQueue<>();
        this.env = new ElabEnvironment();
        this.def = cont;
        this.exceptionHandler = error;
        expHelper = new ExpressionParser(this);
        tyHelper = new TypeParser(this);
        patternHelper = new PatternHelper(this);
    }

    public void continueWith(Token t){
        synchronized (sync){
            pendingTokens.add(t);
            sync.notifyAll();
        }
    }

    public ElabEnvironment getElabEnviron(){
        return env;
    }

    protected Token.Basic current_rep() throws ParserException {
        return current().rep();
    }

    protected Token current() throws ParserException {
        while (pendingTokens.isEmpty()){
            synchronized (sync){
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
        return pendingTokens.peek();
    }

    protected Token next() throws ParserException {
        advance();
        return current();
    }

    protected void advance(){
        synchronized (sync) {
            while (pendingTokens.isEmpty()) {
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
        lastTokens.add(pendingTokens.poll());
    }

    public void run(){
        try{
            while (true){
                while (current_rep() == SEMICOLON)
                    advance();
                Definition def = def();
                this.def.accept(def);
            }
        } catch (Throwable e) {
            exceptionHandler.accept(e);
        }
    }

    private Definition def() throws ParserException{
        CodePosition cp = current().getPosition();
        Token ft = current();
        switch (current_rep()){
            case FUN:
                advance();
                if(current_rep() != TYPE_IDENT) throw new ParserException(current(), "Expected function name");
                String name = ((Token.IDToken)current()).value();
                Type resultType = null;
                advance();
                List<Map.Entry<PatternList, Expression>> l = new LinkedList<>();
                CodePosition pcp = current().getPosition();
                PatternList p = patlist();
                switch (current_rep()){
                    case COLON:
                        advance();
                        resultType = ty();
                        if(current_rep() != DEFEQUAL) throw new ParserException(current(), "Expected \"=\"");
                    case DEFEQUAL:
                        if(resultType == null) resultType = env.newType();
                        advance();
                        Expression e = exp();
                        l.add(Map.entry(p, e));
                        break;
                    default: throw new ParserException(current(), "Expected \":\" or \"=\"");
                }
                loop: while(true){
                    switch (current_rep()){
                        case VBAR:
                            advance();
                            if(current_rep() != TYPE_IDENT) throw new ParserException(current(), "Expected function name");
                            if(!((Token.IDToken)current()).value().equals(name)) throw new ParserException(current(), "Mismatched function name!");
                            advance();
                            p = patlist();
                            switch (current_rep()){
                                case COLON:
                                    advance();
                                    resultType = ty();
                                    if(current_rep() != DEFEQUAL) throw new ParserException(current(), "Expected \"=\"");
                                case DEFEQUAL:
                                    if(resultType == null) resultType = env.newType();
                                    advance();
                                    Expression e = exp();
                                    l.add(Map.entry(p, e));
                                    continue  loop;
                                default: throw new ParserException(current(), "Expected \":\" or \"=\"");
                            }
                        default: break loop;
                    }
                }
                try {
                    return new FunctionDefinition(name, resultType, (PatternListRow) new PatternListRow(l).at(pcp)).at(cp);
                }catch (IllegalArgumentException e){
                    throw new ParserException(ft, e.getMessage());
                }

            case VAL:
                advance();
                Pattern pat = pat();
                if(current_rep() != DEFEQUAL) throw new ParserException(current(), "Expected \"=\"");
                advance();
                return new ValueDefinition(pat, exp()).at(cp);
            case DATATYPE:
                Token tok = current();
                advance();
                List<TypeVariable> tvs = new ArrayList<>();
                switch (current_rep()) {
                    case LPAR:
                        loop:
                        while (true) {
                            switch (current_rep()) {
                                case TYPE_TVAR:
                                    tvs.add(env.translateUserProvidedVar(((Token.IDToken) current()).value()));
                                    advance();
                                    break;
                                case TYPE_TVAR_EQ:
                                    tvs.add(env.translateUserProvidedVar(((Token.IDToken) current()).value(), true));
                                    advance();
                                    break;
                                default:
                                    throw new ParserException(current(), "Expected a type variable");
                            }
                            switch (current_rep()) {
                                case COMMA:
                                    advance();
                                    continue loop;
                                case RPAR:
                                    advance();
                                    break loop;
                                default:
                                    throw new ParserException(current(), "expected \")\" or \",\"");
                            }
                        }
                        break;
                    case TYPE_TVAR:
                        tvs.add(env.translateUserProvidedVar(((Token.IDToken) current()).value()));
                        advance();
                        break;
                    case TYPE_TVAR_EQ:
                        tvs.add(env.translateUserProvidedVar(((Token.IDToken) current()).value(), true));
                        advance();
                        break;
                    case TYPE_IDENT:
                        break;
                    default:
                        throw new ParserException(current(), "Expected \"(\", a type variable or a type name");

                }
                if(current_rep() != TYPE_IDENT) throw new ParserException(current(), "Expected a type name");
                name = ((Token.IDToken)current()).value();
                advance();
                if(current_rep() != DEFEQUAL) throw new ParserException(current(), "Expected \"=\"");
                advance();
                Datatype.DatatypeDef dtdef = new Datatype.DatatypeDef(name, tvs);
                try{
                    env.addDatatypeDef(name, dtdef);
                }catch (IllegalArgumentException e){
                    throw new ParserException(tok, e.getMessage());
                }
                loop: while(true){
                    if(current_rep() != TYPE_IDENT) throw new ParserException(current(), "Expected a dtpart name");
                    String pn = ((Token.IDToken)current()).value();
                    advance();
                    switch (current_rep()){
                        case OF:
                            advance();
                            Type t = ty();
                            dtdef.addNewConstr(pn, tvs, t, env.canEquality(t));
                            if(current_rep() == VBAR){
                                advance();
                                continue loop;
                            }else break loop;
                        case VBAR:
                            dtdef.addAtom(pn);
                            advance();
                            continue loop;
                        default:
                            dtdef.addAtom(pn);
                            break loop;
                    }
                }
                try {
                    env.validateDatatypeDef(dtdef);
                }catch (IllegalArgumentException e){
                    throw new ParserException(tok, e.getMessage());
                }
                return new DatatypeDefinition(dtdef, tvs);
            default:
                Token current = current();
                try {
                    Expression e = exp();
                    return new ValueDefinition(new VariablePattern("it", env.newType()), e).at(e.position());
                }catch (ParserException e) {
                    throw e;
                }
        }
    }

    protected Type ty() throws ParserException {
        return tyHelper.parse();
    }

    protected Expression exp() throws ParserException{
        return expHelper.parse();
    }

    protected Pattern pat() throws ParserException {
        return patternHelper.parse();
    }

    protected PatternList patlist() throws ParserException {
        return patternHelper.patlist();
    }

    protected PatternRow patrow() throws ParserException {
        return patternHelper.patrow();
    }

    protected ConstantValue con() throws ParserException {
        Token current = current();
        if(current() instanceof Token.IntegerToken){
            advance();
            return new ConstantValue.IntegerValue(((Token.IntegerToken) current).value());
        }else{
            switch (current_rep()){
                case TRUE:
                    advance();
                    return new ConstantValue.BooleanValue(true);
                case FALSE:
                    advance();
                    return new ConstantValue.BooleanValue(false);
                default: throw new ParserException(current(), "Expected a constant");
            }
        }
    }


}
