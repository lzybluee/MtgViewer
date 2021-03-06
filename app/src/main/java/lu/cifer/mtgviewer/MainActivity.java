package lu.cifer.mtgviewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MainActivity extends Activity {

    public static final int SORT_SUFFLE = 0;
    public static final int SORT_ASCEND = 1;
    public static final int SORT_DESCEND = 2;
    public static final String[] SORT_DESC = new String[]{"Shuffle", "Ascending", "Descending"};

    public static File SDPath;
    public static String urlInfo = "";
    Gallery mGallery;
    Vector<String> mCardPath = new Vector<>();
    int mSort = SORT_SUFFLE;
    boolean mSelect = false;
    String mSets = "";
    String[] mMiscSets = null;
    long mBackPressed = 0;
    ProgressDialog mProgress;
    Timer mTimer;
    String mProcessSet;
    int mLoadCards;
    boolean mStop;

    public static List<File> ListFiles(File file) {
        List<File> files = Arrays.asList(file.listFiles());
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && f2.isFile()) {
                    return -1;
                }
                if (f1.isFile() && f2.isDirectory()) {
                    return 1;
                }
                return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
            }
        });
        return files;
    }

    private void openUrl() {
        if (urlInfo == null || urlInfo.isEmpty()) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(urlInfo);
        intent.setData(content_url);
        startActivity(intent);
    }

    private void setScreenOn(final boolean on) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (on) {
                    MainActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    MainActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });
    }

    void processFile(File file) {
        if (mStop) {
            return;
        }
        for (File f : ListFiles(file)) {
            if (f.isDirectory()) {
                mProcessSet = f.toString().replace(SDPath + "/MTG/", "");
                processFile(f);
            } else {
                if (f.getName().endsWith(".jpg") || f.getName().endsWith(".png")) {
                    mLoadCards++;
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
        mGallery = (Gallery) findViewById(R.id.gallery);
        mGallery.setAdapter(new GalleryAdapter());
        mGallery.setBackgroundColor(0x222222);
        mGallery.setSpacing(20);

        mGallery.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                CardParser.rulePage++;
                Toast.makeText(
                        MainActivity.this,
                        "[" + (position + 1) + " / " + mCardPath.size() + "]\n"
                                + CardParser.getCardInfo(mCardPath.get(position), false),
                        Toast.LENGTH_LONG).show();
            }
        });

        mGallery.setOnItemLongClickListener(new OnItemLongClickListener() {
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

    void init(String[] cards, int exclude) {
        mCardPath = new Vector<>();

        for (String card : cards) {
            mCardPath.add(SDPath + "/MTG/" + card);
        }

        Toast.makeText(MainActivity.this, "Total Cards : " + mCardPath.size() + (exclude == 0 ? "" : " (Exclude " + exclude + ")"),
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

        final String[] paths = path.split("\\|");

        if (!path.equals("Back")) {
            mProgress = new ProgressDialog(this);
            mProgress.setIndeterminate(true);
            mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mStop = true;
                }
            });
            mProgress.setMessage("Processing...\n");
            mProgress.show();

            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgress != null && mProcessSet != null) {
                                mProgress.setMessage("Processing...\n" + mProcessSet + (mLoadCards > 0 ? "\n(" + mLoadCards + " cards)" : ""));
                            }
                        }
                    });
                }
            }, 0, 200);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mStop = false;
                mLoadCards = 0;

                setScreenOn(true);

                for (String s : paths) {
                    File file = new File(SDPath + "/MTG/" + s);
                    if (file.exists() && file.isDirectory()) {
                        processFile(file);
                    }
                }

                if (mSort == SORT_SUFFLE) {
                    Collections.shuffle(mCardPath);
                } else {
                    Collections.sort(mCardPath);
                    if (mSort == SORT_DESCEND) {
                        Collections.reverse(mCardPath);
                    }
                }

                setScreenOn(false);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initGallery();

                        Toast.makeText(MainActivity.this, "Total Cards : " + mCardPath.size(),
                                Toast.LENGTH_SHORT).show();

                        if (mProgress != null) {
                            mProgress.dismiss();
                            mProgress = null;
                            mTimer.cancel();
                            mProcessSet = null;
                        }
                    }
                });
            }
        };

        new Thread(runnable).start();
    }

    void saveBundle(Vector<String> bundle) {
        SharedPreferences prep = getSharedPreferences("bundle", MODE_PRIVATE);
        SharedPreferences.Editor editor = prep.edit();
        editor.putStringSet("bundle", new HashSet<>(bundle));
        editor.apply();
    }

    void loadBundle() {
        SharedPreferences prep = getSharedPreferences("bundle", MODE_PRIVATE);
        Set<String> set = prep.getStringSet("bundle", null);
        CardBundle.bundle.clear();
        if (set != null && !set.isEmpty()) {
            for (String s : set) {
                if (new File(SDPath + "/MTG/" + s).exists()) {
                    CardBundle.bundle.add(s);
                }
            }
        }
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
        menu.add(Menu.NONE, 0, 1, "MTG");
        menu.add(Menu.NONE, 1, 2, "Modern");
        menu.add(Menu.NONE, 2, 3, "Search");
        menu.add(Menu.NONE, 3, 4, "Comment");
        menu.add(Menu.NONE, 4, 5, "Sort");
        menu.add(Menu.NONE, 5, 6, "All");
        menu.add(Menu.NONE, 6, 7, "Select");
        menu.add(Menu.NONE, 7, 8, "Done");
        menu.add(Menu.NONE, 8, 9, "Bundle");
        menu.add(Menu.NONE, 9, 10, "Ancient");
        menu.add(Menu.NONE, 10, 11, "Token");
        menu.add(Menu.NONE, 11, 12, "Promo");
        menu.add(Menu.NONE, 12, 13, "Special");
        menu.add(Menu.NONE, 13, 14, "Vanguard");
        for (int i = 0; i < CardParser.SetList.length; i++) {
            menu.add(Menu.NONE, i + 14, i + 15, CardParser.SetList[i][0]);
        }
        for (int i = 0; i < mMiscSets.length; i++) {
            menu.add(Menu.NONE, i + CardParser.SetList.length + 14, i + CardParser.SetList.length + 15,
                    mMiscSets[i].substring(mMiscSets[i].lastIndexOf("/") + 1));
        }
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle());
            spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0);
            item.setTitle(spanString);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int n = item.getItemId();

        if (n == 0) {
            init("Ancient|Modern|Commander|Planechase/PC2|Conspiracy|Starter|Other");
        } else if (n == 1) {
            init("Modern");
        } else if (n == 2) {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        } else if (n == 3) {
            openUrl();
        } else if (n == 4) {
            mSort = (mSort + 1) % 3;
            CardAnalyzer.setReverse(mSort == SORT_DESCEND);
            Toast.makeText(this, SORT_DESC[mSort], Toast.LENGTH_SHORT).show();
        } else if (n == 5) {
            init("Ancient|Modern|Commander|Planechase|Archenemy|Conspiracy|Starter|Other|Unset|Reprint");
        } else if (n == 6) {
            mSelect = true;
            showSelectedSets();
        } else if (n == 7) {
            if (!mSelect) {
                mSets = "";
                saveSelectedSets(mSets);
                Toast.makeText(this, "Clear all selectd sets", Toast.LENGTH_SHORT).show();
            } else {
                mSelect = false;
                saveSelectedSets(mSets);
                init(mSets);
            }
        } else if (n == 8) {
            CardBundle.showDialog(this);
        } else if (n == 9) {
            init("Ancient");
        } else if (n == 10) {
            init("Token");
        } else if (n == 11) {
            init("Promo");
        } else if (n == 12) {
            init("Special");
        } else if (n == 13) {
            init("Vanguard");
        } else if (n <= 13 + CardParser.SetList.length) {
            init(CardParser.SetList[n - 14][1]);
        } else {
            init(mMiscSets[n - 14 - CardParser.SetList.length]);
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String pictures[] = new String[0];
        if (CardAnalyzer.showResults && CardAnalyzer.results != null) {
            pictures = CardAnalyzer.results;
            CardAnalyzer.showResults = false;
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

        File test = new File(SDPath + "/MTG");
        if(test.listFiles() == null) {
            Toast.makeText(this, "Need read permission!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        CardParser.initOracle();

        Vector<String> v1 = new Vector<>();
        File file1 = new File(SDPath + "/MTG/Misc/");
        if (file1.exists()) {
            for (File f : ListFiles(file1)) {
                if (f.isDirectory()) {
                    if(!CardParser.containsCode(f.getName())) {
                        v1.add(f.getName());
                    }
                }
            }
        }

        Vector<String> v2 = new Vector<>();
        File file2 = new File(SDPath + "/Misc/");
        if (file2.exists()) {
            for (File f : ListFiles(file2)) {
                if (f.isDirectory()) {
                    if(!CardParser.containsCode(f.getName())) {
                        v2.add(f.getName());
                    }
                }
            }
        }

        mMiscSets = new String[v1.size() + v2.size()];

        int j = 0;
        for (int i = 0; i < v1.size(); i++) {
            mMiscSets[j] = "Misc/" + v1.get(i);
            j++;
        }
        for (int i = 0; i < v2.size(); i++) {
            mMiscSets[j] = "../Misc/" + v2.get(i);
            j++;
        }

        loadBundle();
        mSets = loadSelectedSets();

        setContentView(R.layout.main_layout);

        if (pictures != null && pictures.length > 0) {
            init(pictures, CardAnalyzer.exclude);
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
            super.onBackPressed();
            LuaScript.closeLua();
            System.exit(0);
        } else {
            mBackPressed = System.currentTimeMillis();
            Toast.makeText(this, "Press BACK again to quit", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_SEARCH || (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() > 0)) {
                openUrl();
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

            if (bitmap != null) {
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    Matrix m = new Matrix();
                    m.postRotate(-90);
                    Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                            bitmap.getWidth(), bitmap.getHeight(), m, false);
                    bitmap.recycle();
                    view.setImageBitmap(newBitmap);
                } else {
                    view.setImageBitmap(bitmap);
                }
            }

            view.setBackgroundColor(0x222222);
            view.setLayoutParams(new Gallery.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            view.setScaleType(ImageView.ScaleType.FIT_CENTER);

            return view;
        }

    }
}
