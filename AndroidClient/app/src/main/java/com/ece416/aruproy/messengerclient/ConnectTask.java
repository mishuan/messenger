package com.ece416.aruproy.messengerclient;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ilikecalculus on 2017-03-24.
 */

public class ConnectTask extends AsyncTask<String, String, TcpClient> {

    private static ConnectTask instance = null;
    private static TcpClient mTcpClient;
    private static String ipAddress = null;
    private static String portNumber = null;
    private static Context mContext = null;
    private static List<String> groupList = new ArrayList<>();
    private static List<String> membersList = new ArrayList<>();
    private static Map<String, List<List<String>>> messageQueue = new HashMap<>();

    private static ListObserver listObserver;
    private static MessageObserver messageObserver;

    private ConnectTask() {}

    public static ConnectTask getInstance() {
        if(instance == null) {
            instance = new ConnectTask();
        }
        return instance;
    }

    public static void setListObserver(ListObserver o) {
        listObserver = o;
    }

    public static void setMessageObserver(MessageObserver o) {
        messageObserver = o;
    }

    public static void execute() {
        instance.execute("");
    }


    public static void setIpAndPort(String ip, String port){
        ipAddress = ip;
        portNumber = port;
        execute();
    }

    public static List<List<String>> getMessagesForGroup(String key) {
        if (messageQueue.containsKey(key)) {
            List<List<String>> list = messageQueue.get(key);
            messageQueue.remove(key);
            return list;
        }
        return null;
    }

    public static List<String> getGroupList() {
        return groupList;
    }

    public static List<String> getMembersList() {
        return membersList;
    }

    public static void setContext(Context context){
        mContext = context;
    }

    public String getIp(){
        return ipAddress;
    }

    public TcpClient getTcpClient() {
        return mTcpClient;
    }

    @Override
    protected TcpClient doInBackground(String... message) {

        //we create a TCPClient object
        mTcpClient = new TcpClient(ipAddress, portNumber, new TcpClient.OnMessageReceived() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceived(String message) {
                //this method calls the onProgressUpdate
                publishProgress(message);
            }
        });
        mTcpClient.run();
        return null;
    }

    private void toastGroupOperation(Map<String, Object> map, MessageType mt) {
        if (mt == MessageType.JOIN_GROUP) {
            Toast.makeText(mContext, "Joined group " + map.get(Constants.GROUP_NAME_KEY) + "!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Left group " + map.get(Constants.GROUP_NAME_KEY) + "!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        //response received from server
        Log.e("CONNECT_TASK", "response " + values[0]);
        try {
            Map<String, Object> map = JSONUtil.jsonToMap(values[0]);
            switch(MessageType.get(map.get(Constants.MESSAGE_TYPE_KEY).toString())) {
                case JOIN_GROUP:
                    toastGroupOperation(map, MessageType.JOIN_GROUP);
                    break;

                case LEAVE_GROUP:
                    toastGroupOperation(map, MessageType.LEAVE_GROUP);
                    break;

                case LIST_GROUP:
                    groupList = map.get(Constants.GROUPS_KEY) != null
                            ? (List<String>) map.get(Constants.GROUPS_KEY)
                            : groupList;
                    listObserver.updateList();
                    break;

                case NEW_MESSAGE:
                    Map<String, Object> messageMap = (Map<String, Object>) map.get(Constants.MESSAGE_KEY);
                    for (String key : messageMap.keySet()) {
                        List<Object> currMsgList = (List<Object>) messageMap.get(key);
                        if (currMsgList == null) continue;
                        Log.d("PROCESSING...", currMsgList.toString());
                        for(int i = 0; i < currMsgList.size(); i++) {
                            List<String> currMessage = (List<String>) currMsgList.get(i);
                            if (!messageQueue.containsKey(key)){
                                messageQueue.put(key, new ArrayList<List<String>>());
                            }
                            messageQueue.get(key).add(currMessage);
                        }
                    }

                    membersList = (List<String>) map.get(Constants.MEMBERS_KEY);

                    Log.d("CURRENT MEMBER LIST", membersList.toString());
                    Log.d("CURRENT QUEUE", messageQueue.toString());

                    messageObserver.updateMessage();
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            Log.e("JSON_EXCEPTION", "Error converting:\n" + values[0] + "\nTo a Java Map with Exception: \n" + getStackTrace(e));
        }
    }

    private static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
