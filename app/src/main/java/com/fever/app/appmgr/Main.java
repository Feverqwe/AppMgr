package com.fever.app.appmgr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main extends Activity {
    String fileName = "disabledList.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button saveBtn = (Button) findViewById(R.id.saveBtn);
        Button restoreBtn = (Button) findViewById(R.id.restoreBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONArray list = getDisabledApps();
                saveInFile(list.toString());
            }
        });
        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String list = readFile();
                getJsonList(list);
            }
        });
    }

    public void saveInFile(String json) {
        Context context = getApplicationContext();
        String dir = context.getExternalFilesDir(null).getAbsolutePath();
        File file = new File(context.getExternalFilesDir(null), fileName);
        try {
            FileWriter out = new FileWriter(file);
            out.write(json);
            out.close();
        } catch (IOException e) {
            Log.e("Can't write in file", dir + "/" + fileName);
            showToast("Can't write in file\n" + dir + "/" + fileName);
            return;
        }
        showToast("Saved!\n" + dir + "/" + fileName);
    }

    public String readFile() {
        Context context = getApplicationContext();
        String dir = context.getExternalFilesDir(null).getAbsolutePath();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(new File(context.getExternalFilesDir(null), fileName)));
            while ((line = in.readLine()) != null) stringBuilder.append(line);

        } catch (FileNotFoundException e) {
            Log.e("File not found!", dir + "/" + fileName);
            showToast("File not found!\n" + dir + "/" + fileName);
        } catch (IOException e) {
            Log.e("File read error!", dir + "/" + fileName);
            showToast("File read error!\n" + dir + "/" + fileName);
        }

        return stringBuilder.toString();
    }

    public void getJsonList(String json) {
        try {
            List<String> currentList = getAppList();
            JSONArray list = new JSONArray(json);
            for (int i = 0; i < list.length(); i++) {
                String packageName = list.getString(i);
                if (currentList.contains(packageName)) {
                    openSettings(packageName);
                    showToast("Open...\n" + packageName);
                }
            }
        } catch (JSONException e) {
            Log.e("Bad json string!", json);
            showToast("Bad json string!");
            return;
        }
        showToast("Dune!");
    }

    public JSONArray getDisabledApps() {
        JSONArray list = new JSONArray();
        List<PackageInfo> PackList = getPackageManager().getInstalledPackages(0);
        for (int i=0; i < PackList.size(); i++) {
            PackageInfo PackInfo = PackList.get(i);
            Boolean enabled = PackInfo.applicationInfo.enabled;
            if (enabled) {
                continue;
            }
            list.put(PackInfo.packageName);
        }
        return list;
    }

    public List<String> getAppList() {
        List list = new ArrayList();
        List<PackageInfo> PackList = getPackageManager().getInstalledPackages(0);
        for (int i=0; i < PackList.size(); i++) {
            PackageInfo PackInfo = PackList.get(i);
            Boolean enabled = PackInfo.applicationInfo.enabled;
            if (!enabled) {
                continue;
            }
            try {
                list.add(PackInfo.packageName);
            } catch (NullPointerException e) {
                continue;
            }

        }
        return list;
    }

    private void showToast(String text) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void openSettings(String packageName) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return false;
    }
}
