package melb.mSafe.ui;

import java.util.List;

import static melb.mSafe.utilities.LogUtils.*;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import melb.mSafe.chat.TextMessage;
import melb.mSafe.R;
import melb.mSafe.events.ChatMessageReceivedEvent;
import melb.mSafe.network.ChatCommunication;
import melb.mSafe.network.IChatCommunication;
import melb.mSafe.utilities.BusProvider;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.squareup.otto.Subscribe;

public class ChatViewFragment extends DialogFragment {
	private static final String TAG = makeLogTag(ChatViewFragment.class);
	private ListView messageListView;
	private ArrayAdapter<TextMessage> messagesAdapter;
    private List<TextMessage> textMessages;

	private TextView messageTextView;

    IChatCommunication messageCommunication;
    private boolean isRunning = false;

	public ChatViewFragment() {
		// Empty constructor required for fragment subclasses
	}

    public ChatViewFragment(List<TextMessage> textMessages){
        this.textMessages = textMessages;
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		messageCommunication = new ChatCommunication();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater
				.inflate(R.layout.chat_layout, container, false);
        Button sendMessageButton = (Button) rootView.findViewById(R.id.sendButton);
		sendMessageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (messageTextView != null) {
                    String textToSend = messageTextView.getText() != null ? messageTextView.getText().toString() : null;
                    clearTextInput();
                    if (textToSend != null) {
                        messageCommunication.sendMessage(textToSend,
                                new Listener<String>() {

                                    @Override
                                    public void onResponse(String response) {
                                        if (rootView != null && isRunning){
                                            Crouton.makeText(getActivity(), "Succes", Style.CONFIRM, (ViewGroup) rootView).show();
                                        }
                                    }
                                }, new ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (rootView != null && isRunning){
                                            Crouton.makeText(getActivity(), "Error", Style.ALERT, (ViewGroup) rootView).show();
                                        }
                                    }
                                }
                        );
                    }
                }
            }
        });

		messageTextView = (TextView) rootView.findViewById(R.id.messageText);

		messageListView = (ListView) rootView
				.findViewById(R.id.messageListView);
		messagesAdapter = new MessageArrayAdapter(getActivity(), 0, textMessages);
		messageListView.setAdapter(messagesAdapter);
		return rootView;
	}

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Updates / Notifications");
        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        dialog.getWindow().setLayout(width, height);
        return dialog;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			return true;
		case R.id.options_clear:
			if (messageListView != null) {
				messagesAdapter.clear();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.chat_menu, menu);
	}

	public void clearTextInput() {
		if (messageTextView != null) {
			messageTextView.setText("");
		}
	}

    public static Fragment newInstance(List<TextMessage> chatMessages) {
        Fragment chatViewFragment = new ChatViewFragment(chatMessages);
        return chatViewFragment;
    }


    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        messagesAdapter = new MessageArrayAdapter(getActivity(), 0, textMessages);
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        isRunning = true;
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
        BusProvider.getInstance().unregister(this);
    }

    private class MessageArrayAdapter extends ArrayAdapter<TextMessage> {
        private LayoutInflater mInflater;

        public MessageArrayAdapter(Context context, int i, List<TextMessage> textMessages) {
            super(context, i, textMessages);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextMessage element = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.message, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.message = (TextView) (convertView != null ? convertView
                        .findViewById(R.id.message) : null);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.message.setText(element.toString());
            return convertView;
        }

        class ViewHolder {
            TextView message;
        }
    }
}
