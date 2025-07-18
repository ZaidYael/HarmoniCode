package org.example.harmonicode.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.util.Duration;
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
    Semantico semantico = new Semantico();
    analizadorLexico analizador = new analizadorLexico();
    Parser parser;
    @FXML private CodeArea codigoTextArea;
    @FXML private Button btnNuevo;
    @FXML private Button btnAbrir;
    @FXML private Button btnGuardar;
    @FXML private Button btnCompilar;
    @FXML private Button btnEjecutar;
    @FXML private TextArea resultado;
    @FXML private TextArea lexicoArea;
    @FXML private TextArea sintacticoArea;
    @FXML private TextArea semanticoArea;



    @FXML 
    public void initialize() {
        btnEjecutar.setDisable(true);

        codigoTextArea.setParagraphGraphicFactory(LineNumberFactory.get(codigoTextArea));

        codigoTextArea.textProperty().addListener((obs, oldText, newText) -> {
            codigoTextArea.setStyleSpans(0, computeHighlighting(newText));
        });
        EventHandler<KeyEvent> keyEventHandler = event -> {
            btnEjecutar.setDisable(true);
        };

        codigoTextArea.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
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
        File directorioInicial = new File("./src");
        fileChooser.setInitialDirectory(directorioInicial);

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
        File dirInicial = new File(".");
        fileChooser.setInitialDirectory(dirInicial);
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
        iniciales();
        String codigo = codigoTextArea.getText();
        var tokens = analizador.analizar(codigo);
        if(!(codigo.length()==0)) {
            // ANÁLISIS LÉXICO
            StringBuilder lexico = new StringBuilder();
            lexico.append(String.format("%-15s %-15s %-5s %-7s%n", "Lexema", "Tipo", "Fila", "Columna"));
            lexico.append("-------------------------------------------------------\n");
            for (Token token : tokens) {
                lexico.append(String.format("%-15s %-15s %-5d %-7d%n",
                        token.getLexema(),
                        token.getTipo(),
                        token.getFila(),
                        token.getColumna()
                ));
            }

            // ANÁLISIS SINTÁCTICO
            parser = new Parser(tokens);
            String sintactico = parser.parse();

            // ANÁLISIS SEMÁNTICO
            semantico.ejecucion.setLength(0);
            String semanticoStr = semantico.analizar(tokens);

            // RESULTADO COMPLETO
            escribirresultado(lexicoArea, sintacticoArea, semanticoArea, lexico.toString(), sintactico, semanticoStr);
        }
    }

    @FXML
    private void nuevoArchivo() {
        codigoTextArea.clear();
        iniciales();
    }

    @FXML
    private void ejecutarCodigo(){
        String text = semantico.ejecucion.toString();
        System.out.println(semantico.ejecucion.toString());
        resultado.setText(text);
    }
    private void escribirresultado(TextArea lexicoArea, TextArea sintacticoArea, TextArea semanticoArea, String lexico, String sintactico, String semanticoStr) {
        Timeline timeLexico = new Timeline();
        Timeline timeSintactico = new Timeline();
        Timeline timeSemantico = new Timeline();
        timeSemantico.setOnFinished(e -> {
            semanticoArea.setStyle("-fx-border-color: " + decideColor(semantico.state));
            if(semantico.state && parser.state && analizador.state)
                btnEjecutar.setDisable(false);
            else
                btnEjecutar.setDisable(true);
        });
        timeSintactico.setOnFinished(e -> {
            timeSemantico.play();
            sintacticoArea.setStyle("-fx-border-color: "+decideColor(parser.state));
        });
        timeLexico.setOnFinished(e -> {
            timeSintactico.play();
            lexicoArea.setStyle("-fx-border-color: "+decideColor(analizador.state));
        });
        escribirTexto(timeLexico,lexicoArea,lexico,Duration.millis(5));
        escribirTexto(timeSintactico,sintacticoArea,sintactico,Duration.millis(10));
        escribirTexto(timeSemantico,semanticoArea,semanticoStr,Duration.millis(10));
        timeLexico.play();

    }
    private String decideColor(boolean state){
        String color = "";
        if(state)
            color="rgb(0,255,0);";
        else
            color="rgb(255,0,0);";
        return color;
    }
    public void escribirTexto(Timeline time,TextArea label, String mensaje, Duration delay) {
        for (int i = 0; i <= mensaje.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(delay.multiply(i), e -> {
                label.setText(mensaje.substring(0, index));
            });
            time.getKeyFrames().add(keyFrame);
        }
    }
    private void iniciales(){
        lexicoArea.setStyle("-fx-border-color: white");
        sintacticoArea.setStyle("-fx-border-color: white");
        semanticoArea.setStyle("-fx-border-color: white");
        lexicoArea.setText("");
        sintacticoArea.setText("");
        semanticoArea.setText("");
        resultado.setText("");
        btnEjecutar.setDisable(true);
    }
}

