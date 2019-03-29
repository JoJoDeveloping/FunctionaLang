package de.jojomodding.lang;

import de.jojomodding.lang.ast.def.Definition;
import de.jojomodding.lang.env.BasicTypes;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.exception.*;
import de.jojomodding.lang.parsing.CodePosition;
import de.jojomodding.lang.parsing.Lexer;
import de.jojomodding.lang.parsing.Parser;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.List;

public class Interpreter {

    private String code;

    public Interpreter(String code){
        this.code = code;
    }

    private StringBuilder buildError(StringBuilder sb, LangException e, List<String> lines){
        e.printStackTrace();
        try {
            sb.append(e.format()).append('\n');
            CodePosition cp = e.position();
            if(cp != null) {
                String s = lines.get(cp.line());
                sb.append(s).append('\n');
                for(int i = 1; i < cp.posInLine(); i++)
                    sb.append(' ');
                sb.append('^');
            }
        }catch (IndexOutOfBoundsException ignore){
            //
        }
        return sb;
    }

    public String run(){
        Lexer lex = new Lexer(code);
        StringBuilder sb = new StringBuilder();
        try {
            lex.lex();
            Parser parser = new Parser(lex.getResult());
            ElabEnvironment elabe = parser.getElabEnviron();
            Environment<Value> evale = new Environment<>();
            BasicTypes ee = new BasicTypes();
            ee.initElab(elabe);
            ee.initEval(evale);
            List<Definition> prog = parser.parse();
            for(Definition d : prog){
                List<String> sl = d.elaborate(elabe);
                d.evaluate(evale);
                for(String s : sl){
                    Type t = elabe.resolve(elabe.get(s));
                    Value v = evale.get(s);
                    sb.append(s).append(" = ").append(v).append(" : ").append(t.deparse(elabe)).append('\n');
                }
            }
        }catch (LangException e) {
            buildError(sb, e, lex.lines());
        }catch (Throwable t){
            t.printStackTrace();
            sb.append("ERROR! ").append(t.toString());
        }
        return sb.toString();
    }

}
