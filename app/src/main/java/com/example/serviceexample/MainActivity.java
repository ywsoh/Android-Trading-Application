package com.example.serviceexample;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{

    static final int MAX_STOCKS = 5;
    private Button add_button;
    private EditText ticker;
    private TableLayout table;
    private int numStocks;
    protected HashMap<String, TableRow> rowMap;
    private BroadcastReceiver myBroadcastReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up layout
        setContentView(R.layout.activitymain);
        add_button = (Button) findViewById(R.id.add_button);
        ticker = (EditText) findViewById(R.id.edit_ticker);
        table = (TableLayout) findViewById(R.id.tableLayout);
        numStocks = 0;
        rowMap = new HashMap<>();

        // set up listener
        myBroadcastReceiver = new MyBroadcastReceiver(new Handler(Looper.getMainLooper()));
        registerReceiver(myBroadcastReceiver, new IntentFilter("DOWNLOAD_COMPLETE"));
        registerReceiver(myBroadcastReceiver, new IntentFilter("CALCULATION_COMPLETE"));

        add_button.setOnClickListener(view -> {
            // get and validate stock ticker
            String tickerStr = ticker.getText().toString().toUpperCase(Locale.ENGLISH);
            if (numStocks >= MAX_STOCKS) { // limit to 5 stocks
                Toast.makeText(getApplicationContext(), "Max stocks reached", Toast.LENGTH_SHORT).show();
                return;
            }
            if (tickerStr.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter a stock ticker", Toast.LENGTH_SHORT).show();
                return;
            }
            ticker.getText().clear(); // reset text input field
            TableRow newRow = createTableRow(table, tickerStr);
            rowMap.put(tickerStr, newRow); // store row for each stock for later use
            numStocks++;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myBroadcastReceiver);
    }

    /**
     * Adds new row to table in-place, returning pointer to new row
     * @param table TableView to modify
     * @param tickerStr Stock ticker to add to new row
     * @return new TableRow
     */
    protected TableRow createTableRow(@NonNull TableLayout table, String tickerStr) {

        // create and configure new row
        TableRow row = new TableRow(MainActivity.this);
        row.setLayoutParams(
            new TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
        );

        row.setGravity(Gravity.CENTER_VERTICAL);

        // create and configure elements in row
        TextView tickerText = new TextView(MainActivity.this);
        tickerText.setTag("tickerText");
        tickerText.setText(tickerStr);
        tickerText.setPadding(30, 10, 10, 10);
        tickerText.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        tickerText.setGravity(Gravity.CENTER_VERTICAL);

        TextView returnText = new TextView(MainActivity.this);
        returnText.setTag("returnText");
        returnText.setText(getString(R.string.not_available));
        returnText.setPadding(30, 10, 10, 10);
        returnText.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        returnText.setGravity(Gravity.CENTER_VERTICAL);

        TextView volatilityText = new TextView(MainActivity.this);
        volatilityText.setTag("volatilityText");
        volatilityText.setPadding(30, 10, 10, 10);
        volatilityText.setText(getString(R.string.not_available));
        volatilityText.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        volatilityText.setGravity(Gravity.CENTER_VERTICAL);

        ImageButton downloadButton = new ImageButton(MainActivity.this);
        downloadButton.setBackgroundColor(Color.WHITE);
        downloadButton.setPadding(10, 10, 10, 10);
        downloadButton.setImageResource(R.drawable.download_icon);
        downloadButton.setOnClickListener(view -> {
            //download data for stock ticker
            Toast.makeText(getApplicationContext(), "Downloading data", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), DataDownloadService.class);
            intent.putExtra("ticker", tickerStr);
            startService(intent);
        });

        ImageButton calculateButton = new ImageButton(MainActivity.this);
        calculateButton.setBackgroundColor(Color.WHITE);
        calculateButton.setPadding(10, 10, 10, 10);
        calculateButton.setImageResource(R.drawable.calculate_icon);
        calculateButton.setOnClickListener(view -> {
            //calculate stats, populate textviews
            Toast.makeText(getApplicationContext(), "Calculating stats", Toast.LENGTH_SHORT).show();
            Intent calcIntent = new Intent(getApplicationContext(), CalculationService.class);
            calcIntent.putExtra("ticker", tickerStr);
            startService(calcIntent);
        });

        ImageButton deleteButton = new ImageButton(MainActivity.this);
        deleteButton.setBackgroundColor(Color.WHITE);
        deleteButton.setPadding(10, 10, 10, 10);
        deleteButton.setImageResource(R.drawable.delete_icon);
        deleteButton.setOnClickListener(view -> {
            //remove row from table
            table.removeView(row);
            rowMap.remove(tickerStr);
            numStocks--;
        });

        // add elements to row
        row.addView(tickerText);
        row.addView(returnText);
        row.addView(volatilityText);
        row.addView(downloadButton);
        row.addView(calculateButton);
        row.addView(deleteButton);
        table.addView(row);

        return row;
    }
}