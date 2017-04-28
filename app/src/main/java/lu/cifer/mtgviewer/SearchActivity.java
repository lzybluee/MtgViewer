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

    static String[] SpecailFolder = new String[]{"Token", "Promo", "Special"};
    static int SpecialCards = 4408;
    static String mLastCode = "";
    static String mInitOutput = "";
    EditText mCode;
    TextView mOutput;
    ProgressDialog mProgress;
    Timer mTimer;

    boolean mStop;
    int mFound;
    int mChecked;

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
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
                mInitOutput = CardAnalyzer.initData();

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
                        final int ret = CardAnalyzer.searchCard(mCode.getText().toString(), inResult);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (mProgress != null) {
                                    mProgress.dismiss();
                                    mProgress = null;
                                    mTimer.cancel();
                                }

                                ((Button) findViewById(R.id.help_button)).setText("?");

                                switch (ret) {
                                    case 0:
                                        mOutput.setText("Found No Card!");
                                        return;
                                    case -1:
                                        mOutput.setText(LuaScript.getOutput());
                                        ((Button) findViewById(R.id.help_button)).setText("!");
                                        return;
                                    case -2:
                                        mOutput.setText("Empty Result!");
                                        return;
                                }

                                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
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

        for (File f : file.listFiles()) {
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
        initProgress(SpecialCards, "Searching...", false);
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

                for (String s : SpecailFolder) {
                    File folder = new File(MainActivity.SDPath + "/MTG/" + s);
                    processFolder(folder, search, anyWord, cards);
                }

                if (cards.isEmpty()) {
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
                for (int i = 0; i < CardAnalyzer.results.length; i++) {
                    CardAnalyzer.results[i] = cards.get(i);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgress != null) {
                            mProgress.dismiss();
                            mProgress = null;
                            mTimer.cancel();
                        }

                        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
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
                mLastCode = mCode.getText().toString();
                mCode.setText("");
            }
        });
        cleanButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!mLastCode.isEmpty()) {
                    mCode.setText(mLastCode);
                }
                return true;
            }
        });

        final Button helpButton = (Button) findViewById(R.id.help_button);
        helpButton.setText("?");
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String picture = CardAnalyzer.getWrongCard();
                if (picture != null) {
                    Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    openFile("/MTG/Script/card.lua");
                }
            }
        });
        helpButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openFile("/MTG/Script/global.lua");
                return true;
            }
        });

        final Button specialButton = (Button) findViewById(R.id.special_button);
        specialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mCode.getText().toString().trim();
                mCode.setText(text);
                saveCode(text);
                searchSpecial(mCode.getText().toString(), false);
            }
        });
        specialButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String text = mCode.getText().toString().trim();
                mCode.setText(text);
                saveCode(text);
                searchSpecial(mCode.getText().toString(), true);
                return true;
            }
        });

        final Button singleButton = (Button) findViewById(R.id.single_button);
        singleButton.setText(CardAnalyzer.getSingleMode() ? "1!" : "1+");
        singleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean single = CardAnalyzer.switchSingleMode();
                singleButton.setText(single ? "1!" : "1+");
            }
        });
    }
}
