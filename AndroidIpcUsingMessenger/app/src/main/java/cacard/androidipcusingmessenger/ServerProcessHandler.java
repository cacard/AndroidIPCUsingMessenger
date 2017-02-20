package cacard.androidipcusingmessenger;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cunqingli on 2017/2/16.
 */

public class ServerProcessHandler extends Handler {

    private Map<Integer, Messenger> mClientMessengers = new HashMap<Integer, Messenger>();

    public Messenger getRegisteredMessenger(int processId) {
        if (mClientMessengers != null) {
            return mClientMessengers.get(processId);
        }
        return null;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg == null) {
            return;
        }

        // todo 当然，注册ClientMessenger也可以放到这里，每次操作都注册一次
        switch (msg.what) {
            case IPCConfig.REGISGER_MESSENGER:
                mClientMessengers.put(msg.arg1/* ProcessId */, msg.replyTo);
                log("rcv msg. registerMessenger. processId=" + msg.arg1);
                break;
            case IPCConfig.UNREGISGER_MESSENGER:
                mClientMessengers.remove(msg.arg1);
                log("rcv msg. unregisterMessenger. processId=" + msg.arg1);
                break;
            case IPCConfig.MessageNowProcessActivityOnResume:
                log("rcv msg.MessageNowProcessActivityOnResume");
                break;
            case IPCConfig.MessageNowProcessActivityOnPause:
                log("rcv msg.MessageNowProcessActivityOnPause");
                // 还可以回复消息
                Messenger reply = msg.replyTo;
                if (reply != null) {
                    Message msgReply = Message.obtain();
                    try {
                        reply.send(msgReply);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                super.handleMessage(msg);
        }

        super.handleMessage(msg);
    }

    private void log(String msg) {
        ServerProcess.log(msg);
    }
}
