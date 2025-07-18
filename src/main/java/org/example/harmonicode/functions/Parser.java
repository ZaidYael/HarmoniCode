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
        while (!isAtEnd()) {
            String instruccion = parseStatement();
            result.append(instruccion);
            if (instruccion.contains("Error")) break;  // Detiene en caso de error
        }

        return result.toString();
    }

    private String parseStatement() {
        Token token = peek();
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
        Token type = advance();
        Token name = consume(Tokens.Identificador, "Error Se esperaba nombre de variable.");
        consume(Tokens.Asignacion, "Error Se esperaba '='.");
        Token constante = consume(Tokens.Constante, "Error Se esperaba una constante.");
        consume(Tokens.PuntoYComa, "Error Se esperaba ';' al final.");
        return "Declaración: " + type.getTipo() + " " + name.getLexema() + " = " + constante + "\n";
    }

    private String parseOperation() {
        advance();
        Token par = consume(Tokens.ParentesisIzq, "Error Se esperaba una parentesis ( .");
        Token var1 = consume(Tokens.Identificador, "Error Se esperaba una identificador ");
        Token coma = consume(Tokens.Coma, "Error Se esperaba una coma ");
        Token var2 = consume(Tokens.Identificador, "Error Se esperaba una identificador ");
        Token parD = consume(Tokens.ParentesisDer, "Error Se esperaba una parentesis ) .");
        Token punt = consume(Tokens.PuntoYComa, "Error Se esperaba ';' al final.");
        return "Operacion: " + par.getLexema()+ var1.getLexema()+ coma.getLexema()+ var2.getLexema()+parD.getLexema() +punt.getLexema() + "\n";
    }

    private Token consume(Tokens type, String message) {
        if (check(type)) return advance();
        result.append(peek()+": " +message+"\n");
        return peek();
    }


    private boolean check(Tokens type) {
        while(peek().getTipo() == Tokens.Espacio){
            advance();
        }
        if (isAtEnd()) return false;
        return peek().getTipo() == type;
    }

    private Token advance() {
        System.out.println("Entra a advance");
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private Token peek() {
        if (isAtEnd()) {
            return new Token("Fin del código", Tokens.EOF, -1, -1); // Token dummy para EOF
        }
        return tokens.get(current);
    }


    private Token previous() {
        return tokens.get(current - 1);
    }

    private RuntimeException error(Token token, String message) {
        current ++;
        return new RuntimeException("Error en línea " + token.getFila() + ": " + message);
    }


}