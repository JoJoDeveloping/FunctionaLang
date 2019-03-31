package de.jojomodding.lang.parsing;

import de.jojomodding.lang.ast.def.DatatypeDefinition;
import de.jojomodding.lang.ast.def.Definition;
import de.jojomodding.lang.ast.def.FunctionDefinition;
import de.jojomodding.lang.ast.def.ValueDefinition;
import de.jojomodding.lang.ast.expression.*;
import de.jojomodding.lang.ast.pattern.*;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ParserException;
import de.jojomodding.lang.type.*;
import de.jojomodding.lang.value.AtomValue;
import de.jojomodding.lang.value.ConstantValue;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import static de.jojomodding.lang.parsing.Token.Basic.*;
import static de.jojomodding.lang.ast.expression.OperatorExpression.BinaryOperator;
import static de.jojomodding.lang.ast.expression.OperatorExpression.UnaryOperator;

public class Parser extends Thread{

    private List<Token> lastTokens = new LinkedList();
    private ConcurrentLinkedQueue<Token> pendingTokens;
    private Consumer<Throwable> exceptionHandler;
    private ElabEnvironment env;
    private final Object sync = new Object();
    private Consumer<Definition> def;

    public Parser(Consumer<Definition> cont, Consumer<Throwable> error){
        this.pendingTokens = new ConcurrentLinkedQueue<>();
        this.env = new ElabEnvironment();
        this.def = cont;
        this.exceptionHandler = error;
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

    private Token.Basic current_rep() throws ParserException {
        return current().rep();
    }

    private Token current() throws ParserException {
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

    private Token next() throws ParserException {
        advance();
        return current();
    }

    private void advance(){
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

    private PatternRow patrow() throws ParserException {
        CodePosition cp = current().getPosition();
        List<Map.Entry<Pattern, Expression>> l = new LinkedList<>();
        loop: while(true){
            Pattern p = pat();
            switch (current_rep()){
                case COLON:
                    advance();
                    Type t = ty();
                    p = new AnnotatedPattern(p, t).at(p.position());
                    if(current_rep() != BIGARROW) throw new ParserException(current(), "Expected \"=>\"");
                case BIGARROW:
                    advance();
                    break;
                default: throw new ParserException(current(), "Expected \"=>\" or \":\"");
            }
            l.add(Map.entry(p, exp()));
            switch (current_rep()){
                case VBAR:
                    advance();
                    continue loop;
                default: break loop;
            }
        }
        return (PatternRow) new PatternRow(l).at(cp);
    }

    private PatternList patlist() throws ParserException {
        CodePosition cp = current().getPosition();
        List<Pattern> pl = new LinkedList<>();
        loop: while (true){
            switch (current_rep()){
                case TYPE_IDENT:
                case WILDCARD:
                case LPAR:
                case CLPAR:
                case TYPE_INTEGER:
                case TRUE:
                case FALSE:
                    pl.add(ppat());
                    continue loop;
                default:
                    break loop;
            }
        }
        if(pl.size() == 0)
            throw new ParserException(current(), "Expected a pattern");
        return new PatternList(pl);
    }

    private Pattern pat() throws ParserException {
        Pattern otr = apat();
        switch (current_rep()){
            case COLON:
                advance();
                Type t = ty();
                return new AnnotatedPattern(otr, t).at(otr.position());
            default: return otr;
        }
    }

    private Pattern apat() throws ParserException {
        Pattern p = cpat();
        switch (current_rep()){
            case AS:
                advance();
                return new AsPattern(p, cpat()).at(p.position());
            default: return p;
        }
    }


    private Pattern cpat() throws ParserException {
        if(current_rep() == TYPE_IDENT){
            String v = ((Token.IDToken)current()).value();
            CodePosition cp = current().getPosition();
            Optional<Datatype.DatatypeDef.DatatypeConstr> oc = env.isDatatypeConstr(v);
            if(oc.isPresent()){
                advance();
                Pattern p = lpat();
                return new ConstructorPattern(oc.get(), p).at(cp);
            }
        }
        return lpat();
    }

    private Pattern lpat() throws ParserException {
        Pattern p = ppat();
        if(current_rep() == CONS){
            advance();
            Pattern op = lpat();
            Datatype.DatatypeDef.DatatypeConstr lc = env.isDatatypeConstr("op::").get();
            return new ConstructorPattern(lc, new TuplePattern(p, op));
        }
        return p;
    }

    private Pattern ppat() throws ParserException {
        CodePosition cp = current().getPosition();
        switch (current_rep()){
            case TYPE_IDENT:
                String s = ((Token.IDToken)current()).value();
                Optional<Datatype.DatatypeDef> od = env.isDatatypeAtom(s);
                if(od.isPresent()){
                    advance();
                    return new AtomPattern(od.get(), s);
                }
                if(env.isDatatypeConstr(s).isPresent()){
                    throw new ParserException(current(), "Constructor cannot be used as variable");
                }
                advance();
                return new VariablePattern(s, env.newType());
            case WILDCARD:
                advance();
                return new WildcardPattern(env.newType()).at(cp);
            case TYPE_INTEGER:
            case TRUE:
            case FALSE:
                ConstantValue cv = con();
                return new ConstantPattern(cv).at(cp);
            case LPAR:
                List<Pattern> pl = new ArrayList<>();
                advance();
                if(current_rep() != RPAR)
                    loop: while(true){
                        pl.add(pat());
                        switch (current_rep()){
                            case COMMA:
                                advance();
                                continue loop;
                            case RPAR:
                                advance();
                                break loop;
                            default: throw new ParserException(current(), "expected \")\" or \",\"");
                        }
                    }
                else advance();
                if(pl.size() == 1)return pl.get(0);
                return new TuplePattern(pl).at(cp);
            case CLPAR:
                pl = new ArrayList<>();
                advance();
                if(current_rep() != CRPAR)
                    loop: while(true){
                        pl.add(pat());
                        switch (current_rep()){
                            case COMMA:
                                advance();
                                continue loop;
                            case CRPAR:
                                advance();
                                break loop;
                            default: throw new ParserException(current(), "expected \"]\" or \",\"");
                        }
                    }
                else advance();
                return new ListPattern(pl).at(cp);
            default: throw new ParserException(current(), "Expected a pattern");
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
                    throw new ParserException(current, "Expected a definition");
                }
        }
    }

    private Type ty() throws ParserException {
        Type pty = sty();
        if(current_rep()== ARROW){
            advance();
            Type nty = ty();
            return new FunctionType(pty, nty);
        }
        return pty;
    }

    private Type sty() throws ParserException {
        List<Type> lt = new LinkedList<>();
        loop: while (true){
            lt.add(dty());
            switch (current_rep()){
                case STAR:
                    advance();
                    continue;
                default: break loop;
            }
        }
        if(lt.size() == 1) return lt.get(0);
        return new TupleType(lt.toArray(new Type[0]));
    }

    private Type dty() throws ParserException {
        return dty_(pty());
    }

    private Type dty_(Type given) throws ParserException {
        if(current_rep() == TYPE_IDENT){
            Datatype.DatatypeDef dtdef = env.getDatatypeDef(((Token.IDToken)current()).value());
            if(dtdef == null) throw new ParserException(current(), "Unknown datatype "+((Token.IDToken)current()).value());
            if(1 != dtdef.getAmountOfSpecifyingTypes()) throw new ParserException(current(), "Type"+dtdef.getName()+" requires "+dtdef.getAmountOfSpecifyingTypes()+" subtypes, 1 given!");
            advance();
            return dty_(new Datatype(dtdef, List.of(given)));
        }else return given;
    }

    private Type pty() throws ParserException {
        switch (current_rep()){
            case INT:
                advance();
                return BaseType.INT;
            case BOOL:
                advance();
                return BaseType.BOOL;
            case LPAR:
                advance();
                Type t = ty();
                switch (current_rep()){
                    case RPAR:
                        advance();
                        return t;
                    case COMMA:
                        advance();
                        List<Type> st = new ArrayList<Type>();
                        st.add(t);
                        loop: while (true){
                            st.add(ty());
                            switch (current_rep()){
                                case COMMA:
                                    advance();
                                    continue loop;
                                case RPAR:
                                    advance();
                                    if(current_rep() != TYPE_IDENT) throw new ParserException(current(), "Expected a custom type name");
                                    Datatype.DatatypeDef dtdef = env.getDatatypeDef(((Token.IDToken)current()).value());
                                    if(dtdef == null) throw new ParserException(current(), "Unknown datatype "+((Token.IDToken)current()).value());
                                    if(st.size() != dtdef.getAmountOfSpecifyingTypes()) throw new ParserException(current(), "Type"+dtdef.getName()+" requires "+dtdef.getAmountOfSpecifyingTypes()+" subtypes, "+st.size()+" given!");
                                    return new Datatype(dtdef, st);
                                default:
                                    throw new ParserException(current(), "expected \")\" or \",\"");
                            }
                        }
                    default:
                        throw new ParserException(current(), "expected \")\" or \",\"");
                }
            case TYPE_TVAR:
                Token.IDToken to = (Token.IDToken) current();
                advance();
                return env.translateUserProvidedVar(to.value());
            case TYPE_TVAR_EQ:
                to = (Token.IDToken) current();
                advance();
                TypeVariable tv = env.translateUserProvidedVar(to.value(), true);
                return env.withEquality(tv);
            case TYPE_IDENT:
                Datatype.DatatypeDef dtdef = env.getDatatypeDef(((Token.IDToken)current()).value());
                if(dtdef == null) throw new ParserException(current(), "Unknown datatype "+((Token.IDToken)current()).value());
                if(0 != dtdef.getAmountOfSpecifyingTypes()) throw new ParserException(current(), "Type"+dtdef.getName()+" requires "+dtdef.getAmountOfSpecifyingTypes()+" subtypes, 0 given!");
                advance();
                return new Datatype(dtdef, List.of());
            default:
                throw new ParserException(current(), "expected \"int\", \"bool\", \"(\" ty \")\" or a type identifier");
        }
    }

    private Expression exp() throws ParserException {
        switch (current_rep()){
            case FN:
                CodePosition cp = current().getPosition();
                advance();
                PatternRow pr = patrow();
                return new AbstractionExpression(pr).at(cp);
            case IF:
                cp = current().getPosition();
                advance();
                Expression cond = exp();
                if(current_rep() == THEN){
                    advance();
                    Expression tc = exp();
                    if(current_rep() == ELSE){
                        advance();
                        Expression ec = exp();
                        return new ConditionalExpression(cond, tc, ec).at(cp);
                    }else throw new ParserException(current(), "expected \"else\"");
                }else throw new ParserException(current(), "expected \"then\"");
            default: return anexp();
        }
    }

    private Expression anexp() throws ParserException {
        Expression p = oexp_(cexp());
        switch (current_rep()){
            case COLON:
                advance();
                Type t = ty();
                return new TypeAnnotatedExpression(p, t).at(p.position());
            default: return p;
        }

    }

    private Expression oexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case OR:
                advance();
                return oexp_(new OperatorExpression(given, BinaryOperator.OR, exp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression cexp() throws ParserException {
        return cexp_(xexp());
    }


    private Expression cexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case AND:
                advance();
                return cexp_(new OperatorExpression(given, BinaryOperator.ADD, cexp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression xexp() throws ParserException {
        return xexp_(eqexp());
    }


    private Expression xexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case XOR:
                advance();
                return xexp_(new OperatorExpression(given, BinaryOperator.XOR, xexp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression eqexp() throws ParserException {
        Expression given = cmpexp();
        switch (current_rep()){
            case EQUAL:
                advance();
                return new OperatorExpression(given, BinaryOperator.EQUAL, cmpexp()).at(given.position());
            case UNEQUAL:
                advance();
                return new OperatorExpression(given, BinaryOperator.UNEQUAL, cmpexp()).at(given.position());
            default:
                return given;
        }
    }

    private Expression cmpexp() throws ParserException {
        Expression given = lexp();
        switch (current_rep()){
            case GREATER:
                advance();
                return new OperatorExpression(given, BinaryOperator.GREATER, lexp()).at(given.position());
            case LESS:
                advance();
                return new OperatorExpression(given, BinaryOperator.LESS, lexp()).at(given.position());
            case GREATEREQUAL:
                advance();
                return new OperatorExpression(given, BinaryOperator.GEQ, lexp()).at(given.position());
            case LESSEQUAL:
                advance();
                return new OperatorExpression(given, BinaryOperator.LEQ, lexp()).at(given.position());
            default:
                return given;
        }
    }

    private Expression lexp() throws ParserException {
        Expression e = aexp();
        switch (current_rep()){
            case CONS:
                CodePosition cp = current().getPosition();
                advance();
                return new OperatorExpression(e, BinaryOperator.CONS, lexp()).at(e.position());
            default: return e;
        }
    }

    private Expression aexp() throws ParserException {
        return aexp_(mexp());
    }


    private Expression aexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case PLUS:
                advance();
                return aexp_(new OperatorExpression(given, BinaryOperator.ADD, aexp()).at(given.position()));
            case MINUS:
                advance();
                return aexp_(new OperatorExpression(given, BinaryOperator.SUB, aexp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression mexp() throws ParserException {
        return mexp_(uexp());
    }


    private Expression mexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case STAR:
                advance();
                return mexp_(new OperatorExpression(given, BinaryOperator.MUL, mexp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression uexp() throws ParserException {
        switch (current_rep()){
            case PLUS:
                advance();
                return uexp();
            case MINUS:
                CodePosition cp = current().getPosition();
                advance();
                return new OperatorExpression(UnaryOperator.NEG, uexp()).at(cp);
            case NOT:
                cp = current().getPosition();
                advance();
                return new OperatorExpression(UnaryOperator.NOT, uexp()).at(cp);
            case TYPE_PROJ:
                cp = current().getPosition();
                Token.IntegerToken it = (Token.IntegerToken) current();
                advance();
                try{
                    return new ProjectionExpression(it.value().intValueExact(), uexp()).at(cp);
                }catch (ArithmeticException e){
                    throw new ParserException(it, "Projection index too large!");
                }
            default:
                return sexp();
        }
    }

    private Expression sexp() throws ParserException {
        return sexp_(pexp());
    }


    private Expression sexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case TRUE:
            case FALSE:
            case CLPAR:
            case LPAR:
            case TYPE_INTEGER:
            case TYPE_IDENT:
                return sexp_(new ApplicationExpression(given, pexp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression pexp() throws ParserException {
        Token current = current();
        if(current() instanceof Token.IDToken){
            advance();
            return new VariableExpression(((Token.IDToken) current).value()).at(current.getPosition());
        }else if(current_rep() == LPAR){
            advance();
            if(current_rep() == RPAR){
                advance();
                return new TupleExpression(/*unit*/).at(current.getPosition());
            }
            else{
                List<Expression> exprs = new LinkedList<>();
                loop: while(true){
                    exprs.add(exp());
                    switch (current_rep()){
                        case COMMA:
                            advance();
                            continue;
                        case RPAR:
                            advance();
                            break loop;
                    }
                }
                if(exprs.size() == 1) return exprs.get(0);
                return new TupleExpression(exprs).at(current.getPosition());
            }
        }else if(current_rep() == CLPAR) {
            advance();
            Datatype.DatatypeDef.DatatypeConstr c = env.isDatatypeConstr("op::").get();
            if(current_rep() == CRPAR){
                advance();
                return new ConstantExpression(new AtomValue(env.getDatatypeDef("list"), "nil"));
            }
            else{
                List<Expression> exprs = new LinkedList<>();
                loop: while(true){
                    exprs.add(exp());
                    switch (current_rep()){
                        case COMMA:
                            advance();
                            continue;
                        case CRPAR:
                            advance();
                            break loop;
                    }
                }
                return new ListExpression(exprs).at(current.getPosition());
            }
        }else{
            try {
                return new ConstantExpression(con()).at(current.getPosition());
            }catch (ParserException p){
                throw new ParserException(p.getToken(), "Expected an expression");
            }
        }
    }

    private ConstantValue con() throws ParserException {
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
