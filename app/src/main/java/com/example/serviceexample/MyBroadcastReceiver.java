package com.example.serviceexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private final Handler handler;

    public MyBroadcastReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("DOWNLOAD_COMPLETE")) {
            // update stock fields to indicate data download completion
            handler.post(() -> {
                Map<String, TableRow> rowMap =((MainActivity) context).rowMap;
                for (Map.Entry<String, TableRow> entry : rowMap.entrySet()) {
                    String stock = entry.getKey();
                    TableRow row = entry.getValue();
                    if (stock.equals(intent.getStringExtra("stock"))) {
                        TextView returnText = row.findViewWithTag("returnText");
                        returnText.setText("data");
                        TextView volatilityText = row.findViewWithTag("volatilityText");
                        volatilityText.setText("loaded");
                    }
                }
            });
        } else if (intent.getAction().equals("CALCULATION_COMPLETE")) {
            // update stock fields with stats
            handler.post(() -> {
                Map<String, TableRow> rowMap =((MainActivity) context).rowMap;
                for (Map.Entry<String, TableRow> entry : rowMap.entrySet()) {
                    String stock = entry.getKey();
                    TableRow row = entry.getValue();
                    if (stock.equals(intent.getStringExtra("stock"))) {
                        // format numbers to fit UI
                        NumberFormat doubleFormatter = new DecimalFormat("#0.000");
                        TextView returnText = row.findViewWithTag("returnText");
                        String returnString = doubleFormatter.format(intent.getDoubleExtra("return", 0.0));
                        returnText.setText(returnString);
                        TextView volatilityText = row.findViewWithTag("volatilityText");
                        String volatilityString = doubleFormatter.format(intent.getDoubleExtra("volatility", 0.0));
                        volatilityText.setText(volatilityString);
                    }
                }
            });
        }
    }
}
