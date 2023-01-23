package fi.abo.kogni.soile2.qmarkup.typespec;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.IdentityHashMap;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import fi.abo.kogni.soile2.qmarkup.QuestionnaireBuilder;

public class GenerateValidator {
    public static void main(String[] args) {
        FileInputStream is = null;
        FileOutputStream out = null;
        try {
        	
            is = new FileInputStream(GenerateValidator.class.getClassLoader().getResource("spec/t.typespec").getPath());
            out = new FileOutputStream("./Validator.java");
            ANTLRInputStream input = new ANTLRInputStream(is);
            TypeSpecLexer lexer = new TypeSpecLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            TypeSpecParser parser = new TypeSpecParser(tokens);
            parser.setBuildParseTree(true); 
            ParseTree tree = parser.spec();
            ParseTreeWalker walker = new ParseTreeWalker();
            IdentityHashMap<ParserRuleContext, NodeData> nodeData = new IdentityHashMap<>();
            Pass1 p1 = new Pass1(nodeData);
            Pass2 p2 = new Pass2(nodeData);
            walker.walk(p1, tree);
            walker.walk(p2, tree);
            String output = p2.output();
            out.write(output.getBytes());
        } catch (IOException ex) {
            System.err.println("IOException." + ex.getMessage());
        } finally {
            try {
                is.close();
                out.close();
            } catch (IOException ex) {
                System.err.println("File not closed.");
            }
        }
    }
}
