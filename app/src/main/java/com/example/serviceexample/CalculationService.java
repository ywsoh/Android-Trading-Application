package com.example.serviceexample;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class CalculationService extends Service {

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private String ticker = "";

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) { super(looper); }

        @Override
        public void handleMessage(Message msg) {

            Uri CONTENT_URI = Uri.parse("content://com.example.serviceexample.HistoricalDataProvider/history");

            double totalReturns = 0.0;
            int count = 0;

            // get data for stock from database
            String selection = String.format("ticker_name = \"%s\"", ticker);
            Cursor cursor = getApplicationContext().getContentResolver().query(CONTENT_URI, null, selection, null, null);
            if (cursor.moveToFirst()) {
                double close = cursor.getDouble(cursor.getColumnIndexOrThrow("close"));
                double open = cursor.getDouble(cursor.getColumnIndexOrThrow("open"));
                double dailyReturn = ((close - open) / open) * 100;
                ArrayList <Double> returnsList = new ArrayList<Double>();
                returnsList.add((Double) dailyReturn);
                totalReturns += dailyReturn;
                count += 1;
                while (!cursor.isAfterLast() && count <= 184) {
                    close = cursor.getDouble(cursor.getColumnIndexOrThrow("close"));
                    open = cursor.getDouble(cursor.getColumnIndexOrThrow("open"));
                    dailyReturn = ((close - open) / open) * 100;
                    returnsList.add((Double) dailyReturn);
                    totalReturns += dailyReturn;
                    count++;
                    cursor.moveToNext();
                    Log.v("data", close + "");
                }

                // calculate stats
                Double annualizedVolatility = calculateSD(returnsList) * Math.sqrt(184);
                Log.v("returnsList", Integer.toString(returnsList.size()));
                Double annualizedReturn = totalReturns / 184;

                Intent intent = new Intent("CALCULATION_COMPLETE");
                intent.putExtra("stock", ticker);
                intent.putExtra("return", annualizedReturn);
                intent.putExtra("volatility", annualizedVolatility);
                sendBroadcast(intent);

            } else {
                Toast.makeText(getApplicationContext(), "No data", Toast.LENGTH_SHORT).show();
            }

            cursor.close();
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("CalculatorService", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        ticker = intent.getStringExtra("ticker");
        Toast.makeText(this, "calculation starting", Toast.LENGTH_SHORT).show();

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy(){ Toast.makeText(this, "calculation done", Toast.LENGTH_SHORT).show(); }

    //helper method for calculating SD
    private static Double calculateSD(ArrayList<Double> numArray)
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.size();

        for(Double num : numArray) {
            sum += num;
        }

        Double mean = sum/length;

        for(Double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/length);
    }
}



