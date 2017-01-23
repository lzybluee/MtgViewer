package lu.cifer.mtgviewer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.Vector;

public class MainActivity extends Activity {

    public static File SDPath;
    public static String urlInfo = "";
    Vector<String> mCardPath = new Vector<>();
    boolean mShuffle = true;
    boolean mAscending = false;
    boolean mSelect = false;
    String mSets = "";
    String[] mMiscSets = null;
    long mBackPressed = 0;

    void processFile(File file) {
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                System.out.println(f);
                processFile(f);
            } else {
                if (f.getName().endsWith(".jpg") || f.getName().endsWith(".png")) {
                    mCardPath.add(file.getAbsolutePath() + "/" + f.getName());
                }
            }
        }
    }

    void showSelectedSets() {
        String str = "Selected Sets:\n";
        if (mSets.equals("")) {
            str += "All";
        } else {
            String[] paths = mSets.split("\\|");
            int i = 0;
            for (String s : paths) {
                i++;
                str += s + ((i == paths.length) ? "" : "\n");
            }
        }

        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    void initGallery() {
        Gallery gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setAdapter(new GalleryAdapter());
        gallery.setBackgroundColor(0x222222);
        gallery.setSpacing(20);

        gallery.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                CardParser.rulePage++;
                Toast.makeText(
                        MainActivity.this,
                        CardParser.getCardInfo(mCardPath.get(position), false),
                        Toast.LENGTH_LONG).show();
            }
        });

        gallery.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v,
                                           int position, long id) {
                Toast.makeText(
                        MainActivity.this,
                        CardParser.getCardInfo(mCardPath.get(position), true),
                        Toast.LENGTH_LONG).show();
                return false;
            }

        });
    }

    void init(String[] cards) {
        mCardPath = new Vector<>();

        for (String card : cards) {
            mCardPath.add(SDPath + "/MTG/" + card);
        }

        Toast.makeText(MainActivity.this, "Total Cards : " + mCardPath.size(),
                Toast.LENGTH_SHORT).show();

        initGallery();
    }

    void init(String path) {

        if (mSelect) {
            String[] addPaths = path.split("\\|");
            String[] paths = mSets.split("\\|");
            boolean flag = false;
            for (String s : paths) {
                for (String s1 : addPaths) {
                    if (s1.equals(s)) {
                        flag = true;
                        if (mSets.contains("|" + s + "|")) {
                            mSets = mSets.replace("|" + s + "|", "|");
                        } else if (mSets.startsWith(s + "|")) {
                            mSets = mSets.substring(mSets.indexOf("|") + 1);
                        } else if (mSets.endsWith("|" + s)) {
                            mSets = mSets.substring(0, mSets.lastIndexOf("|"));
                        } else if (mSets.equals(s)) {
                            mSets = "";
                        }
                    }
                }
            }
            if (!flag && !path.equals("")) {
                mSets += (mSets.equals("") ? "" : "|") + path;
            }

            showSelectedSets();
            return;
        }

        CardAnalyzer.setFilter(path);

        mCardPath = new Vector<>();

        String[] paths = path.split("\\|");

        for (String s : paths) {
            File file = new File(SDPath + "/MTG/" + s);
            if (file.exists() && file.isDirectory()) {
                processFile(file);
            }
        }

        if (mShuffle) {
            Collections.shuffle(mCardPath);
        } else {
            Collections.sort(mCardPath);
            if (!mAscending) {
                Collections.reverse(mCardPath);
            }
        }

        Toast.makeText(MainActivity.this, "Total Cards : " + mCardPath.size(),
                Toast.LENGTH_SHORT).show();

        initGallery();
    }

    void saveSelectedSets(String sets) {
        SharedPreferences prep = getSharedPreferences("mtg", MODE_PRIVATE);
        Editor editor = prep.edit();
        editor.putString("selected_sets", sets);
        editor.apply();
    }

    String loadSelectedSets() {
        SharedPreferences prep = getSharedPreferences("mtg", MODE_PRIVATE);
        return prep.getString("selected_sets", "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 1, "All");
        menu.add(Menu.NONE, 1, 2, "Modern");
        menu.add(Menu.NONE, 2, 3, "Search");
        menu.add(Menu.NONE, 3, 4, "Shuffle");
        menu.add(Menu.NONE, 4, 5, "Numerical");
        menu.add(Menu.NONE, 5, 6, "Select");
        menu.add(Menu.NONE, 6, 7, "Done");
        menu.add(Menu.NONE, 7, 8, "Token");
        menu.add(Menu.NONE, 8, 9, "Plane");
        menu.add(Menu.NONE, 9, 10, "Scheme");
        menu.add(Menu.NONE, 10, 11, "Conspiracy");
        menu.add(Menu.NONE, 11, 12, "Unset");
        menu.add(Menu.NONE, 12, 13, "Promo");
        menu.add(Menu.NONE, 13, 14, "Special");
        menu.add(Menu.NONE, 14, 15, "Vanguard");
        for (int i = 0; i < CardParser.SetList.length; i++) {
            menu.add(Menu.NONE, i + 15, i + 16, CardParser.SetList[i][0]);
        }
        for (int i = 0; i < mMiscSets.length; i++) {
            menu.add(Menu.NONE, i + CardParser.SetList.length + 15, i + CardParser.SetList.length + 16,
                    mMiscSets[i].substring(mMiscSets[i].lastIndexOf("/") + 1));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int n = item.getItemId();

        if (n == 0) {
            init("Ancient|Modern|Commander|Planechase/PC2|Planechase/HOP|Archenemy/ARC|Conspiracy/CNS|Starter|Reprint/MMA|Reprint/MM2|Special/MBP|Token|Planechase/Plane|Planechase/Plane2012|Archenemy/Scheme|Conspiracy/Conspiracy|Unset");
        } else if (n == 1) {
            init("Modern");
        } else if (n == 2) {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
            finish();
        } else if (n == 3) {
            mShuffle = true;
            Toast.makeText(this, "Shuffle", Toast.LENGTH_SHORT).show();
        } else if (n == 4) {
            mShuffle = false;
            mAscending = !mAscending;
            Toast.makeText(this, mAscending ? "Ascending" : "Descending",
                    Toast.LENGTH_SHORT).show();
        } else if (n == 5) {
            mSelect = true;
            showSelectedSets();
        } else if (n == 6) {
            if (!mSelect && !mSets.equals("")) {
                mSets = "";
                saveSelectedSets(mSets);
                Toast.makeText(this, "Clear all selectd sets", Toast.LENGTH_SHORT).show();
            } else {
                mSelect = false;
                saveSelectedSets(mSets);
                init(mSets);
            }
        } else if (n == 7) {
            init("Token");
        } else if (n == 8) {
            init("Planechase/Plane|Planechase/Plane2012");
        } else if (n == 9) {
            init("Archenemy/Scheme");
        } else if (n == 10) {
            init("Conspiracy/Conspiracy");
        } else if (n == 11) {
            init("Unset");
        } else if (n == 12) {
            init("Promo");
        } else if (n == 13) {
            init("Special");
        } else if (n == 14) {
            init("Vanguard");
        } else if (n <= 14 + CardParser.SetList.length) {
            init(CardParser.SetList[n - 15][1]);
        } else {
            init(mMiscSets[n - 15 - CardParser.SetList.length]);
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String pictures[] = new String[0];
        if (getIntent() != null && getIntent().getExtras() != null) {
            pictures = getIntent().getExtras().getStringArray("pictures");
        }

        SDPath = Environment.getExternalStorageDirectory();

        if (!new File(SDPath + "/MTG/Back").exists()) {
            File[] dirs = this.getExternalFilesDirs(null);
            for (File f : dirs) {
                if (f == null)
                    continue;
                String s = f.getAbsolutePath();
                if (s.contains("/Android/data/")) {
                    s = s.substring(0, s.indexOf("/Android/data/"));
                    if (new File(s + "/MTG/Back").exists()) {
                        SDPath = new File(s);
                        break;
                    }
                }
            }
        }

        Vector<String> v1 = new Vector<>();
        File file1 = new File(SDPath + "/MTG/Misc/");
        if (file1.exists()) {
            File[] folders = file1.listFiles();
            for (File f : folders) {
                if (f.isDirectory()) {
                    v1.add(f.getName());
                }
            }
        }

        Vector<String> v2 = new Vector<>();
        File file2 = new File(SDPath + "/Misc/");
        if (file2.exists()) {
            File[] folders = file2.listFiles();
            for (File f : folders) {
                if (f.isDirectory()) {
                    v2.add(f.getName());
                }
            }
        }

        mMiscSets = new String[v1.size() + v2.size()];

        int j = 0;
        for (int i = 0; i < v1.size(); i++) {
            mMiscSets[j] = "/Misc/" + v1.get(i);
            j++;
        }
        for (int i = 0; i < v2.size(); i++) {
            mMiscSets[j] = "../Misc/" + v2.get(i);
            j++;
        }

        mSets = loadSelectedSets();

        setContentView(R.layout.main_layout);

        if (pictures != null && pictures.length > 0) {
            init(pictures);
        } else {
            init("Back");
        }
    }

    @Override
    public void onBackPressed() {
        if (mSelect) {
            if (mSets.equals("")) {
                mSelect = false;
                Toast.makeText(this, "Cancel select", Toast.LENGTH_SHORT).show();
            } else {
                mSelect = false;
                saveSelectedSets(mSets);
                init(mSets);
            }
        } else if (System.currentTimeMillis() - mBackPressed <= 1000) {
            saveSelectedSets("");
            super.onBackPressed();
        } else {
            mBackPressed = System.currentTimeMillis();
            Toast.makeText(this, "Press BACK again to quit", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((keyCode == KeyEvent.KEYCODE_SEARCH || (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() > 0)) && !urlInfo.equals("")) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(urlInfo);
                intent.setData(content_url);
                startActivity(intent);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    class GalleryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCardPath.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            urlInfo = "";
            CardParser.rulePage = 0;
            ImageView view = new ImageView(MainActivity.this);
            Bitmap bitmap = BitmapFactory.decodeFile(mCardPath.get(position));

            boolean wotc = (bitmap.getWidth() == 265 && bitmap.getHeight() == 370);

            if (bitmap.getWidth() > bitmap.getHeight() || wotc) {
                Matrix m = new Matrix();
                m.postRotate(-90);
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, false);
                bitmap.recycle();
                view.setImageBitmap(newBitmap);
            } else {
                view.setImageBitmap(bitmap);
            }

            view.setBackgroundColor(0x222222);
            view.setLayoutParams(new Gallery.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            if (wotc) {
                view.setScaleType(ImageView.ScaleType.CENTER);
            } else {
                view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            return view;
        }

    }
}
