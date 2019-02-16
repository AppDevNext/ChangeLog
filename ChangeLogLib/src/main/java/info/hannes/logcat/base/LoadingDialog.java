package info.hannes.logcat.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import info.hannes.R;

public class LoadingDialog extends DialogFragment {

    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String CANCELABLE = "CANCELABLE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setCancelable(false);
    }

    @SuppressWarnings("unused")
    static LoadingDialog newInstance(int messageId, boolean cancelable) {
        LoadingDialog fragment = new LoadingDialog();
        Bundle args = new Bundle();
        args.putInt(MESSAGE_ID, messageId);
        args.putBoolean(CANCELABLE, cancelable);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("SameParameterValue")
    public static LoadingDialog newInstance(boolean cancelable) {
        LoadingDialog fragment = new LoadingDialog();
        Bundle args = new Bundle();
        args.putBoolean(CANCELABLE, cancelable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.loading_dialog, container, false);

        if (getArguments() != null) {
            TextView tv = v.findViewById(R.id.loadingText);
            int messageId = getArguments().getInt(MESSAGE_ID, R.string.placeholder);
            tv.setText(messageId);
        }

//        ProgressBar progressBar  = v.findViewById(R.id.loadingProgress);
//        progressBar.getIndeterminateDrawable().setColorFilter(
//            ContextCompat.getColor(getActivity(), R.color.color_accent),
//            PorterDuff.Mode.SRC_IN
//        );

        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        /// set cancellation behavior
        boolean cancelable = false;
        if (getArguments() != null) {
            cancelable = getArguments().getBoolean(CANCELABLE, false);
        }
        dialog.setCancelable(cancelable);
        if (!cancelable) {
            // disable the back button
            DialogInterface.OnKeyListener keyListener = (dialog1, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK;
            dialog.setOnKeyListener(keyListener);
        }
        return dialog;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
