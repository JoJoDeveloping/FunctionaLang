package de.jojomodding.lang;

import de.jojomodding.lang.ast.def.Definition;
import de.jojomodding.lang.env.BasicTypes;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.exception.*;
import de.jojomodding.lang.parsing.CodePosition;
import de.jojomodding.lang.parsing.Lexer;
import de.jojomodding.lang.parsing.Parser;
import de.jojomodding.lang.parsing.Token;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.io.InputStream;
import java.util.List;

public class Interpreter {

    private Lexer l;
    private Parser p;
    private Environment<Value> evalEnv;
    private ElabEnvironment elabEnv;

    public Interpreter(InputStream source){
        this.p = new Parser(this::executeDefinition, this::errorHandler);
        this.evalEnv = new Environment<>();
        this.elabEnv = p.getElabEnviron();
        BasicTypes bt = new BasicTypes();
        bt.initElab(elabEnv);
        bt.initEval(evalEnv);
        p.start();
        this.l = new Lexer(source, this::accept);
    }

    public void run(){
        try {
            l.lex();
        } catch (LexerException e) {
            errorHandler(e);
        }
    }

    private void errorHandler(Throwable error){
        if(error instanceof LangException){
            System.err.println(buildError((LangException) error, l.lines()));
        }
        error.printStackTrace();
        System.exit(1);
    }


    private String buildError(LangException e, List<String> lines){
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(e.format()).append('\n');
            CodePosition cp = e.position();
            if(cp != null) {
                String s = lines.get(cp.line());
                sb.append(s).append('\n');
                for(int i = 1; i < cp.charInLine(); i++)
                    sb.append(' ');
                sb.append('^');
            }
        }catch (IndexOutOfBoundsException ignore){
            //
        }
        return sb.toString();
    }

    private void executeDefinition(Definition def){
        try {
            List<String> strings = def.elaborate(elabEnv);
            def.evaluate(this.evalEnv);
            for(String s : strings){
                Type t = elabEnv.get(s);
                Value v = this.evalEnv.get(s);
                System.out.println(s+" = "+v.toString()+" : "+elabEnv.resolve(t).deparse(elabEnv));
            }
        } catch (Throwable e) {
            errorHandler(e);
        }
    }

    private void accept(Token tokens) {
        p.continueWith(tokens);
    }
}
