package com.ece416.aruproy.messengerclient;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ilikecalculus on 2017-03-24.
 */

public class ConnectTask extends AsyncTask<String, String, TcpClient> {

    private static ConnectTask instance = null;
    private static TcpClient mTcpClient;
    private static String ipAddress = null;
    private static String portNumber = null;
    private static String username = "";
    private static String currGroup = "";
    private static Context mContext = null;
    private static boolean isServerOnline = false;
    private static List<String> groupList = new ArrayList<>();
    private static List<String> membersList = new ArrayList<>();
    private static Map<String, List<List<String>>> messageQueue = new HashMap<>();
    private static Queue<Map<String, String>> messageBuffer = new ConcurrentLinkedQueue<>();

    private static ListObserver listObserver;
    private static MessageObserver messageObserver;

    private ConnectTask() {}

    public static ConnectTask getInstance() {
        if(instance == null) {
            instance = new ConnectTask();
        }
        return instance;
    }

    public static void setUsername(String u) {
        username = u;
    }

    public static void setCurrGroup(String gn) {
        currGroup = gn;
    }

    public static String getUsername() {
        return username;
    }

    public static void setListObserver(ListObserver o) {
        listObserver = o;
    }

    public static void setMessageObserver(MessageObserver o) {
        messageObserver = o;
    }

    public static void setIpAndPort(String ip, String port){
        ipAddress = ip;
        portNumber = port;
    }

    public static boolean isServerOnline() {
        return isServerOnline;
    }


    public static void tcpSendMessage(String message, String groupName){
        Map<String, String> data = new HashMap<>();
        data.put(Constants.USERNAME_KEY, ConnectTask.getUsername());
        data.put(Constants.MESSAGE_TYPE_KEY, MessageType.NEW_MESSAGE.getValue());
        if (!message.equals("")) data.put(Constants.MESSAGE_KEY, message);
        if (!groupName.equals("")) data.put(Constants.GROUP_NAME_KEY, groupName);
        bufferSendMessage(data);
    }

    public static void tcpGroupAction(MessageType mt, String groupName) {
        Map<String, String> data = new HashMap<>();
        data.put(Constants.USERNAME_KEY, ConnectTask.getUsername());
        data.put(Constants.MESSAGE_TYPE_KEY, mt.getValue());
        data.put(Constants.GROUP_NAME_KEY, groupName);
        bufferSendMessage(data);
    }

    public static void tcpLogin() {
        Map<String, String> data = new HashMap<>();
        data.put(Constants.USERNAME_KEY, ConnectTask.getUsername());
        data.put(Constants.MESSAGE_TYPE_KEY, MessageType.LIST_GROUP.getValue());
        bufferSendMessage(data);
    }

    public static void bufferSendMessage(Map<String, String> data) {
        JSONObject json = new JSONObject(data);
        Log.e("Send JSON Dictionary", json.toString());
        messageBuffer.add(data);
        ConnectTask.getInstance().getTcpClient().sendMessage(json.toString());
    }


    public static List<List<String>> getMessagesForGroup(String key) {
        if (messageQueue.containsKey(key)) {
            List<List<String>> list = messageQueue.get(key);
            messageQueue.remove(key);
            return list;
        }
        return null;
    }

    public static boolean hasNewMessagesForGroup(String group) {
        return messageQueue.containsKey(group);
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

    private void toastNotif(List<String> groups) {
        if (groups.size() > 0)
            Toast.makeText(mContext, "New message from groups: " + groups.toString(), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        //response received from server
        Log.e("CONNECT_TASK", "response " + values[0]);
        isServerOnline = true;
        try {
            Map<String, Object> map = JSONUtil.jsonToMap(values[0]);
            if (!map.get(Constants.MESSAGE_TYPE_KEY).equals(messageBuffer.peek().get(Constants.MESSAGE_TYPE_KEY)))
                return;
            messageBuffer.poll();

            // update status
            messageObserver.updateStatus();

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
                    // populate lists
                    listObserver.updateList();
                    break;

                case NEW_MESSAGE:
                    Map<String, Object> messageMap = (Map<String, Object>) map.get(Constants.MESSAGE_KEY);
                    List<String> newMesageGroup = new ArrayList<>();
                    for (String group : messageMap.keySet()) {
                        List<Object> currMsgList = (List<Object>) messageMap.get(group);
                        if (currMsgList == null || currMsgList.size() == 0) continue;
                        if (!group.equals(currGroup)) newMesageGroup.add(group);
                        for(int i = 0; i < currMsgList.size(); i++) {
                            List<String> currMessage = (List<String>) currMsgList.get(i);
                            if (!messageQueue.containsKey(group)){
                                messageQueue.put(group, new ArrayList<List<String>>());
                            }
                            messageQueue.get(group).add(currMessage);
                        }
                    }

                    membersList = (List<String>) map.get(Constants.MEMBERS_KEY);
                    messageObserver.updateMessage();
                    toastNotif(newMesageGroup);

                    Log.d("CURRENT MEMBER LIST", membersList.toString());
                    Log.d("CURRENT QUEUE", messageQueue.toString());

                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            Log.e("JSON_EXCEPTION", "Error converting:\n" + values[0] + "\nTo a Java Map with Exception: \n" + getStackTrace(e));
        }
    }

    private static void resolveBuffer() {
        int max = 0;
        while (max < 10) {
            if (messageBuffer.peek() == null) return;
            JSONObject json = new JSONObject(messageBuffer.peek());
            Log.e("Resending...", json.toString());
            ConnectTask.getInstance().getTcpClient().sendMessage(json.toString());
            try {
                Thread.sleep(300);
            } catch (Exception e) {
                Log.e("CONNECT_TASK_EXCEPTION", e.toString());
            }
            max += 1;
        }
    }

    public static void restartTcpClient() {
        if (instance != null) {
            instance.getTcpClient().stopClient();
        }
        instance = new ConnectTask();
        instance.execute("");
    }

    // Lawl hacky af, but whatever
    private static Timer timer;
    private static TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (messageBuffer.size() < 1) {
                messageObserver.sendMessage();
            }
            else {
                isServerOnline = false;
                messageObserver.updateStatus();
                restartTcpClient();
                resolveBuffer();
            }
        }
    };

    public static void startTcpPing() {
        if(timer != null) {
            return;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 5000);
    }

    public static void stopTcpPing() {
        timer.cancel();
        timer = null;
    }

    private static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
