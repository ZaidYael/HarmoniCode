package org.example.harmonicode.models;

public class Token {
    private String lexema;
    private Tokens tipo;
    private int fila;
    private int columna;

    public Token(String lexema, Tokens tipo, int fila, int columna) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.fila = fila;
        this.columna = columna;
    }

    @Override
    public String toString() {
        return "Token{" +
                "lexema='" + lexema + '\'' +
                ", tipo='" + tipo + '\'' +
                ", fila=" + fila +
                ", columna=" + columna +
                '}';
    }

    public String getLexema() { return lexema; }
    public Tokens getTipo() { return tipo; }
    public int getLine() { return fila; }
    public int getColumna() { return columna; }
}
