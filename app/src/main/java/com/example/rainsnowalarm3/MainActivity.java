package com.example.rainsnowalarm3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;

import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private GpsTracker gpsTracker;
    private String x = "", y = "", address = "";
    private String date = "", time = "";
    TextView text1, maintext;

    long mNow;
    Date mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //얘네를 batch 함수로 옮겨야 함
        readExcel("강남구"); //행정시 이름으로 격자값 구하기. 일단은 구 단위로만 검색 가능하도록. 수원시영통구

        final String[] temp = {""};
        String weather ="";
        final WeatherData wd = new WeatherData();
        long now = System.currentTimeMillis();

        Date mDate = new Date(now);

        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMdd");//20211030
        date = simpleDate.format(mDate);
        time = "0300";
        // ex) date = "20210722", time = "0500"

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            String test = "";
            @Override
            public void run() {
                try {

                    temp[0] = wd.lookUpWeather(date, time, x, y); //call by address can't bb
                    Log.i("LOG", "temp[0]"+temp[0]);

                } catch (IOException e) {
                    Log.i("THREE_ERROR1", e.getMessage());
                } catch (JSONException e) {
                    Log.i("THREE_ERROR2", e.getMessage());
                }

            }

        });


        try{
            Thread.sleep(30000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        //should implement those after things above are executed, but are not since they are executed on another thread.
        weather = temp[0]; //
        Log.i("LOG", "weather"+weather);
/*
    21시 => 기온: 13ºC 강수: 없음
    22시 => 기온: 12ºC 강수: 없음
    23시 => 기온: 12ºC 강수: 없음
 */

        text1= (TextView)findViewById(R.id.text1);
        text1.setText(weather);

        //1. 시별로 split
        String[] hours = weather.split("\n");

        //2. '없음' 아닌 것 count + categorize
        int rscnt=0;
        String result = "오늘 "+date.substring(4,6)+"월 "+date.substring(6,8)+"일 ";

        for (int i = 0; i<hours.length; i++) {

            if (!hours[i].contains("없음")) {
                int rsidx = hours[i].indexOf("강수: ");
//                Log.i("LOG", "rsidx "+rsidx);

                result += hours[i].substring(0, 4) + hours[i].substring(rsidx + 4)+", ";

                rscnt++;
            }
        }

        result = result.substring(0, result.length()-2);
        result += " 예정입니다.";
        Log.i("LOG", result);

        maintext= (TextView)findViewById(R.id.maintext);
        maintext.setText(result);
        //3. rscnt > 0인 경우 push notification


    }

    public void readExcel(String localName) {
        Log.i("LOG", "readExcel "+localName);
        try {
            InputStream is = getBaseContext().getResources().getAssets().open("local_name.xls");
            Workbook wb = Workbook.getWorkbook(is);

            if (wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if (sheet != null) {
                    int colTotal = sheet.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet.getColumn(colTotal - 1).length;


                    for (int row = rowIndexStart; row < rowTotal; row++) {
                        String contents = sheet.getCell(1, row).getContents();

                        if (contents.contains(localName)) {
                            x = sheet.getCell(3, row).getContents();
                            y = sheet.getCell(4, row).getContents();
                            row = rowTotal;
                            Log.i("LOG", "x: "+x+"y: "+y);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        } catch (BiffException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        }
        Log.i("격자값", "x = " + x + "  y = " + y);
    }

}