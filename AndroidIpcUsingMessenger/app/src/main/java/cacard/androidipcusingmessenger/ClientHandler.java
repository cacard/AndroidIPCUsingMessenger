package cacard.androidipcusingmessenger;

import android.os.Handler;
import android.os.Message;

/**
 * Created by cunqingli on 2017/2/16.
 */

public class ClientHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        if (msg == null) {
            return;
        }

        switch (msg.what) {
            case 0:
                break;
            default:
                super.handleMessage(msg);
        }

    }
}
