package com.example.fpl1104.mymusic_dome;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String SerName;
    private String timestamp;
    private EditText editText;
    private Button btn;
    public RequestQueue mQueue;
    private ListView listView;
    String[] singername;
    String[] albumname;
    String[] paths;
    private String url;
    private LayoutInflater inflater;
    private int index;
    private MediaPlayer mediaPlayer;
    /** 显示下载进度TextView */
    private TextView mMessageView;
    /** 显示下载进度ProgressBar */
    private ProgressBar mProgressbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SerName = editText.getText() + "";
                url = setMyUil(SerName);
                Log.e("TEST", url);
                getResult(MainActivity.this, url);

            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("询问")
                        .setPositiveButton("试听", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mediaPlayer = new MediaPlayer();
                                try {
                                    mediaPlayer.setDataSource(paths[index]);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    mediaPlayer.prepare();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                mediaPlayer.start();
                            }
                        })
                        .setNegativeButton("下载", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               new Thread(){
                                   @Override
                                   public void run() {
                                       super.run();
                                downLoadFile(paths[index], "MyDowload", editText.getText() + timestamp);
                                   }
                               }.start();

                            }
                        })
                        .show();
                index = position;
            }
        });


    }


    private void init() {
        editText = (EditText) findViewById(R.id.editText);
        listView = (ListView) findViewById(R.id.listView);
        btn = (Button) findViewById(R.id.button);
        mMessageView = (TextView) findViewById(R.id.download_message);
        mProgressbar = (ProgressBar) findViewById(R.id.download_progress);
//        Log.e("TIME",timestamp);
    }



    private String setMyUil(String sName) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        timestamp = df.format(new Date());
        String url1 = "https://route.showapi.com/213-1?keyword=" + sName +
                "&page=1&showapi_appid=20421&" +
                "showapi_timestamp=" + timestamp + "&" +
                "showapi_sign=b57a18e3f6194bfe9c9164f5f80a3b6f";
        return url1;
    }

    private void getResult(Context context, String url) {
        mQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
//                        Log.e("TEEEEEE",jsonObject.toString()+"");
//                        get(jsonObject);
                        String data = jsonObject.toString();
                        JSONObject Music = null;
                        JSONArray result = null;
                        JSONObject M1 = null;
                        JSONObject M2 = null;
                        try {
                            M1 = new JSONObject(data);
                            M2 = M1.getJSONObject("showapi_res_body");
                            Music = M2.getJSONObject("pagebean");
                            result = Music.getJSONArray("contentlist");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Log.e("LENGTH", result.length() + "");
                        singername = new String[result.length()];
                        albumname = new String[result.length()];
                        paths = new String[result.length()];
                        for (int index = 0; index < result.length(); index++) {
                            try {
                                JSONObject air = result.getJSONObject(index);
                                singername[index] = air.getString("singername");
                                albumname[index] = air.getString("albumname");
                                paths[index] = air.getString("downUrl");
                            } catch (JSONException e) {
                                singername[index] = "无名歌" + index;
                                albumname[index] = "无名";
                                e.printStackTrace();

                            }
                        }
                        for (String s : singername) {
                            Log.e("TTT", s.toString());
                        }
                        MyAdapter myAdapter = new MyAdapter(MainActivity.this, singername, albumname, paths);
                        listView.setAdapter(myAdapter);
                        myAdapter.notifyDataSetChanged();


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        mQueue.add(jsonObjectRequest);
    }

    class MyAdapter extends BaseAdapter {
        LayoutInflater layoutInflater;
        Context context;
        String[] singername;
        String[] albumname;
        String[] path;


        public MyAdapter(Context context, String[] singername, String[] albumname, String[] path) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.singername = singername;
            this.albumname = albumname;
            this.path = path;


        }

        @Override
        public int getCount() {
//            Log.e("TttttttTT",singername.length+"");
            return singername.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder = null;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                view = inflater.inflate(R.layout.item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            TextView t1 = holder.getT1();
            TextView t2 = holder.getT2();
            TextView path = holder.getPath();

            t1.setText(singername[position]);
//            Log.e("POSSSSSSS",position+"");
            t2.setText(albumname[position]);
            path.setText(paths[position]);

            return view;
        }
    }

    class ViewHolder {
        private View view;
        private TextView t1, t2, path;

        public ViewHolder(View view) {
            this.view = view;
        }

        public TextView getT1() {
            if (t1 == null) {
                t1 = (TextView) view.findViewById(R.id.textView);
            }
            return t1;
        }

        public TextView getT2() {
            if (t2 == null) {
                t2 = (TextView) view.findViewById(R.id.textView2);
            }
            return t2;
        }

        public TextView getPath() {
            if (path == null) {
                path = (TextView) view.findViewById(R.id.path);
            }
            return path;
        }

    }

    /*
* 该函数返回整形-1：代表下载文件出错。
* 0：代表下载文件成功
* 1：代表下载文件经存在
*/
    public int downLoadFile(String urlStr, String path, String fileName) {
        InputStream inputStream = null;
        GoToSDCard gotoSDCard = new GoToSDCard();
        if (gotoSDCard.isFileExist(path + fileName)) {
            return 1;
        } else {

            try {
                URL url = new URL(urlStr);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                inputStream = urlConn.getInputStream();
                File resultFile = gotoSDCard.write2SDFromInput(path, fileName, inputStream);//将数据流保存到SD卡当中
                if (resultFile == null) {
                    return -1;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            finally {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
        return 0;
    }

    public class GoToSDCard {
        private String SDPATH = null;

        public String getSDPATH() {
            return SDPATH;
        }

        public GoToSDCard() {
//得到当前外部存储设备的目录
// SDCARD
            SDPATH = Environment.getExternalStorageDirectory() + "";
            System.out.println("SDPATH=" + SDPATH);
        }

        /*
        *在SD卡上创建文件
        */
        public File CreatSDFile(String fileNmae) {
            File file = new File(SDPATH + fileNmae);
            try {
                file.createNewFile();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
            return file;
        }

        /*
        * 在SD卡上创建目录
        */
        public File creatSDDir(String dirName) {
            File dir = new File(SDPATH + dirName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }

        /*
        *判断SD卡上的文件夹是否存在
        */
        public boolean isFileExist(String fileName) {
            File file = new File(SDPATH + fileName);
            return file.exists();
        }

        /*
        *将一个InputSteam里面的数据写入到SD卡中
        */
        public File write2SDFromInput(String path, String fileName, InputStream input) {
            System.out.println("path=" + path + ";fileName=" + fileName + ";");
            File file = null;
            File folder = null;
            OutputStream output = null;
            try {
                folder = creatSDDir(path);
                System.out.println("folder=" + folder);
                file = CreatSDFile(path + fileName);
                System.out.println("file=" + file);
                output = new FileOutputStream(file);
                byte buffer[] = new byte[4 * 1024];
                while ((input.read()) != -1) {
                    output.write(buffer);
                }
                output.flush();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return file;
        }
    }
}


