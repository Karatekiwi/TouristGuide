package at.ac.tuwien.touristguide.tools;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import at.ac.tuwien.touristguide.R;
import at.ac.tuwien.touristguide.entities.RowItem;


/**
 * @author Manu Weilharter
 * A custom list view adapter for the nearby fragment
 */
public class CustomListViewAdapterNearby extends ArrayAdapter<RowItem> {

    Context context;

    public CustomListViewAdapterNearby(Context context, int resourceId,
                                       List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (position == 0) {
            convertView = mInflater.inflate(R.layout.listview_header, null);
            ViewHolderHeader holder2 = new ViewHolderHeader();
            holder2.txtTitle = convertView.findViewById(R.id.tv_listheader);
            holder2.txtTitle2 = convertView.findViewById(R.id.tv_listheader2);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            holder2.txtTitle.setText(context.getResources().getString(R.string.lva1));
            holder2.txtTitle2.setText(context.getResources().getString(R.string.lva2) + " " + sdf.format(new Date()));
            convertView.setTag(holder2);
        } else {
            RowItem rowItem = getItem(position);
            convertView = mInflater.inflate(R.layout.listview_lines, null);
            ViewHolder holder = new ViewHolder();
            holder.txtDesc = convertView.findViewById(R.id.line_b);
            holder.txtTitle = convertView.findViewById(R.id.line_a);
            holder.imageView = convertView.findViewById(R.id.icon);
            holder.imageText = convertView.findViewById(R.id.icon_text);
            convertView.setTag(holder);

            // List Header
            if (rowItem.getVisited() == 0) {
                holder.txtTitle.setText(Html.fromHtml("<font color=\"#668DC2\">" + rowItem.getTitle() + "<font>"));
            } else {
                holder.txtTitle.setText(Html.fromHtml("<font color=\"#8f8f8f\">" + rowItem.getTitle() + "<font>"));
            }

            // List Content
            if (rowItem.getVisited() == 0) {
                holder.txtDesc.setText(Html.fromHtml("<font color=\"black\">" + rowItem.getDesc() + "...<font>"));
            } else {
                holder.txtDesc.setText(Html.fromHtml("<font color=\"#bfbfbf\">" + rowItem.getDesc() + "...<font>"));
            }

            holder.imageView.setImageResource(rowItem.getImageId());
            holder.imageText.setText(rowItem.getDistance() + "\nmeters");
        }


        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
        TextView imageText;
    }

    private class ViewHolderHeader {
        ImageView imageView;
        TextView txtTitle;
        TextView txtTitle2;
    }
}
