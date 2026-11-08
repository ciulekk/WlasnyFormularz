package com.example.wlasnyformularz;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RadioGroup radioGroupSize;
    private Spinner spinnerDough;
    private CheckBox checkCheese;
    private CheckBox checkHam;
    private CheckBox checkMushrooms;
    private CheckBox checkOlives;
    private Switch switchExtraCheese;
    private SeekBar seekBarQuantity;
    private TextView textQuantity;
    private EditText editAddress;
    private EditText editPhone;
    private EditText editNotes;
    private Button btnOrder;

    private String lastOrderSummary = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // onCreate tylko podpina i konfiguruje
        bindViews();
        setupSpinner();
        setupSeekBar();
        setupListeners();
        setupOrderButton();
    }


    private void bindViews() {
        radioGroupSize = findViewById(R.id.radioGroupSize);
        spinnerDough = findViewById(R.id.spinnerDough);
        checkCheese = findViewById(R.id.checkCheese);
        checkHam = findViewById(R.id.checkHam);
        checkMushrooms = findViewById(R.id.checkMushrooms);
        checkOlives = findViewById(R.id.checkOlives);
        switchExtraCheese = findViewById(R.id.switchExtraCheese);
        seekBarQuantity = findViewById(R.id.seekBarQuantity);
        textQuantity = findViewById(R.id.textQuantity);
        editAddress = findViewById(R.id.editAddress);
        editPhone = findViewById(R.id.editPhone);
        editNotes = findViewById(R.id.editNotes);
        btnOrder = findViewById(R.id.btnOrder);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.ciasto_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDough.setAdapter(adapter);
    }

    private void setupSeekBar() {
        seekBarQuantity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = Math.max(progress, 1);
                textQuantity.setText("Ilość: " + value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupListeners() {
        radioGroupSize.setOnCheckedChangeListener((group, checkedId) -> {
            updateExtraCheeseVisibility(checkedId);
        });
    }

    private void setupOrderButton() {
        btnOrder.setOnClickListener(v -> {
            if (validateForm()) {
                showConfirmationDialog();
            }
        });
    }


    private void updateExtraCheeseVisibility(int checkedId) {
        if (checkedId == R.id.radioLarge) {
            switchExtraCheese.setVisibility(View.VISIBLE);
        } else {
            switchExtraCheese.setVisibility(View.GONE);
            switchExtraCheese.setChecked(false);
        }
    }

    private boolean validateForm() {
        // 1. Rozmiar
        if (radioGroupSize.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Wybierz rozmiar pizzy", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 2. Adres
        String address = editAddress.getText().toString().trim();
        if (address.isEmpty()) {
            editAddress.setError("Adres jest wymagany");
            editAddress.requestFocus();
            return false;
        } else {
            editAddress.setError(null);
        }

        // 3. Telefon
        String phone = editPhone.getText().toString().trim();
        if (phone.length() < 9) {
            editPhone.setError("Podaj prawidłowy numer telefonu (9 cyfr)");
            editPhone.requestFocus();
            return false;
        } else {
            editPhone.setError(null);
        }

        return true;
    }

    private void showConfirmationDialog() {
        String summary = buildOrderSummary();

        new AlertDialog.Builder(this)
                .setTitle("Potwierdzenie zamówienia")
                .setMessage(summary)
                .setPositiveButton("Tak, zamów", (dialog, which) -> placeOrder(summary))
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private String buildOrderSummary() {
        String size;
        int checkedId = radioGroupSize.getCheckedRadioButtonId();
        if (checkedId == R.id.radioSmall) {
            size = "Mała";
        } else if (checkedId == R.id.radioMedium) {
            size = "Średnia";
        } else if (checkedId == R.id.radioLarge) {
            size = "Duża";
        } else {
            size = "—";
        }

        String dough = spinnerDough.getSelectedItem().toString();

        List<String> toppings = new ArrayList<>();
        if (checkCheese.isChecked()) toppings.add("Extra ser");
        if (checkHam.isChecked()) toppings.add("Szynka");
        if (checkMushrooms.isChecked()) toppings.add("Pieczarki");
        if (checkOlives.isChecked()) toppings.add("Oliwki");
        if (switchExtraCheese.isChecked()) toppings.add("Ekstra ser za darmo");

        int quantity = Math.max(seekBarQuantity.getProgress(), 1);
        String address = editAddress.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();

        StringBuilder sb = new StringBuilder();
        sb.append("Rozmiar: ").append(size).append("\n");
        sb.append("Ciasto: ").append(dough).append("\n");
        sb.append("Dodatki: ").append(toppings.isEmpty() ? "brak" : String.join(", ", toppings)).append("\n");
        sb.append("Ilość: ").append(quantity).append("\n");
        sb.append("Adres: ").append(address).append("\n");
        sb.append("Telefon: ").append(phone).append("\n");
        if (!notes.isEmpty()) {
            sb.append("Uwagi: ").append(notes);
        }

        return sb.toString();
    }

    private void placeOrder(String summary) {
        lastOrderSummary = summary;

        Snackbar.make(
                findViewById(android.R.id.content),
                "Zamówienie złożone!",
                Snackbar.LENGTH_LONG
        ).setAction("Cofnij", v -> {
            Toast.makeText(MainActivity.this, "Zamówienie cofnięte", Toast.LENGTH_SHORT).show();
        }).show();
    }
}


