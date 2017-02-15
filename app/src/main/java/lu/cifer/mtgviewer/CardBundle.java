package lu.cifer.mtgviewer;

import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Vector;

public class CardBundle {

    static Vector<String> bundle = new Vector<>();

    public static void showDialog(final MainActivity activity) {
        final AlertDialog dialog = new AlertDialog.Builder(activity).create();
        dialog.show();
        dialog.getWindow().setContentView(R.layout.dialog_layout);

        Button addButton = (Button) dialog.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String card = activity.mCardPath.get((int) activity.mGallery.getSelectedItemId()).replace(MainActivity.SDPath + "/MTG/", "");
                if (!bundle.contains(card)) {
                    bundle.add(card);
                    activity.saveBundle(bundle);
                    Toast.makeText(activity, "#" + bundle.size() + " Added to Bundle", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Already in Bundle", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        Button showButton = (Button) dialog.findViewById(R.id.show_button);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.init(bundle.toArray(new String[0]));
                dialog.dismiss();
            }
        });

        if (bundle.isEmpty()) {
            showButton.setEnabled(false);
            showButton.setText("Show");
        } else {
            showButton.setEnabled(true);
            showButton.setText("Show " + bundle.size());
        }

        Button clearButton = (Button) dialog.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.clear();
                activity.saveBundle(bundle);
                Toast.makeText(activity, "Bundle Cleared", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        Button removeButton = (Button) dialog.findViewById(R.id.remove_button);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String card = activity.mCardPath.get((int) activity.mGallery.getSelectedItemId()).replace(MainActivity.SDPath + "/MTG/", "");
                if (bundle.contains(card)) {
                    bundle.remove(card);
                    activity.saveBundle(bundle);
                    Toast.makeText(activity, "Removed from Bundle", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Not in Bundle", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
    }
}
