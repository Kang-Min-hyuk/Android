package com.test.myapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerTourType, spinnerArea, spinnerSiGu;

    List<String> areaList = new ArrayList<>(); // 지역명 목록
    Map<String, Integer> areaMap = new HashMap<>();  //  지역코드

    List<String> siGuList = new ArrayList<>(); // 시군구명 목록
    Map<String, Integer> siGuMap = new HashMap<>(); // 시군구 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerTourType = (Spinner) findViewById(R.id.spinnerTourType);
        spinnerArea = (Spinner) findViewById(R.id.spinnerArea);
        spinnerSiGu = (Spinner) findViewById(R.id.spinnerSiGu);

        spinnerTourType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) spinnerTourType.getItemAtPosition(position);
                int typeId = getContentTypeId(str);
                Toast.makeText(MainActivity.this, "typeId: " + typeId, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

                 new Thread(new AreaRunnable()).start(); // 지역목록 스피너 반영

        // 지역선택 스피너에 이벤트 연결
        spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String areaName = (String) parent.getItemAtPosition(position);
                int areaCode = areaMap.get(areaName);

                 new Thread(new SiGuRunnable(areaCode)).start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    } // onCreate

    // 지역명, 지역코드 가져와서 지역선택 스피너에 반영
    class AreaRunnable implements Runnable {
        @Override
        public void run() {
            getAreaInfo(1);  // 지역정보 1페이지
            getAreaInfo(2);  // 지역정보 2페이지

            areaList.add(0, "지역선택");
            areaMap.put("지역선택", 0);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<String>(MainActivity.this,
                                    android.R.layout.simple_spinner_dropdown_item, areaList);

                    spinnerArea.setAdapter(adapter);
                }
            });
        }
    } // AreaRunnable

    // 시군구명, 시군구코드 가져와서 시군구선택 스피너에 반영
    class SiGuRunnable implements Runnable {
        private int areaCode;

        public SiGuRunnable(int areaCode) {
            this.areaCode = areaCode;
        }

        @Override
        public void run() {
            getSiGuInfo(areaCode);

            siGuList.add(0, "시군구 선택");
            siGuMap.put("시군구 선택", 0);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<String>(MainActivity.this,
                                    android.R.layout.simple_spinner_dropdown_item, siGuList);

                    spinnerSiGu.setAdapter(adapter);
                }
            });
        }
    } // SiGuRunnable

    public void getAreaInfo(int pageNo) {
        if (pageNo == 1) {
            areaList.clear();
            areaMap.clear(); // 1페이지 가져올 경우는 비운 후 채우기
        }

        String urlStr = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaCode"
                + "?ServiceKey=Z%2BvdVssJ5Gu0sQiBIfilHY%2BG6OL062nMq%2BORIyhW8id8kb4lA7izkOrR1NwXxP1iWGg2jsHqK80krE%2FP6Odz5Q%3D%3D"
                + "&MobileOS=AND"
                + "&MobileApp=MyApp"
                + "&pageNo=" + pageNo;
        InputStream is = null;

        try {
            URL url = new URL(urlStr);
            is = url.openStream();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(is));

            NodeList itemList = document.getElementsByTagName("item");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element itemEle = (Element) itemList.item(i);

                NodeList nameList = itemEle.getElementsByTagName("name");
                Element nameEle = (Element) nameList.item(0);
                String name = nameEle.getFirstChild().getNodeValue();

                NodeList codeList = itemEle.getElementsByTagName("code");
                Element codeEle = (Element) codeList.item(0);
                String codeStr = codeEle.getFirstChild().getNodeValue();
                int code = Integer.parseInt(codeStr);

                areaList.add(name);
                areaMap.put(name, code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    } // getAreaInfo

    public void getSiGuInfo(int areaCode) {

        siGuList.clear();
        siGuMap.clear();

        String strUrl = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaCode" +
                "?ServiceKey=Z%2BvdVssJ5Gu0sQiBIfilHY%2BG6OL062nMq%2BORIyhW8id8kb4lA7izkOrR1NwXxP1iWGg2jsHqK80krE%2FP6Odz5Q%3D%3D" +
                "&numOfRows=100" +
                "&MobileOS=AND" +
                "&MobileApp=MyApp" +
                "&areaCode=" + areaCode;
        InputStream is = null;

        try {
            URL url = new URL(strUrl);
            is = url.openStream();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(is));

            NodeList itemList = document.getElementsByTagName("item");
            for (int i = 0; i < itemList.getLength(); i++) {
                Element itemEle = (Element) itemList.item(i);

                NodeList nameList = itemEle.getElementsByTagName("name");
                Element nameEle = (Element) nameList.item(0);
                String name = nameEle.getFirstChild().getNodeValue();

                NodeList codeList = itemEle.getElementsByTagName("code");
                Element codeEle = (Element) codeList.item(0);
                String codeStr = codeEle.getFirstChild().getNodeValue();
                int code = Integer.parseInt(codeStr);

                siGuList.add(name);
                siGuMap.put(name, code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } // getSiGuInfo

    public int getContentTypeId(String typeName) {
        int contentTypeId = 0;

        switch (typeName) {
            case "관광지":
                contentTypeId = 12;
                break;
            case "문화시설":
                contentTypeId = 14;
                break;
            case "축제공연행사":
                contentTypeId = 15;
                break;
            case "여행코스":
                contentTypeId = 25;
                break;
            case "레포츠":
                contentTypeId = 28;
                break;
            case "숙박":
                contentTypeId = 32;
                break;
            case "쇼핑":
                contentTypeId = 38;
                break;
            case "음식점":
                contentTypeId = 39;
                break;
            default:
                contentTypeId = 0;
        }

        return contentTypeId;
    } // getContentTypeId
}
