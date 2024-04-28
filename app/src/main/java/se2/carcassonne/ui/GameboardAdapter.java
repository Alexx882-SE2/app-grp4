package se2.carcassonne.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import se2.carcassonne.R;

public class GameboardAdapter extends BaseAdapter {

    private final Context context;
    private final int rows;
    private final int cols;

    public GameboardAdapter(Context context, int rows, int cols) {
        this.context = context;
        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public int getCount() {
        return rows * cols;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(18, 15)); // Anpassen Sie die Größe nach Bedarf
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        int middleRow = (rows - 1) / 2;
        int middleCol = (cols - 1) / 2;
        boolean isMiddleField = position / cols == middleRow && position % cols == middleCol;
        //Startfield
        if (isMiddleField) {
            imageView.setImageResource(R.drawable.start_field);
        } else {
            // Andernfalls verwenden Sie das Standardbild
            imageView.setImageResource(R.drawable.backside);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imageView.getScaleX() == 1.0f) {
                    imageView.setScaleX(1.5f);
                    imageView.setScaleY(1.5f);
                } else {
                    imageView.setScaleX(1.0f);
                    imageView.setScaleY(1.0f);
                }
            }
        });



        return imageView;
    }
}
