package org.example.harmonicode.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import org.example.harmonicode.functions.Parser;
import org.example.harmonicode.functions.Semantico;
import org.example.harmonicode.models.Token;
import org.example.harmonicode.functions.analizadorLexico;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class compiladorController {
    @FXML
    private Label welcomeText;
    @FXML private CodeArea codigoTextArea;
    @FXML private Button btnNuevo;
    @FXML private Button btnAbrir;
    @FXML private Button btnGuardar;
    @FXML private Button btnCompilar;
    @FXML private Button btnEjecutar;
    @FXML private TextArea texto;
    @FXML private TextArea lexicoArea;
    @FXML private TextArea sintacticoArea;
    @FXML private TextArea semanticoArea;


    @FXML 
    public void initialize() {
        btnEjecutar.setVisible(false);

        codigoTextArea.setParagraphGraphicFactory(LineNumberFactory.get(codigoTextArea));

        codigoTextArea.textProperty().addListener((obs, oldText, newText) -> {
            codigoTextArea.setStyleSpans(0, computeHighlighting(newText));
        });
    }

    //Esto es para remarcar las palabras reservadas
    private static final String[] KEYWORDS = new String[] {
            "transponer", "Transponer", "Registro", "registro", "invertir", "Invertir",
            "modular", "Modular", "rotar", "Rotar"
    };
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
    );
    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastKwEnd = 0;
        while (matcher.find()) {
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            if (matcher.group("KEYWORD") != null) {
                spansBuilder.add(Collections.singleton("keyword"), matcher.end() - matcher.start());
            }
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    @FXML
    private void abrirArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir archivo de código");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"),
                new FileChooser.ExtensionFilter("Archivos fuente", "*.hm")
        );

        File archivoSeleccionado = fileChooser.showOpenDialog(codigoTextArea.getScene().getWindow());

        if (archivoSeleccionado != null) {
            try {
                String contenido = Files.readString(archivoSeleccionado.toPath());
                codigoTextArea.replaceText(contenido);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void guardarArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar archivo de código");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos .harmoni", "*.hm")
        );

        File archivoGuardar = fileChooser.showSaveDialog(codigoTextArea.getScene().getWindow());

        if (archivoGuardar != null) {
            try {
                String filePath = archivoGuardar.getAbsolutePath();
                if (!filePath.endsWith(".harmoni")) {
                    archivoGuardar = new File(filePath + ".hm");
                }

                Files.writeString(archivoGuardar.toPath(), codigoTextArea.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void compilarCodigo() {

        String codigo = codigoTextArea.getText();
        analizadorLexico analizador = new analizadorLexico();
        var tokens = analizador.analizar(codigo);

        // ANÁLISIS LÉXICO
        StringBuilder lexico = new StringBuilder();
        for (Token token : tokens) {
            lexico.append(token.toString()).append("\n");
        }
        lexicoArea.setText(lexico.toString());

        // ANÁLISIS SINTÁCTICO
        Parser parser = new Parser(tokens);
        String sintactico = parser.parse();
        sintacticoArea.setText(sintactico);

        // ANÁLISIS SEMÁNTICO
        Semantico semantico = new Semantico();
        String semanticoStr = semantico.analizar(tokens);
        if (!semantico.state){
            btnEjecutar.setVisible(true);
        }
        semanticoArea.setText(semanticoStr);

        // RESULTADO COMPLETO
        texto.setText("Compilación finalizada.");
    }



    @FXML
    private void nuevoArchivo() {
        codigoTextArea.clear();
    }

}

