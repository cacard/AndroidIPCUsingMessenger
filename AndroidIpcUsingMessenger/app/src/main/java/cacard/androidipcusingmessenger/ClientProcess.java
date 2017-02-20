package cacard.androidipcusingmessenger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


/**
 * 运行在NowProcess中。向MainProcess发送交互操作，并能接收到处理结果。
 * Created by cunqingli on 2017/2/15.
 */

public class ClientProcess {

    private static String TAG = "NowProcessClient";

    boolean isBind = false;
    boolean autoConnect = false;
    Application mApp;
    Messenger mMessengerServer;
    Messenger mMessengerClient = new Messenger(new ClientHandler());

    private static class Holder {
        public static ClientProcess sInstance = new ClientProcess();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dump("onServiceConnected() componentName:" + name);
            isBind = true;
            mMessengerServer = new Messenger(service);
            afterBind();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dump("onServiceDisconnected() componentName:" + name);
            isBind = false;

            // 断开连接后，尝试重新bind
            if (autoConnect) {
                dump("will reconnect...");
                startBind(mApp, true);
            }
        }
    };

    private ClientProcess() {

    }

    public static ClientProcess getInstance() {
        return Holder.sInstance;
    }

    /**
     * 开始向Server发动绑定
     *
     * @param app
     * @param autoReConnect
     */
    public void startBind(Application app, boolean autoReConnect) {
        dump("startBind()");
        mApp = app;
        autoConnect = autoReConnect;
        Intent i = new Intent(mApp, ServerProcess.class);
        mApp.bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * 绑定完毕。
     * 1，可以立刻向Server发送一个“注册Messeneger”的消息；
     */
    private void afterBind() {
        dump("afterBind()");

        // 向Server注册Messenger
        registerToServer();

    }

    /**
     * 向Server注册自己的Messenger
     */
    public void registerToServer() {
        dump("->registerToServer()");

        if (!isBind || mMessengerServer == null) {
            dump("registerToServer(), !bind or messengerServer is null.");
            return;
        }

        Message msg = Message.obtain();
        msg.what = Config.REGISGER_MESSENGER;
        msg.arg1 = Config.NOW_PROCESS;
        msg.replyTo = mMessengerClient;
        try {
            mMessengerServer.send(msg);
        } catch (RemoteException e) {
            dump("registerToServer(), error when send msg:" + e.getMessage());
        }
    }

    /**
     * 向Server进程发送消息。前提是已经绑定完成。
     *
     * @param data
     */
    public void sendMessageToServer(int msgWhat, String activityName) {
        if (!isBind || mMessengerServer == null) {
            dump("sendMessageToServer(), !bind or messengerServer is null.");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("activityName", activityName);
        Message message = Message.obtain();
        message.what = msgWhat;
        message.setData(bundle);
        message.replyTo = mMessengerClient;
        try {
            mMessengerServer.send(message);
        } catch (RemoteException e) {
            dump("registerToServer(), error when send msg:" + e.getMessage());
        }
    }

    public void unbind() {
        autoConnect = false;
        if (mApp != null && mServiceConnection != null) {
            mApp.unbindService(mServiceConnection);
        }
    }

    public static void dump(String msg) {
        Log.i(TAG, msg);
    }

}
