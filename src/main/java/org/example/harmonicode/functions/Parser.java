package org.example.harmonicode.functions;


import org.example.harmonicode.models.Token;
import org.example.harmonicode.models.Tokens;

import java.util.List;

//Analisis sintactico

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final StringBuffer result = new StringBuffer();


    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public String parse() {
        System.out.println("Entra a metodo parse");
        while (!isAtEnd()) {
            String instruccion = parseStatement();
            result.append(instruccion);
            if (instruccion.contains("Error")) break;  // Detiene en caso de error
        }

        return result.toString();
    }

    private String parseStatement() {
        System.out.println("Entra a parseStatement");
        Token token = peek();
        System.out.println(token +"token a tratar");
        switch (token.getTipo()) {

            case Tokens.Operacion -> {
                return parseOperation();
            }
            case Tokens.Declaracion -> {
                return parseDeclaration();
            }
            default -> {
                advance(); // Avanza 1 token inválido
                return error(token, "Error Instrucción no válida o desconocida").getMessage() + "\n";
            }
        }
    }

    private String parseDeclaration() {
        System.out.println("Entra a parseDeclaration");
        Token type = advance();
        System.out.println(type.getTipo()+"Valor de variable type");
        Token name = consume(Tokens.Identificador, "Error Se esperaba nombre de variable.");
        System.out.println(name.getTipo()+"Valor de variable name");
        consume(Tokens.Asignacion, "Error Se esperaba '='.");
        Token constante = consume(Tokens.Constante, "Error Se esperaba una constante.");
        consume(Tokens.PuntoYComa, "Error Se esperaba ';' al final.");
        return "Declaración: " + type.getTipo() + " " + name.getLexema() + " = " + constante + "\n";
    }

    private String parseOperation() {
        System.out.println("Entra a parseOperation");
        advance();
        Token par = consume(Tokens.ParentesisIzq, "Error Se esperaba una parentesis ( .");
        Token var1 = consume(Tokens.Identificador, "Error Se esperaba una identificador ");
        Token coma = consume(Tokens.Coma, "Error Se esperaba una coma ");
        Token var2 = consume(Tokens.Identificador, "Error Se esperaba una identificador ");
        Token parD = consume(Tokens.ParentesisDer, "Error Se esperaba una parentesis ) .");
        Token punt = consume(Tokens.PuntoYComa, "Error Se esperaba ';' al final.");
        return "Operacion: " + par.getLexema()+ var1.getLexema()+ coma.getLexema()+ var2.getLexema()+parD.getLexema() +punt.getLexema() + "\n";
    }


    // Helpers

//    private boolean match(Tokens... types) {
//        System.out.println("Entra a match");
//        for (Tokens type : types) {
//            if (check(type)) {
//                advance();
//                return true;
//            }
//        }
//        return false;
//    }

    private Token consume(Tokens type, String message) {
        System.out.println("Entra a consume");
        if (check(type)) return advance();
        result.append(peek()+": " +message+"\n");
        return peek();
    }


    private boolean check(Tokens type) {
        System.out.println("Entra a check");
        while(peek().getTipo() == Tokens.Espacio){
            advance();
        }
        if (isAtEnd()) return false;
        System.out.println(peek().getTipo()+" Valor de peek()getTipo");
        return peek().getTipo() == type;
    }

    private Token advance() {
        System.out.println("Entra a advance");
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        System.out.println("Entra a isAtEnd");
        System.out.println(tokens.size()+"token size");
        System.out.println(current+"current token size");
        return current >= tokens.size();
    }

    private Token peek() {
        System.out.println("Entra a peek");
        if (isAtEnd()) {
            return new Token("Fin del código", Tokens.EOF, -1, -1); // Token dummy para EOF
        }
        return tokens.get(current);
    }


    private Token previous() {
        System.out.println("Entra a previous");
        return tokens.get(current - 1);
    }
//    private Token next() {
//        System.out.println("Entra a next");
//        return tokens.get(current + 1);
//    }

    private RuntimeException error(Token token, String message) {
        current ++;
        return new RuntimeException("Error en línea " + token.getLine() + ": " + message);
    }


}