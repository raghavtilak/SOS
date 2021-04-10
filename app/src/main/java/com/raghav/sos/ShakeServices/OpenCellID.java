package com.raghav.sos.ShakeServices;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

public class OpenCellID {
    String mcc; //Mobile Country Code
    String mnc; //mobile network code
    String cellid; //Cell ID
    String lac; //Location Area Code
    String strURLSent;
    String latitude;
    String longitude;
    Context context;

    OpenCellID(Context context){
        this.context=context;
    }

    public void setMcc(String value) {
        mcc = value;
    }

    public void setMnc(String value) {
        mnc = value;
    }

    public void setCallID(int value) {
        cellid = String.valueOf(value);
    }

    public void setCallLac(int value) {
        lac = String.valueOf(value);
    }


    public void groupURLSent() {
        strURLSent =
                "http://www.opencellid.org/cell/get?key=pk.644c68a93bf0f2b835b775dd4396bf6e&mcc=" + mcc
                        + "&mnc=" + mnc
                        + "&cellid=" + cellid
                        + "&lac=" + lac
                        + "&fmt=txt";
    }

    public void GetOpenCellID() {
        groupURLSent();

            /*
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(strURLSent);
            HttpResponse response = client.execute(request);
            GetOpenCellID_fullresult = EntityUtils.toString(response.getEntity());

             */
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strURLSent,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("Check: ", response);
                        parseXml(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );
//creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        //adding the string request to request queue
        requestQueue.add(stringRequest);

    }


    public void parseXml(String str) {
        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(str)); // pass input whatever xml you have
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    Log.d("TAG", "Start tag " + xpp.getName());
                    if (xpp.getName().equals("cell")) {
                        latitude = xpp.getAttributeValue(0);
                        longitude = xpp.getAttributeValue(1);

                        Log.d("TAG", xpp.getAttributeValue(0));
                        Log.d("TAG", xpp.getAttributeValue(1));
                    }
                }
                eventType = xpp.next();
            }
            Log.d("TAG", "End document");

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}