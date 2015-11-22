package com.pmdm.audioplayersd;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;

/*
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void ejecutar(View v) {
        boolean b = false;
        String mp3 = "gato.mp3";
        if (!b) b = runFromExternalStorage(mp3);
        if (!b) b = runFromSecondaryStorage(mp3);
        if (!b) b = runFromMntVfat(mp3);
    }

    public boolean runFromExternalStorage(String mp3){
        boolean b = false;
        String primary_sd_path = System.getenv("EXTERNAL_STORAGE") + "/" + mp3;
        File file = new File(primary_sd_path);
        if (file.exists()) {
            try {
                Uri datos = Uri.parse(primary_sd_path);
                MediaPlayer mp = MediaPlayer.create(this, datos);
                mp.start();
                b = true;
                Toast.makeText(getApplicationContext(),"EXTERNAL_STORAGE=\n" + primary_sd_path,
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {}
        }
        return b;
    }

    public boolean runFromSecondaryStorage(String mp3){
        boolean b = false;
        String secondary_sd_path = System.getenv("SECONDARY_STORAGE") + "/" + mp3;
        File file = new File(secondary_sd_path);
        if (file.exists()) {
            try {
                Uri datos = Uri.parse(secondary_sd_path);
                MediaPlayer mp = MediaPlayer.create(this, datos);
                mp.start();
                b = true;
                Toast.makeText(getApplicationContext(),"SECONDARY_STORAGE=\n" + secondary_sd_path,
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {}
        }
        return b;
    }

    public boolean runFromMntVfat(String mp3){
        boolean b = false;
        StringBuffer sb = new StringBuffer("");
        try {
            Process process = new ProcessBuilder().command("mount").redirectErrorStream(true).start();
            process.waitFor();
            InputStream is = process.getInputStream();
            byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                sb.append(new String(buffer));
            }
            is.close();
        } catch (Exception e) {}
        String[] lines = sb.toString().split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (-1 != lines[i].indexOf("/mnt/")&& -1 != lines[i].indexOf("vfat")) {
                String[] blocks = lines[i].split("\\s");
                for (int j = 0; j < blocks.length; j++) {
                    if (-1 != blocks[j].indexOf("/mnt/")) {
                        // TEST
                        String sd_path = blocks[j] + "/" + mp3;
                        File file = new File(sd_path);
                        if (file.exists()) {
                            try {
                                // Android MediaPlayer.prepare throws status=0×1 error(1, -2147483648)
                                // http://www.weston-fl.com/blog/?p=2988
                                // Set to Readable and MODE_WORLD_READABLE
                                file.setReadable(true, false);
                                Uri datos = Uri.parse(sd_path);
                                MediaPlayer mp = MediaPlayer.create(this, datos);
                                mp.start();
                                b = true;
                                Toast.makeText(getApplicationContext(), "STORAGE=\n" + sd_path,
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {}
                        }
                    }
                }
            }
        }
        return b;
    }

}