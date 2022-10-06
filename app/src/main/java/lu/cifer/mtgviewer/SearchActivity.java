package lu.cifer.mtgviewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class SearchActivity extends Activity {

    static String[] SpecialFolder = new String[]{"Promo", "Special", "Token", "Vanguard"};
    static int SpecialCards;
    static String mLastCode = "";
    static String mInitOutput = "";
    EditText mCode;
    TextView mOutput;
    ProgressDialog mProgress;
    Timer mTimer;

    boolean mStop;
    int mFound;
    int mChecked;

    private void setScreenOn(final boolean on) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (on) {
                    SearchActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    SearchActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });
    }

    private void saveCode(String code) {
        SharedPreferences sp = getSharedPreferences("code", Context.MODE_PRIVATE);
        sp.edit().putString("code", code).apply();
    }

    private String loadCode() {
        SharedPreferences sp = getSharedPreferences("code", Context.MODE_PRIVATE);
        return sp.getString("code", "");
    }

    private void saveFilter(String filter) {
        SharedPreferences sp = getSharedPreferences("filter", Context.MODE_PRIVATE);
        sp.edit().putString("filter", filter).apply();
    }

    private String loadFilter() {
        SharedPreferences sp = getSharedPreferences("filter", Context.MODE_PRIVATE);
        return sp.getString("filter", "Modern");
    }

    private void savePromos(int num) {
        SharedPreferences sp = getSharedPreferences("promos", Context.MODE_PRIVATE);
        sp.edit().putInt("promos", num).apply();
    }

    private int loadPromos() {
        SharedPreferences sp = getSharedPreferences("promos", Context.MODE_PRIVATE);
        return sp.getInt("promos", 6666);
    }

    private void initProgress(int max, String title, boolean timer) {
        CardAnalyzer.initProgress();

        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(false);
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                CardAnalyzer.setStop();
            }
        });
        mProgress.setMax(max);
        mProgress.setTitle(title);
        mProgress.show();

        if (!timer) {
            return;
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgress != null) {
                            mProgress.setProgress(CardAnalyzer.getProgress());
                            int foundCards = CardAnalyzer.getFoundCards();
                            if (foundCards > 0) {
                                mProgress.setTitle(foundCards + " card" + (foundCards == 1 ? "" : "s") + " found ...");
                            }
                        }
                    }
                });
            }
        }, 0, 200);
    }

    private void initDatabase(final Runnable search) {
        initProgress(CardAnalyzer.getInitProgressMax(), "Initializing...", true);

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                setScreenOn(true);

                mInitOutput = CardAnalyzer.initData();

                setScreenOn(false);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.output)).setText(mInitOutput);

                        if (mProgress != null) {
                            mProgress.dismiss();
                            mProgress = null;
                            mTimer.cancel();
                        }

                        if (search != null) {
                            search.run();
                        }
                    }
                });
            }
        };

        new Thread(runnable).start();
    }

    private void searchDatabase(final boolean inResult) {

        initDatabase(new Runnable() {
            @Override
            public void run() {
                initProgress(CardAnalyzer.getSearchProgressMax(inResult), "Searching...", true);

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        setScreenOn(true);

                        final int ret = CardAnalyzer.searchCard(mCode.getText().toString(), inResult);

                        setScreenOn(false);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (mProgress != null) {
                                    mProgress.dismiss();
                                    mProgress = null;
                                    mTimer.cancel();
                                }

                                switch (ret) {
                                    case 0:
                                        mOutput.setText("Found No Card!");
                                        return;
                                    case -1:
                                        mOutput.setText(LuaScript.getOutput());
                                        return;
                                    case -2:
                                        mOutput.setText("Empty Result!");
                                        return;
                                }

                                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                };

                new Thread(runnable).start();
            }
        });
    }

    private void processFolder(File file, String search, boolean anyWord, Vector<String> cards) {
        if (mStop) {
            return;
        }

        for (File f : MainActivity.ListFiles(file)) {
            if (f.isDirectory()) {
                processFolder(f, search, anyWord, cards);
            } else {
                String path = f.getAbsolutePath();
                path = path.substring(path.indexOf("/MTG/") + 5);
                if (path.endsWith(".jpg") && CardAnalyzer.checkStringGroup(path, search, anyWord)) {
                    cards.add(path);
                    mFound++;
                }
                mChecked++;
            }
        }
    }

    private void searchSpecial(final String search, final boolean anyWord) {
        CardAnalyzer.exclude = 0;
        initProgress(loadPromos(), "Searching...", false);
        mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mStop = true;
            }
        });
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgress != null) {
                            mProgress.setProgress(mChecked);
                            if (mFound > 0) {
                                mProgress.setTitle(mFound + " card" + (mFound == 1 ? "" : "s") + " found ...");
                            }
                        }
                    }
                });
            }
        }, 0, 200);

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Vector<String> cards = new Vector<>();
                mStop = false;
                mFound = 0;
                mChecked = 0;

                setScreenOn(true);

                for (String s : SpecialFolder) {
                    File folder = new File(MainActivity.SDPath + "/MTG/" + s);
                    processFolder(folder, search, anyWord, cards);
                }

                savePromos(mChecked);

                if (cards.isEmpty()) {
                    setScreenOn(false);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgress != null) {
                                mProgress.dismiss();
                                mProgress = null;
                                mTimer.cancel();
                            }

                            Toast.makeText(SearchActivity.this, "Found No Card!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return;
                }

                if (CardAnalyzer.getSortType().equals("Random")) {
                    Collections.shuffle(cards);
                } else {
                    Collections.sort(cards, new Comparator<String>() {
                        @Override
                        public int compare(String left, String right) {
                            return CardAnalyzer.isReverse() ? right.compareTo(left) : left.compareTo(right);
                        }
                    });
                }

                CardAnalyzer.results = new String[cards.size()];
                CardAnalyzer.showResults = true;
                for (int i = 0; i < CardAnalyzer.results.length; i++) {
                    CardAnalyzer.results[i] = cards.get(i);
                }

                setScreenOn(false);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgress != null) {
                            mProgress.dismiss();
                            mProgress = null;
                            mTimer.cancel();
                        }

                        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        };

        new Thread(runnable).start();
    }

    private boolean trimCode() {
        String text = mCode.getText().toString().trim();
        mCode.setText(text);
        saveCode(text);
        saveFilter(CardAnalyzer.getFilterString());
        if (text.isEmpty()) {
            Toast.makeText(this, "Empty Text!", Toast.LENGTH_SHORT).show();
        }
        return text.isEmpty();
    }

    private void openFile(String path) {
        File file = new File(MainActivity.SDPath + path);
        if (file.exists()) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "text/plain");
            startActivity(intent);
        }
    }

    private void addFuncButton(int id) {
        final Button button = (Button) findViewById(id);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mCode.getText().toString();
                text += button.getHint();
                mCode.setText(text);
                mCode.setSelection(text.length());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        if (CardAnalyzer.getFilterString().equals("Back")) {
            CardAnalyzer.setFilter(loadFilter());
        }

        mCode = (EditText) findViewById(R.id.code);
        mCode.setText(loadCode());

        mOutput = (TextView) findViewById(R.id.output);
        mOutput.setText(CardAnalyzer.getFilterString());

        final Button initButton = (Button) findViewById(R.id.init_button);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDatabase(null);
            }
        });
        initButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CardAnalyzer.clearResults();
                Toast.makeText(SearchActivity.this, "Clear Results", Toast.LENGTH_SHORT).show();
                ((TextView) findViewById(R.id.output)).setText(mInitOutput);
                return true;
            }
        });

        final Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trimCode()) {
                    return;
                }
                searchDatabase(false);
            }
        });
        searchButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (trimCode()) {
                    return true;
                }
                searchDatabase(true);
                return true;
            }
        });

        final Button sortButton = (Button) findViewById(R.id.sort_button);
        sortButton.setText(CardAnalyzer.getSortType());
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sort = CardAnalyzer.switchSortType(true);
                sortButton.setText(sort);
            }
        });
        sortButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String sort = CardAnalyzer.switchSortType(false);
                sortButton.setText(sort);
                return true;
            }
        });

        final Button cleanButton = (Button) findViewById(R.id.clean_button);
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mCode.getText().toString();
                text = text.substring(0, text.length() - 1);
                mCode.setText(text);
                mCode.setSelection(text.length());
            }
        });
        cleanButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mCode.setText("");
                return true;
            }
        });

        final Button singleButton = (Button) findViewById(R.id.single_button);
        singleButton.setText(CardAnalyzer.getSingleMode() ? "Unique" : "All");
        singleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean single = CardAnalyzer.switchSingleMode();
                singleButton.setText(single ? "Unique" : "All");
            }
        });

        int[] buttons = {
                R.id.search_text_button,
                R.id.return_button,
                R.id.space_button,
                R.id.left_button,
                R.id.right_button,
                R.id.and_button,
                R.id.or_button,
                R.id.not_button,
                R.id.equal_button,
                R.id.greater_button,
                R.id.lesser_button,
                R.id.greater_equal_button,
                R.id.lesser_equal_button,
                R.id.not_equal_button,
                R.id.quote_button,
                R.id.white_button,
                R.id.blue_button,
                R.id.black_button,
                R.id.red_button,
                R.id.green_button,
                R.id.colors_button,
                R.id.multicolor_button,
                R.id.number_button,
                R.id.text_button,
                R.id.mana_button,
                R.id.value_button,
                R.id.name_button,
                R.id.flavor_button,
                R.id.creature_button,
                R.id.instant_button,
                R.id.sorcery_button,
                R.id.enchantment_button,
                R.id.land_button,
                R.id.artifact_button,
                R.id.planeswalker_button,
                R.id.tribal_button,
                R.id.power_button,
                R.id.toughness_button,
                R.id.loyalty_button,
                R.id.pn_button,
                R.id.tn_button,
                R.id.ln_button,
                R.id.hasname_button,
                R.id.hastext_button,
                R.id.containstype_button,
                R.id.legendary_button,
                R.id.cw_button,
                R.id.cu_button,
                R.id.cb_button,
                R.id.cr_button,
                R.id.cg_button,
                R.id.common_button,
                R.id.uncommon_button,
                R.id.rare_button,
                R.id.mythic_button,
                R.id.part_index_button,
                R.id.double_button,
                R.id.split_button,
                R.id.flip_button,
                R.id.set_button,
                R.id.code_button,
                R.id.artist_button,
                R.id.watermark_button,
                R.id.string_contains_button,
                R.id.string_contains_case_button,
                R.id.table_contains_str_button,
                R.id.table_contains_str_case_button,
        };
        for(int button : buttons) {
            addFuncButton(button);
        }
    }
}
