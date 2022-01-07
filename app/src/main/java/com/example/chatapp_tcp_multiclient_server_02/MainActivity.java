package com.example.chatapp_tcp_multiclient_server_02;


import android.app.Activity;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private boolean side = false;
    ///////////////
    int flag=0;
    public Socket socket;
    Client client;
    public static final String  HOST_lAPTOPHP="192.168.1.5";
    Random random= new Random();
    String name;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        linkview();


        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        listView.setAdapter(chatArrayAdapter);
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Log.e("adas","asdsa");
                }
                return false;
            }
        });
//        buttonSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                sendChatMessage();
//            }
//        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
        SendMesseage();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.e("Thông báo","Đã đóng kết nối với server");
        client.closeEverything();


    }

    //////////////////////////////////////////////////////////
    Handler handler= new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String updateMsg=(String) msg.obj;
            chatArrayAdapter.add(new ChatMessage(true,updateMsg));
            return false;

        }
    });
    private boolean sendChatMessage() {
        chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString()));
        chatText.setText("");
        side = !side;
        return true;
    }
    private void linkview() {
        buttonSend = (Button) findViewById(R.id.send);
        chatText = (EditText) findViewById(R.id.msg);
        listView = (ListView) findViewById(R.id.msgview);
    }

    private void SendMesseage() {
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(flag==0){
                            try {
                                Log.d("asdasd","fasdfsdf");
                                socket=new Socket(HOST_lAPTOPHP,1511);
                                client = new Client(socket,"aaaaaaaaa");
                                client.sendMessengerAndName(chatText.getText().toString());
                                client.execute();
                            } catch (IOException e) {
                                Log.e("Lỗi","Không thể kết nối đến Server");
                            }
                            flag=1;
                        } else {
                            client.sendMessenger(chatText.getText().toString());
                        }
                    }
                }).start();
            }
        });
    }

    public class Client extends AsyncTask<Void,String,Void> {
        private Socket socket;
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private String clientUserName;
        public Client(Socket socket, String clientUserName) {
            try {
                this.socket = socket;
                this.dataInputStream= new DataInputStream(socket.getInputStream());
                this.dataOutputStream= new DataOutputStream(socket.getOutputStream());
                this.clientUserName = clientUserName;
            } catch (IOException e) {
                closeEverything();
            }
        }
        public void sendMessenger(String msg){
            try {
//            while (socket.isConnected()) {
                String messengerToSend = msg;
                dataOutputStream.writeUTF(messengerToSend);
                //  bufferedWriter.newLine();
                dataOutputStream.flush();
//            }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message= handler.obtainMessage();
                        message.obj=msg;
                        handler.sendMessage(message);
                    }
                }).start();
//            }
            } catch (Exception e) {
                closeEverything();
            }
        }
        public void sendMessengerAndName(String msg){
            try {
                dataOutputStream.writeUTF(clientUserName);
                dataOutputStream.flush();
//            while (socket.isConnected()) {
                String messengerToSend = msg;
                dataOutputStream.writeUTF(clientUserName + messengerToSend);
                //  bufferedWriter.newLine();
                dataOutputStream.flush();
//            }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message= handler.obtainMessage();
                        message.obj=msg;
                        handler.sendMessage(message);

                    }
                }).start();
            } catch (Exception e) {
                closeEverything();
            }
        }

        private void closeEverything() {
            try {
                if(dataInputStream!=null){
                    this.dataInputStream.close();
                    Log.e("dataInputStream","dataInputStream");

                }
                if(dataOutputStream!=null){
                    this.dataOutputStream.close();
                    Log.e("dataOutputStream","dataOutputStream");

                }
                if(socket!=null){
                    socket.close();
                    Log.e("socket","socket");

                }
                Log.e("Thông báo","Đã đóng kết nối với server");
            } catch (Exception e) {
            }
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String a=values[0];
            Log.d(a,a);
            chatArrayAdapter.add(new ChatMessage(false,a));
        }
        @Override
        protected Void doInBackground(Void... voids) {
            String msgFromGroupChat;
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat=dataInputStream.readUTF();
                    publishProgress(msgFromGroupChat);
                } catch (Exception e) {
                    closeEverything();
                }
            }
            return null;
        }
    }
}