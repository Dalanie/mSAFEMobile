package melb.mSafe.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import melb.mSafe.R;

import java.util.ArrayList;

import melb.mSafe.common.AccessibilityMenu;

/**
 * Created by Daniel on 13.01.14.
 */
public class AccessibilityAdapter extends ArrayAdapter<AccessibilityMenu>{

    Context context;
    LayoutInflater inflater;

    public AccessibilityAdapter(Context context,
                                ArrayList<AccessibilityMenu> data) {
        super(context, R.layout.accessible_item_drop_down, data);
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.accessible_item_drop_down, null);
        TextView title = (TextView) (convertView != null ? convertView.findViewById(R.id.title) : null);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);

        final AccessibilityMenu item = getItem(position);
        if (item != null) {
            title.setText(item.accessibility.toString());
            title.setTextColor(context.getResources().getColor(R.color.textcolor));
            icon.setImageResource(item.iconId);
        }
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.accessible_item, null);
        ImageView icon = (ImageView) (convertView != null ? convertView.findViewById(R.id.icon) : null);

        final AccessibilityMenu item = getItem(position);
        if (item != null) {
            icon.setImageResource(item.iconId);
        }
        return convertView;
    }
}
