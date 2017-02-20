package cacard.androidipcusingmessenger;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * MainProcess中运行的服务
 * 双向交互。支持多个Client，由于使用了Messenger，没有线程不安全问题。
 * <p>
 * 优点：
 * - 可双向交互；
 * - 多个Client与Server进程交互，不存在线程安全问题；
 * <p>
 * 缺点：
 * - 每个操作对应哦一个标识Id，进程之间传递的仅是Id，类似于进程间通过Id号来表示方法调用；
 * <p>
 * Created by cunqingli on 2017/2/15.
 */

public class ServerProcess extends Service {

    private static final String TAG = "MainProcessServer";

    private ServerProcessHandler mHandler = new ServerProcessHandler();
    private Messenger mMessengerServer = new Messenger(mHandler);


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessengerServer.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Server向Client发送消息。
     * 前提是Client已经注册了自己的Messenger。
     *
     * @param processId
     * @param data
     */
    private void sendMessageToClient(int processId, int msgWhat, Parcelable data) {
        Messenger client = mHandler.getRegisteredMessenger(processId);
        if (client == null) {
            log("Client Messenger No Registered.");
        } else {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putParcelable("msg", data);
            msg.setData(bundle);
            msg.what = msgWhat;
            msg.replyTo = mMessengerServer;
            try {
                client.send(msg);
            } catch (RemoteException e) {
                log("sendMessageToClient() error. e:" + e.getMessage());
            }
        }
    }

    public static void log(String msg) {
        Log.i(TAG, msg);
    }
}
