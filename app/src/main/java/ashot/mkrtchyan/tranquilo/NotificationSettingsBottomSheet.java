package ashot.mkrtchyan.tranquilo;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.Locale;

public class NotificationSettingsBottomSheet extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_notifications, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE);

        Switch switchReminder = view.findViewById(R.id.switchReminder);
        TextView tvTime = view.findViewById(R.id.tvReminderTime);
        View rowTime = view.findViewById(R.id.rowPickTime);


        boolean enabled = prefs.getBoolean("reminder_enabled", false);
        int hour = prefs.getInt("reminder_hour", 9);
        int minute = prefs.getInt("reminder_minute", 0);

        switchReminder.setChecked(enabled);
        tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        rowTime.setVisibility(enabled ? View.VISIBLE : View.GONE);

        switchReminder.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("reminder_enabled", isChecked).apply();
            rowTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            if (isChecked) {
                int h = prefs.getInt("reminder_hour", 9);
                int m = prefs.getInt("reminder_minute", 0);
                ReminderScheduler.schedule(requireContext(), h, m);
            } else {
                ReminderScheduler.cancel(requireContext());
            }
        });

        rowTime.setOnClickListener(v -> {
            int h = prefs.getInt("reminder_hour", 9);
            int m = prefs.getInt("reminder_minute", 0);

            new TimePickerDialog(requireContext(), (picker, selectedHour, selectedMinute) -> {
                prefs.edit()
                        .putInt("reminder_hour", selectedHour)
                        .putInt("reminder_minute", selectedMinute)
                        .apply();

                tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
                ReminderScheduler.schedule(requireContext(), selectedHour, selectedMinute);

            }, h, m, true).show();
        });

        return view;
    }
}