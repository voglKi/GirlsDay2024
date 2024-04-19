package epos.girlsday.calculator;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import epos.girlsday.calculator.databinding.ActivityMainBinding;

import android.widget.Button;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.button0.setOnClickListener(handleClickDefault);
        binding.button1.setOnClickListener(handleClickDefault);
        binding.button2.setOnClickListener(handleClickDefault);
        binding.button3.setOnClickListener(handleClickDefault);
        binding.button4.setOnClickListener(handleClickDefault);
        binding.button5.setOnClickListener(handleClickDefault);
        binding.button6.setOnClickListener(handleClickDefault);
        binding.button7.setOnClickListener(handleClickDefault);
        binding.button8.setOnClickListener(handleClickDefault);
        binding.button9.setOnClickListener(handleClickDefault);
        binding.buttonDecimal.setOnClickListener(handleClickDefault);
        binding.buttonAdd.setOnClickListener(handleClickDefault);
        binding.buttonSubtract.setOnClickListener(handleClickDefault);
        binding.buttonMultiply.setOnClickListener(handleClickDefault);
        binding.buttonDivide.setOnClickListener(handleClickDefault);

        binding.buttonEqual.setOnClickListener(handleClickResult);

        binding.buttonClear.setOnClickListener(handleClickClear);
    }

    private final View.OnClickListener handleClickDefault = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof Button) {
                Button button = (Button) v;
                String buttonText = button.getText().toString();
                String currentText = binding.tvResult.getText().toString(); //hier wird der aktuell angezeigte Eingabe ausgelesen
                List<String> orderedNumberList = Arrays.asList(currentText.split("[-+×÷]"));
                String lastPart = orderedNumberList.get(orderedNumberList.size() - 1);

                // Setze eine 0 voran, wenn ein Leerzeichen eingegeben wird.
                if (currentText.isEmpty() && "+-×÷.".contains(buttonText)) {
                    binding.tvResult.setText("0");
                }

                Pattern pattern = Pattern.compile(".$");
                Matcher matcher = pattern.matcher(currentText);

                // Überprüfe, ob das letzte Zeichen in der Liste enthalten ist und entferne es, wenn diese Bedingung erfüllt ist.
                if (matcher.find() && "+-×÷.".contains(matcher.group()) && "+-×÷.".contains(buttonText)) {
                    binding.tvResult.setText(currentText.substring(0, currentText.length() - 1));
                }

                // Wenn das eingegebene Zeichen ein Punkt ist und wenn die eingegebene Zahl schon einen Punkt hat,
                // wird die Eingabe verworfen. Wenn es nicht zutrifft, wird ein Punkt eingegeben.
                if (buttonText.equals(".")) {
                    if (!lastPart.contains(".")) {
                        binding.tvResult.append(buttonText);
                    }
                } else {
                    binding.tvResult.append(buttonText);
                }
            }
        }
    };

    private final View.OnClickListener handleClickResult = v -> {
        if (v.getId() != binding.buttonEqual.getId()) {     // Überprüfe, ob der betätigte Button nicht der Gleichheits-Button ist.
            return;                                         // Wenn nicht, wird die Methode beendet.
        }
        String text = binding.tvResult.getText().toString(); // Textausgabe auslesen.
        if (text.isEmpty()) {                                // wenn leer, wird die Methode beendet
            return;
        }
        try {                                               // Versuche, das erste Zeichen des Textes in eine Zahl umzuwandeln.
            char firstCharacter = text.charAt(0);
            Integer.parseInt(String.valueOf(firstCharacter));
        } catch (Exception e) {                             // Wenn eine Ausnahme auftritt (d. h. das erste Zeichen ist keine Zahl),
            binding.tvResult.setText("ERROR");              //  wird "ERROR" im Resultatfeld angezeigt und die Methode beendet.
            return;
        }

        //Aufsplitten des Textes ab Rechenzeichen
        List<Float> orderedNumberList = Arrays.stream(text.split("[-+×÷]")).map(Float::parseFloat).collect(Collectors.toList());
        List<Character> orderedOperatorList = new ArrayList<>();

        //String Rechenzeile wird in ein Array umgewandelt und durch iteriert, wobei Rechenzeichen in der Reihenfolge in die Liste orderedOperatorList hinzugefügt werden
        for (char c : text.toCharArray()) {
            if (c == '+' || c == '-' || c == '×' || c == '÷') {
                orderedOperatorList.add(c);
            }
        }

        //Wenn ein Rechenzeichen am Ende steht, löschen
        if (orderedNumberList.size() == orderedOperatorList.size()) {
            orderedOperatorList.remove(orderedOperatorList.size() - 1);
        }

        boolean error = false;
        // Punkt-Run (Mal, Geteilt)
        ListIterator<Character> iterator = orderedOperatorList.listIterator();
        while (iterator.hasNext()) {
            Character operator = iterator.next();
            float firstOperand = orderedNumberList.get(iterator.previousIndex());
            float secondOperand = orderedNumberList.get(iterator.nextIndex());
            float result;
            switch (operator) {
                // Multiplication
                case '×':
                    result = firstOperand * secondOperand;
                    orderedNumberList.set(iterator.previousIndex(), result);
                    orderedNumberList.remove(iterator.nextIndex());
                    iterator.remove();
                    break;
                // Division
                case '÷':
                    if (secondOperand == 0) {
                        error = true;
                    } else {
                        result = firstOperand / secondOperand;
                        orderedNumberList.set(iterator.previousIndex(), result);
                        orderedNumberList.remove(iterator.nextIndex());
                        iterator.remove();
                    }
                    break;
                // Addition and Subtraction (do nothing at first)
                default:
                    break;
            }
        }

        float endResult = orderedNumberList.get(0);
        // Strich-Run (Plus, Minus)
        for (int i = 0; i < orderedOperatorList.size(); i++) {
            if (orderedOperatorList.get(i) == '+') {
                endResult = endResult + orderedNumberList.get(i + 1);
            } else {
                endResult = endResult - orderedNumberList.get(i + 1);
            }
        }

    // Wenn ein Fehler aufgetreten ist, wird "ERROR" angezeigt.
    // Andernfalls wird das Ergebnis formatiert und im Ergebnisfeld angezeigt, wobei Kommas durch Punkte ersetzt werden.
        if (error) {
            binding.tvResult.setText("ERROR");
        } else {
            String stringEndResult = Formatter(endResult);
            binding.tvResult.setText(stringEndResult.replace(',', '.'));
        }
    };

    // Löscht den Inhalt der Ausgabe, wenn die Lösch-Schaltfläche geklickt wird.
    private final View.OnClickListener handleClickClear = v -> {
        if (v.getId() == binding.buttonClear.getId()) {
            binding.tvResult.setText("");
        }
    };

    //Begrenzt die Formatierung der Zahlen auf höchstens 2 Nachkommastellen.
    private String Formatter(float value) {
        DecimalFormat formatter = new DecimalFormat("0.##");
        return formatter.format(value);
    }
}