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
import java.util.Timer;
import java.util.TimerTask;

public class SearchActivity extends Activity {

    static String mLastCode = "";
    static String mInitOutput = "";
    EditText mCode;
    TextView mOutput;
    ProgressDialog mProgress;
    Timer mTimer;

    private void saveCode(String code) {
        SharedPreferences sp = getSharedPreferences("code", Context.MODE_PRIVATE);
        sp.edit().putString("code", code).apply();
    }

    private String loadCode() {
        SharedPreferences sp = getSharedPreferences("code", Context.MODE_PRIVATE);
        return sp.getString("code", "");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    private void initProgress(int max, String title) {
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
                                mProgress.setTitle(CardAnalyzer.getFoundCards() + " card" + (foundCards == 1 ? "" : "s") + " found ...");
                            }
                        }
                    }
                });
            }
        }, 0, 200);
    }

    private void initDatabase(final Runnable search) {
        initProgress(CardAnalyzer.getInitProgressMax(), "Initializing...");

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
                initProgress(CardAnalyzer.getSearchProgressMax(), "Searching...");

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

                                ((Button) findViewById(R.id.help_button)).setText("Help");

                                switch (ret) {
                                    case 0:
                                        mOutput.setText("Found No Card!");
                                        return;
                                    case -1:
                                        mOutput.setText(LuaScript.getOutput());
                                        ((Button) findViewById(R.id.help_button)).setText("Show");
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

    private void trimCode() {
        String text = mCode.getText().toString();
        mCode.setText(text.trim());
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

        mCode = (EditText) findViewById(R.id.code);
        mCode.setText(loadCode());

        mOutput = (TextView) findViewById(R.id.output);
        mOutput.setText(CardAnalyzer.getFilterString());

        Button initButton = (Button) findViewById(R.id.init_button);
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

        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trimCode();
                saveCode(mCode.getText().toString());
                searchDatabase(false);
            }
        });
        searchButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                trimCode();
                saveCode(mCode.getText().toString());
                searchDatabase(true);
                return true;
            }
        });

        Button sortButton = (Button) findViewById(R.id.sort_button);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sort = CardAnalyzer.switchSortType(false);
                Toast.makeText(SearchActivity.this, sort, Toast.LENGTH_SHORT).show();
            }
        });
        sortButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String sort = CardAnalyzer.switchSortType(true);
                Toast.makeText(SearchActivity.this, sort, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Button cleanButton = (Button) findViewById(R.id.clean_button);
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

        Button helpButton = (Button) findViewById(R.id.help_button);
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
    }
}
