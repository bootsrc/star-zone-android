package com.appjishu.starzone.push;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.appjishu.starzone.R;
import com.appjishu.starzone.activity.ChatActivity;
import com.appjishu.starzone.activity.ChatWindowActivity;
import com.appjishu.starzone.app.MyApp;
import com.appjishu.starzone.constant.MiSdkConstant;
import com.appjishu.starzone.model.ChatMsg;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.util.ActivityUtil;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import razerdp.github.com.lib.api.AppContext;
import razerdp.github.com.lib.helper.AppSetting;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * 1、PushMessageReceiver 是个抽象类，该类继承了 BroadcastReceiver。<br/>
 * 2、需要将自定义的 DemoMessageReceiver 注册在 AndroidManifest.xml 文件中：
 * <pre>
 * {@code
 *  <receiver
 *      android:name="com.xiaomi.mipushdemo.DemoMessageReceiver"
 *      android:exported="true">
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.RECEIVE_MESSAGE" />
 *      </intent-filter>
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.MESSAGE_ARRIVED" />
 *      </intent-filter>
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.ERROR" />
 *      </intent-filter>
 *  </receiver>
 *  }</pre>
 * 3、DemoMessageReceiver 的 onReceivePassThroughMessage 方法用来接收服务器向客户端发送的透传消息。<br/>
 * 4、DemoMessageReceiver 的 onNotificationMessageClicked 方法用来接收服务器向客户端发送的通知消息，
 * 这个回调方法会在用户手动点击通知后触发。<br/>
 * 5、DemoMessageReceiver 的 onNotificationMessageArrived 方法用来接收服务器向客户端发送的通知消息，
 * 这个回调方法是在通知消息到达客户端时触发。另外应用在前台时不弹出通知的通知消息到达客户端也会触发这个回调函数。<br/>
 * 6、DemoMessageReceiver 的 onCommandResult 方法用来接收客户端向服务器发送命令后的响应结果。<br/>
 * 7、DemoMessageReceiver 的 onReceiveRegisterResult 方法用来接收客户端向服务器发送注册命令后的响应结果。<br/>
 * 8、以上这些方法运行在非 UI 线程中。
 *
 * @author mayixiang
 */
public class DemoMessageReceiver extends PushMessageReceiver {
    private static final String MY_TAG = "PushMsgRec";

