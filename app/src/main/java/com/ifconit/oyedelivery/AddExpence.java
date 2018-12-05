package com.ifconit.oyedelivery;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Created by Rakhi on 4/1/2017.
 */
public class AddExpence extends AppCompatActivity{
    EditText etDesc,etAmount,etRefNo,etExpenseDate;
    TextInputLayout tilAmount;
    String strRefNo,strAmount,strDesc,strCat,code,message,token,userId,strBaseUrl,selectedDate,picturePath,JsonURL,cws;
    Spinner spCat;
    Button btSubmit,btTakePic;
    ImageView ivClick;
    String [] spValues;
    ProgressDialog pDialog;
    SharedPreferences prefsUid;
    public static final String PREFS_UID="loginUserId";
    private final static int CAMERA_RQ = 6969;
    private final static int PERMISSION_RQ = 84;
    Bitmap pic;
    File file;
    File sourceFile;
    int totalSize = 0;
    private int serverResponseCode = 0;
    TextView tvPercentage;
    public DonutProgress donut_progress;

    private Calendar cal;
    private int day;
    private int month;
    private int year;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expence);

        try{
            //android O fix bug orientation
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        }catch (RuntimeException re){
            re.printStackTrace();
        }

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Add Expense");

        etDesc=(EditText)findViewById(R.id.etDesc);
        etRefNo=(EditText)findViewById(R.id.input_orderRef);
        etAmount=(EditText)findViewById(R.id.input_Amount);
        etExpenseDate=(EditText)findViewById(R.id.input_expenseDate);
        spCat=(Spinner)findViewById(R.id.spCategory);
        btSubmit=(Button)findViewById(R.id.btSubmit);
        tilAmount=(TextInputLayout)findViewById(R.id.input_layout_Amount);
        btTakePic=(Button)findViewById(R.id.btTakePic);
        ivClick=(ImageView)findViewById(R.id.ivClick);
        donut_progress=(DonutProgress) findViewById(R.id.progressBarUpload);
        tvPercentage=(TextView)findViewById(R.id.txtPercentage) ;

        token=GlobalClass.getToken(this);
        strBaseUrl=getApplicationContext().getString(R.string.base_url);

        prefsUid = getSharedPreferences(PREFS_UID, 0);
        userId= prefsUid.getString("uid", "");

        spValues=getResources().getStringArray(R.array.expencecategory_arrays);
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spValues);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCat.setAdapter(dataAdapter);
        spCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                strCat = parent.getItemAtPosition(position).toString().replaceAll(" ", "%20").trim();

                //Toast.makeText(parent.getContext(), "" + item, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //selectedDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

        etExpenseDate.setText(day+"-"+(month+1)+"-"+year);

        etExpenseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DateDialog();
                /*Calendar c=Calendar.getInstance();
                DatePickerDialog mdiDialog =new DatePickerDialog(AddExpence.this,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Toast.makeText(getApplicationContext(),dayOfMonth+"-"+monthOfYear+1+"-"+year,Toast.LENGTH_LONG).show();
                        selectedDate=year+"-"+monthOfYear+1+"-"+dayOfMonth;
                        etExpenseDate.setText(selectedDate);
                    }
                },c.get(Calendar.YEAR), c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));

                mdiDialog.show();*/
            }
        });
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to save videos in external storage
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_RQ);
        }

        btTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File saveDir = null;
                if (ContextCompat.checkSelfPermission(AddExpence.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveDir = new File(Environment.getExternalStorageDirectory(), "MaterialCamera");
                    saveDir.mkdirs();
                }
                new MaterialCamera(AddExpence.this)
                        .stillShot() // launches the Camera in stillshot mode
                        .labelConfirm(R.string.mcam_use_stillshot)
                        .start(CAMERA_RQ);
            }
        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strRefNo=etRefNo.getText().toString().replaceAll(" ", "%20").trim();
                strDesc=etDesc.getText().toString().replaceAll("\n","%20");
                strDesc=strDesc.replaceAll(" ", "%20").trim();
                selectedDate=etExpenseDate.getText().toString().trim();
                submitForm();

            }
        });
    }

    public void DateDialog(){

        DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth)
            {
                Toast.makeText(getApplicationContext(),dayOfMonth+"-"+(monthOfYear+1)+"-"+year,Toast.LENGTH_LONG).show();
                etExpenseDate.setText(dayOfMonth+"-"+(monthOfYear+1)+"-"+year);

            }};

        DatePickerDialog dpDialog=new DatePickerDialog(this, listener, year, month, day);
        dpDialog.show();

    }
    private void submitForm() {

        if (!validateAmount()) {
            return;
        }

        if (!validateSp()) {
            return;
        }

    }

    private boolean validateAmount() {
        if (etAmount.getText().toString().trim().isEmpty()) {
            tilAmount.setError("Please Enter Amount");
            requestFocus(etAmount);
            return false;
        } else {
            tilAmount.setErrorEnabled(false);
            strAmount=etAmount.getText().toString().replaceAll(" ", "%20").trim();

        }

        return true;
    }

    private boolean validateSp() {
        if (strCat.startsWith("Select")) {
            Toast.makeText(getApplicationContext(), "Please " + getString(R.string.spinner_title), Toast.LENGTH_LONG).show();
            return false;
        } else {
            if(Utils.isConnected(getApplicationContext())){
               // new AsyncAddExpence().execute();

                if(picturePath!=null&&!picturePath.isEmpty()){
                    JsonURL=strBaseUrl+"add_delivery_boy_expense&del_boy_id="+userId+"&category="+strCat+"&amount="+strAmount+"&order_refno="+strRefNo+"&desc="+strDesc+"&token="+token+"&expense_date="+selectedDate;
                    new UploadFileToServer().execute();
                }else{
                    new AsyncAddExpence().execute();
                }

               /* pDialog = ProgressDialog.show(AddExpence.this, "", "file uploading", true);
                new Thread(new Runnable() {
                    public void run() {
                        FileUpload(picturePath);
                    }
                }).start();*/
            }else{
                Toast.makeText(getApplicationContext(),"No Internet Connection !!",Toast.LENGTH_LONG).show();
            }
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_RQ) {

            if (resultCode == RESULT_OK) {
                // Toast.makeText(this, "Saved to: " + data.getDataString(), Toast.LENGTH_LONG).show();
                file = new File(data.getData().getPath());
                Uri imgUri = Uri.fromFile(file);
                picturePath = file.getAbsolutePath();
               // Log.d("CAPTURE_PICTURE_PATH : ", "*** picturePath " + picturePath);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
                    pic = rotateImage(bitmap, 90);
                    ivClick.setImageBitmap(pic);

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (data != null) {
                Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // Sample was denied WRITE_EXTERNAL_STORAGE permission
            Toast.makeText(this, "Image will be saved in a cache directory instead of an external storage directory since permission was denied.", Toast.LENGTH_LONG).show();
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    private class UploadFileToServer extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            donut_progress.setVisibility(View.VISIBLE);
            tvPercentage.setVisibility(View.VISIBLE);

            donut_progress.setProgress(0);
            sourceFile = new File(picturePath);
            totalSize = (int)sourceFile.length();
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
          //  Log.d("PROG", progress[0]);
            donut_progress.setProgress(Integer.parseInt(progress[0])); //Updating progress
        }

        @Override
        protected String doInBackground(String... args) {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = null;
            String fileName = sourceFile.getName();

            try {
                connection = (HttpURLConnection) new URL(JsonURL).openConnection();
                connection.setRequestMethod("POST");
                String boundary = "---------------------------boundary";
                String tail = "\r\n--" + boundary + "--\r\n";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setDoOutput(true);

                String metadataPart = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n"
                        + "" + "\r\n";

                String fileHeader1 = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"filename\"; filename=\""
                        + fileName + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Transfer-Encoding: binary\r\n";

                long fileLength = sourceFile.length() + tail.length();
                String fileHeader2 = "Content-length: " + fileLength + "\r\n";
                String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
                String stringData = metadataPart + fileHeader;

                long requestLength = stringData.length() + fileLength;
                connection.setRequestProperty("Content-length", "" + requestLength);
                connection.setFixedLengthStreamingMode((int) requestLength);
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(stringData);
                out.flush();

                int progress = 0;
                int bytesRead = 0;
                byte buf[] = new byte[1024];
                BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(sourceFile));
                while ((bytesRead = bufInput.read(buf)) != -1) {
                    // write output
                    out.write(buf, 0, bytesRead);
                    out.flush();
                    progress += bytesRead; // Here progress is total uploaded bytes

                    publishProgress(""+(int)((progress*100)/totalSize)); // sending progress percent to publishProgress
                }

                // Write closing boundary and close stream
                out.writeBytes(tail);
                out.flush();
                out.close();

                // Get server response
               /* BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder builder = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    builder.append(line);
                }*/

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null){
                    System.out.println(inputLine);
                    JSONObject jo = new JSONObject(inputLine);
                    code = jo.getString("code");
                    //Log.d("SERVER_RESPONCE code", "" + code);
                    message = jo.getString("message");
                   // Log.d("SERVER_RESPONCE message", "" + message);
                    if(code.equals("200")){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvPercentage.setText("Data Uploaded Successfully!!");
                            }
                        });

                        //PostMeterRecord(imgId);
                    }
                }
                in.close();

            } catch (Exception e) {
                // Exception
            } finally {
                if (connection != null) connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
           // Log.e("Response", "Response from server: " + result);
            tvPercentage.setText("Data Uploaded Successfully!!");
            donut_progress.setVisibility(View.GONE);
            tvPercentage.setVisibility(View.GONE);
            try {
                if (code.equals("200")) {
                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getApplicationContext(),FinanceList.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0,0);
                }
                else if(code.equals("501")){
                   /* try {
                        String createTokenUrl=getApplicationContext().getString(R.string.create_token_url);

                        JsonParser jParser = new JsonParser();
                        JSONObject jo = jParser.getJSONFromUrl(createTokenUrl);
                        String c = jo.getString("code");
                        if(c.equals("200")){
                            token=jo.getString("token");
                            GlobalClass.putToken(token,AddExpence.this);
                            JsonURL=strBaseUrl+"add_delivery_boy_expense&del_boy_id="+userId+"&category="+strCat+"&amount="+strAmount+"&order_refno="+strRefNo+"&desc="+strDesc+"&token="+token+"&expense_date="+selectedDate;
                            new UploadFileToServer().execute();
                        }
                    }catch (JSONException je){
                        je.printStackTrace();
                    }*/

                    try {
                        String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                        cws = new CallWebService(AddExpence.this,createTokenUrl).execute().get();
                       // Log.d(TAG,"Token_URL:  "+cws);
                        JSONObject jo = new JSONObject(cws);
                        String c = jo.getString("code");
                        if (c.equals("200")) {
                            token = jo.getString("token");
                            GlobalClass.putToken(token, AddExpence.this);
                            JsonURL=strBaseUrl+"add_delivery_boy_expense&del_boy_id="+userId+"&category="+strCat+"&amount="+strAmount+"&order_refno="+strRefNo+"&desc="+strDesc+"&token="+token+"&expense_date="+selectedDate;
                            new UploadFileToServer().execute();
                        }
                    }catch (ExecutionException | InterruptedException ei){
                        ei.printStackTrace();
                        JsonURL=strBaseUrl+"add_delivery_boy_expense&del_boy_id="+userId+"&category="+strCat+"&amount="+strAmount+"&order_refno="+strRefNo+"&desc="+strDesc+"&token="+token+"&expense_date="+selectedDate;
                        new UploadFileToServer().execute();
                    }catch (JSONException je){
                        je.printStackTrace();
                        JsonURL=strBaseUrl+"add_delivery_boy_expense&del_boy_id="+userId+"&category="+strCat+"&amount="+strAmount+"&order_refno="+strRefNo+"&desc="+strDesc+"&token="+token+"&expense_date="+selectedDate;
                        new UploadFileToServer().execute();
                    }catch (NullPointerException ne){
                        JsonURL=strBaseUrl+"add_delivery_boy_expense&del_boy_id="+userId+"&category="+strCat+"&amount="+strAmount+"&order_refno="+strRefNo+"&desc="+strDesc+"&token="+token+"&expense_date="+selectedDate;
                        new UploadFileToServer().execute();
                    }

                }
                else if(code.equals("400"))
                {
                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                }
            }catch (NullPointerException ex){
                ex.printStackTrace();
            }
            super.onPostExecute(result);
        }

    }

    public class AsyncAddExpence extends AsyncTask<String, String, String> {

        final String TAG = "AsyncAddExpence";

        String yourJsonStringUrl = strBaseUrl+"add_delivery_boy_expense&del_boy_id="+userId+"&category="+strCat+"&amount="+strAmount+"&order_refno="+strRefNo+"&desc="+strDesc+"&token="+token+"&expense_date="+selectedDate;// set your json string url here
        // contacts JSONArray
        JSONArray dataJsonArr = null,expenceJsonArr=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(AddExpence.this);
            pDialog.setMessage(" data loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {

            try {
                URL url = new URL(yourJsonStringUrl);
               // Log.d(TAG, "URL_is: " + url);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    //return stringBuilder.toString();
                    JSONObject json = new JSONObject(stringBuilder.toString());
                    code=json.getString("code");
                    // Log.d(TAG, "code: " + code);
                    message=json.getString("message");
                    // Log.d(TAG,"message: "+message);
                    if(code.equals("200")) {

                    }
                    return code;
                }
                finally{
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
                return "";
            } catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return "";
            }
        }
        @Override
        protected void onPostExecute(String strFromDoInBg) {
            pDialog.dismiss();
            try {
            if (code.equals("200")) {
                    Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getApplicationContext(),FinanceList.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0,0);
            }
            else if(code.equals("501")){
               /* try {
                    String createTokenUrl=getApplicationContext().getString(R.string.create_token_url);

                    JsonParser jParser = new JsonParser();
                    JSONObject jo = jParser.getJSONFromUrl(createTokenUrl);
                    String c = jo.getString("code");
                    if(c.equals("200")){
                        token=jo.getString("token");
                        GlobalClass.putToken(token,AddExpence.this);
                        new AsyncAddExpence().execute();
                    }
                }catch (JSONException je){
                    je.printStackTrace();
                }*/

                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                    cws = new CallWebService(AddExpence.this,createTokenUrl).execute().get();
                    //Log.d(TAG,"Token_URL:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token, AddExpence.this);
                        new AsyncAddExpence().execute();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                    new AsyncAddExpence().execute();
                }catch (JSONException je){
                    je.printStackTrace();
                    new AsyncAddExpence().execute();
                }catch (NullPointerException ne){
                    new AsyncAddExpence().execute();
                }

            }
            else if(code.equals("400"))
            {
                Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_SHORT).show();
            }
            }catch (NullPointerException ex){
                ex.printStackTrace();
            }

        }
    }

    @Override
    public void onBackPressed() {

        Intent intent=new Intent(getApplicationContext(),FinanceList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        // getMenuInflater().inflate(R.menu.menu_calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent=new Intent(getApplicationContext(),FinanceList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                overridePendingTransition(0,0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
