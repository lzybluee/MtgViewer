package lu.cifer.mtgviewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class SearchActivity extends Activity {

    EditText code;
    TextView output;
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
        mProgress.setCancelable(false);
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
                final String ret = CardAnalyzer.initData();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.output)).setText(ret);

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

    private void searchDatabase() {
        initDatabase(new Runnable() {
            @Override
            public void run() {
                initProgress(CardAnalyzer.getSearchProgressMax(), "Searching...");

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        final int ret = CardAnalyzer.searchCard(code.getText().toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                output.setText(LuaScript.getOutput());

                                if (mProgress != null) {
                                    mProgress.dismiss();
                                    mProgress = null;
                                    mTimer.cancel();
                                }

                                if (ret == -1) {
                                    ((Button) findViewById(R.id.help_button)).setText("Show");
                                    return;
                                }
                                if (ret == 0) {
                                    Toast.makeText(SearchActivity.this, "Found no card", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        code = (EditText) findViewById(R.id.code);
        code.setText(loadCode());

        output = (TextView) findViewById(R.id.output);
        output.setText(CardAnalyzer.getFilterString());

        Button initButton = (Button) findViewById(R.id.init_button);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDatabase(null);
            }
        });

        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCode(code.getText().toString());
                searchDatabase();
            }
        });

        Button cleanButton = (Button) findViewById(R.id.clean_button);
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code.setText("");
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
                    code.setText("name -> String\notherPart -> String\npartIndex -> Integer\nisSplit -> Boolean\n" +
                            "isDoubleFaced -> Boolean\nisFlip -> Boolean\nisLegendary -> Boolean\nisFun -> Boolean\n" +
                            "isInCore -> Boolean\ntypes -> StringArray\nsubTypes -> StringArray\nsuperTypes -> StringArray\n" +
                            "mana -> String\nconverted -> Integer\ncolorIndicator -> String\npower -> String\ntoughness -> String\n" +
                            "loyalty -> String\ntext -> String\nrules -> String\nlegal -> StringArray\n" +
                            "restricted -> StringArray\nbanned -> StringArray\nreserved -> Boolean\nrarityChanged -> Boolean\n" +
                            "\nmultiverseid -> Integer\nrating -> Float\nvotes -> Integer\nset -> String\ncode -> String\n" +
                            "folder -> String\naltCode -> String\nnumber -> String\nflavor -> String\nartist -> String\n" +
                            "rarity -> String\nwatermark -> String\nspecialType -> String\npicture -> String\n" +
                            "sameIndex -> Integer\nformatedNumber -> String");
                }
            }
        });
    }
}