    private String mRegId;
    private String mTopic;
    private String mAlias;
    private String mAccount;
    private String mStartTime;
    private String mEndTime;

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        Log.v(MyApp.TAG,
                "onReceivePassThroughMessage is called. " + message.toString());
        String log = context.getString(R.string.recv_passthrough_message, message.getContent());
        ChatWindowActivity.logList.add(0, getSimpleDate() + " " + log);

        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }

        Message msg = Message.obtain();
        msg.obj = log;
        MyApp.getHandler().sendMessage(msg);
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        Log.v(MyApp.TAG,
                "onNotificationMessageClicked is called. " + message.toString());
        String log = context.getString(R.string.click_notification_message, message.getContent());
        ChatWindowActivity.logList.add(0, getSimpleDate() + " " + log);

        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }

        Message msg = Message.obtain();
        if (message.isNotified()) {
            msg.obj = log;
        }

        Map<String, String> extra = message.getExtra();
        if (extra != null && extra.size() > 0) {
            String pushType = extra.get(MiSdkConstant.EXTRA_KEY_PUSH_TYPE);
            String data = extra.get(MiSdkConstant.EXTRA_KEY_DATA);
            if (MiSdkConstant.PUSH_TYPE_CHAT.equals(pushType)) {
                ChatMsg chatMsg = GsonSingleton.getInstance().getGson().fromJson(data, ChatMsg.class);
                if (chatMsg != null && chatMsg.getSenderId() > 0) {
                    String senderIdStr = "senderId=" + chatMsg.getSenderId();
                    Log.i("onMsgClicked", senderIdStr);
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(ChatActivity.INTENT_DATA_KEY_USER_ID, chatMsg.getSenderId());
                    intent.putExtra(ChatActivity.INTENT_DATA_KEY_TARGET_HEAD, chatMsg.getSenderHead());
                    context.startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        Log.v(MyApp.TAG,
                "onNotificationMessageArrived is called. " + message.toString());
        String log = context.getString(R.string.arrive_notification_message, message.getContent());
        ChatWindowActivity.logList.add(0, getSimpleDate() + " " + log);

        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }

        Map<String, String> msgData = message.getExtra();
        if (msgData != null) {
            String pushType = msgData.get(MiSdkConstant.EXTRA_KEY_PUSH_TYPE);
            if (MiSdkConstant.PUSH_TYPE_CHAT.equals(pushType)) {
                String chatMsgStr = msgData.get(MiSdkConstant.EXTRA_KEY_DATA);
                ChatMsg chatMsg = GsonSingleton.getInstance().getGson().fromJson(chatMsgStr,
                        ChatMsg.class);
                long currentUserId = AppSetting.getUserId();

                boolean chatActivityForeground = ActivityUtil.isForeground(context, ChatActivity.class.getName());
                if (chatActivityForeground && chatMsg != null) {
                    long receiverId = chatMsg.getReceiverId();
                    long senderId = chatMsg.getSenderId();
                    if (senderId > 0 && receiverId > 0) {
                        if (senderId == receiverId && senderId == currentUserId) {
                            // 发给自己的，不用在界面上自动更新
                            return;
                        } else {
                            Message msg = Message.obtain();
                            msg.what = ChatActivity.MSG_WHAT_RECEIVE_PUSH;
                            msg.obj = chatMsg;
                            ChatActivity chatActivity = ChatActivity.getInstance();
                            if (chatActivity != null) {
                                chatActivity.sendMessage(msg);
                            }
                        }
                    }
                }

            } else if (MiSdkConstant.PUSH_TYPE_NOTICE.equals(pushType)) {

            }
        }

    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        Log.v(MyApp.TAG,
                "onCommandResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                Log.i(MyApp.TAG, "OnRegisterSuccess_mRegId=" + mRegId);
                MyApp.getInstance().setAlias();
                log = context.getString(R.string.register_success);
                SharedPreferencesMyUtil.storeMiRegid(MyApp.getInstance(), mRegId);
                String storedRegid = SharedPreferencesMyUtil.queryMiRegid(MyApp.getInstance());
                Log.i(MyApp.TAG, "storedRegid=" + storedRegid);
            } else {
                log = context.getString(R.string.register_fail);
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
                log = context.getString(R.string.set_alias_success, mAlias);
                Log.i(MY_TAG, "---onCommandResult_setAlias,alias=" + mAlias);
            } else {
                log = context.getString(R.string.set_alias_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
                log = context.getString(R.string.unset_alias_success, mAlias);
            } else {
                log = context.getString(R.string.unset_alias_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;
                log = context.getString(R.string.set_account_success, mAccount);
            } else {
                log = context.getString(R.string.set_account_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;
                log = context.getString(R.string.unset_account_success, mAccount);
            } else {
                log = context.getString(R.string.unset_account_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
                log = context.getString(R.string.subscribe_topic_success, mTopic);
            } else {
                log = context.getString(R.string.subscribe_topic_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
                log = context.getString(R.string.unsubscribe_topic_success, mTopic);
            } else {
                log = context.getString(R.string.unsubscribe_topic_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mStartTime = cmdArg1;
                mEndTime = cmdArg2;
                log = context.getString(R.string.set_accept_time_success, mStartTime, mEndTime);
            } else {
                log = context.getString(R.string.set_accept_time_fail, message.getReason());
            }
        } else {
            log = message.getReason();
        }
        ChatWindowActivity.logList.add(0, getSimpleDate() + "    " + log);

        Message msg = Message.obtain();
        msg.obj = log;
        MyApp.getHandler().sendMessage(msg);
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        Log.v(MyApp.TAG,
                "onReceiveRegisterResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                log = context.getString(R.string.register_success);
            } else {
                log = context.getString(R.string.register_fail);
            }
        } else {
            log = message.getReason();
        }

        Message msg = Message.obtain();
        msg.obj = log;
        MyApp.getHandler().sendMessage(msg);
    }

    @Override
    public void onRequirePermissions(Context context, String[] permissions) {
        super.onRequirePermissions(context, permissions);
        Log.e(MyApp.TAG,
                "onRequirePermissions is called. need permission" + arrayToString(permissions));

        if (Build.VERSION.SDK_INT >= 23 && context.getApplicationInfo().targetSdkVersion >= 23) {
            Intent intent = new Intent();
            intent.putExtra("permissions", permissions);
            intent.setComponent(new ComponentName(context.getPackageName(), PermissionActivity.class.getCanonicalName()));
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static String getSimpleDate() {
        return new SimpleDateFormat("MM-dd hh:mm:ss").format(new Date());
    }

    public String arrayToString(String[] strings) {
        String result = " ";
        for (String str : strings) {
            result = result + str + " ";
        }
        return result;
    }
}
