package com.example.janpr.white_board;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.app.Activity;
import android.content.ClipData;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;


public class MainActivity extends Activity implements OnClickListener, OnGesturePerformedListener {

    private DrawingView drawView;
    private float smallBrush, mediumBrush, largeBrush;
    private ImageButton drawBtn, eraseBtn, newBtn, equalsBtn, copyBtn, saveBtn;
    private EditText runningQ;


    GestureLibrary mLibrary;
    String gestureResult="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawView = (DrawingView)findViewById(R.id.drawing);
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);
        drawView.setBrushSize(mediumBrush);
        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);
        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);
        equalsBtn = (ImageButton)findViewById(R.id.equals_btn);
        equalsBtn.setOnClickListener(this);

        runningQ = (EditText)findViewById(R.id.text_field);
        runningQ.setOnClickListener(this);



        //The next 8 lines of code handle a receiving intent
        Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();
//find out what we are dealing with
        String receivedType = receivedIntent.getType();
//make sure it's an action and type we can handle
        if (receivedAction.equals(Intent.ACTION_SEND)) {
            //content is being shared
            if (receivedType.startsWith("text/")) {
                //get the received text
                String receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
                //check that we have a string when receiving
                if (receivedText != null) {
                    //set the received text on the running queue
                    runningQ.setText(receivedText);
                }
            }
        }



        //on create gestures

        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!mLibrary.load()) {
            finish();
        }

        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestureOverlayView);
        gestures.setGestureStrokeAngleThreshold( 80.0f);//need to figure out side effets of this line

        gestures.addOnGesturePerformedListener(this);




    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {

        ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

        if (predictions.size() > 0 && predictions.get(0).score > 1.1) {
            gestureResult += predictions.get(0).name;

            System.out.println(gestureResult);

        }
    }

    public void sendClick(View view){
        TextView t = (TextView) findViewById(R.id.text_field);
        System.out.println("this is the textfield" + t.getText().toString());
        String website = stringEncoder(t.getText().toString());


        setContentView(R.layout.result_screen);


        WebView myWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        myWebView.setWebViewClient(new Callback());


        System.out.println(website);
        myWebView.loadUrl(website);

    }

    public void backClick(View view){
        setContentView(R.layout.activity_main);
        onCreate(new Bundle());


    }

    public void saveClick(View view){
            //save the calculation entered by user
        System.out.println('0');

        TextView t = (TextView) findViewById(R.id.text_field);

        String calc = t.getText().toString();//runningQ.getText().toString().trim();
            System.out.println('1');
            Intent sendIntent = new Intent();
            //indicates the the intent is to send data
            sendIntent.setAction(Intent.ACTION_SEND);
                System.out.println('2');

        //attaches the string we are sending to the other app
            sendIntent.putExtra(Intent.EXTRA_TEXT, calc);
            //defines the MIME type text/plain it is sending
        System.out.println('3');

        sendIntent.setType("text/plain");

            try {
                //displays a chooser with a list of apps that matches the MIME type if this
                //type of app is not installed a message is displayed.
                startActivity(Intent.createChooser(sendIntent, "Save calculation using"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "There are no apps installed to save your file.", Toast.LENGTH_SHORT).show();
            }



    }

    public void copyClick(View view){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        TextView t = (TextView) findViewById(R.id.text_field);
        ClipData clip = ClipData.newPlainText("Equation", t.getText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Saved to Clipboard", Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onClick(View view){


        System.out.println(view.getId());
        if(view.getId()==R.id.draw_btn){
            if(!drawView.getErase()) {
                final Dialog brushDialog = new Dialog(this);
                brushDialog.setTitle("Brush size:");
                brushDialog.setContentView(R.layout.brush_chooser);
                ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setBrushSize(smallBrush);
                        drawView.setLastBrushSize(smallBrush);
                        brushDialog.dismiss();
                    }
                });
                ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setBrushSize(mediumBrush);
                        drawView.setLastBrushSize(mediumBrush);
                        brushDialog.dismiss();
                    }
                });
                ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        drawView.setBrushSize(largeBrush);
                        drawView.setLastBrushSize(largeBrush);
                        brushDialog.dismiss();
                    }
                });
                brushDialog.show();
            }
            drawView.setErase(false);
        }
        else if(view.getId()==R.id.erase_btn){
            if(drawView.getErase()){
                final Dialog brushDialog = new Dialog(this);
                brushDialog.setTitle("Eraser size:");
                brushDialog.setContentView(R.layout.brush_chooser);
                ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        drawView.setBrushSize(smallBrush);
                        brushDialog.dismiss();
                    }
                });
                ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setBrushSize(mediumBrush);
                        brushDialog.dismiss();
                    }
                });
                ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setBrushSize(largeBrush);
                        brushDialog.dismiss();
                    }
                });
                brushDialog.show();
            }
            drawView.setErase(true);
        }
        else if(view.getId()==R.id.new_btn){
            System.out.println("code is running");

            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New equation");
            newDialog.setMessage("Start new equation?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newDialog.show();
            TextView t = (TextView) findViewById(R.id.text_field);
            t.setText("");
            gestureResult ="";

        }
        else if (view.getId() == R.id.equals_btn) {
                //save drawing
                System.out.println(gestureResult);
                drawView.startNew();

                if (gestureResult.equals("1") || gestureResult.equals("2") || gestureResult.equals("3") || gestureResult.equals("6")||
                gestureResult.equals("7") || gestureResult.equals("8") || gestureResult.equals("9") || gestureResult.equals("-") ||
                        gestureResult.equals("0")|| gestureResult.matches("[0-9]+"))
                {
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+ gestureResult);
                }
                else if (gestureResult.equals("1-") || gestureResult.equals("-1")) {
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+"+");
                }
                else if (gestureResult.equals("4_11") || gestureResult.equals("14_1")) {
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+"4");
                }else if (gestureResult.equals("5-") || gestureResult.equals("-5") ||gestureResult.equals("5divide")||gestureResult.equals("divide5")){
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+"5");
                }else if (gestureResult.equals("mult1divide") || gestureResult.equals("dividedivide")
                        ||gestureResult.equals("dividemult1") || gestureResult.equals("-mult1") || gestureResult.equals("mult1-")
                        || gestureResult.equals("mult1mult1")) {
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+"*");
                }else if(gestureResult.equals("divide")){
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+"/");
                }else if(gestureResult.equals("exp")){
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+"^");
                }else if(gestureResult.equals("rp")){
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+")");
                }else if(gestureResult.equals("lp")){
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+"(");
                }else if(gestureResult.equals("--")){
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+"+");
                }else if(gestureResult.equals("period")) {
                    TextView t = (TextView) findViewById(R.id.text_field);
                    t.setText(t.getText()+"+");
                }


                gestureResult ="";
            }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            System.out.println("settings");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String stringEncoder(String expr) {
        String url = "http://m.wolframalpha.com/input/?i=";

        try {
            url += URLEncoder.encode(expr, "UTF-8");
        }

        catch(UnsupportedEncodingException e) {
                System.out.println("The expression could not be encoded in a URL.");
        }

        return url;
    }

    private class Callback extends WebViewClient {  //HERE IS THE MAIN CHANGE.

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }

    }

}
