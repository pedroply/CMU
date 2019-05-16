package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

    private final Context mContext;
    private final Bitmap[] bitmaps;

    public ImageAdapter(Context context, Bitmap[] bitmaps) {
        this.mContext = context;
        this.bitmaps = bitmaps;
    }

    @Override
    public int getCount() {
        return bitmaps.length;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bitmap bitmap = bitmaps[position];

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.activity_photo, null);
        }

        ImageView imageView = (ImageView)convertView.findViewById(R.id.photo);
        imageView.setImageBitmap(bitmap);

        return convertView;
    }
}