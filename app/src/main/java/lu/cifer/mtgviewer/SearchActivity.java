package lu.cifer.mtgviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class SearchActivity extends Activity {

    static String mInitOutput = "";
    EditText mCode;
    TextView mOutput;
    ProgressDialog mProgress;
    Timer mTimer;

    private void setScreenOn(final boolean on) {
        runOnUiThread(() -> {
            if (on) {
                SearchActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                SearchActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

    private void initProgress(int max, String title) {
        CardAnalyzer.initProgress();

        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(false);
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setOnCancelListener(dialog -> CardAnalyzer.setStop());
        mProgress.setMax(max);
        mProgress.setTitle(title);
        mProgress.show();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (mProgress != null) {
                        mProgress.setProgress(CardAnalyzer.getProgress());
                        int foundCards = CardAnalyzer.getFoundCards();
                        if (foundCards > 0) {
                            mProgress.setTitle(foundCards + " card" + (foundCards == 1 ? "" : "s") + " found ...");
                        }
                    }
                });
            }
        }, 0, 200);
    }

    private void initDatabase(final Runnable search) {
        initProgress(CardAnalyzer.getInitProgressMax(), "Initializing...");

        final Runnable runnable = () -> {
            setScreenOn(true);

            mInitOutput = CardAnalyzer.initData();

            setScreenOn(false);

            runOnUiThread(() -> {
                ((TextView) findViewById(R.id.output)).setText(mInitOutput);

                if (mProgress != null) {
                    mProgress.dismiss();
                    mProgress = null;
                    mTimer.cancel();
                }

                if (search != null) {
                    search.run();
                }
            });
        };

        new Thread(runnable).start();
    }

    private void searchDatabase(final boolean inResult) {

        initDatabase(() -> {
            initProgress(CardAnalyzer.getSearchProgressMax(inResult), "Searching...");

            Runnable runnable = () -> {
                setScreenOn(true);

                final int ret = CardAnalyzer.searchCard(mCode.getText().toString(), inResult);

                setScreenOn(false);

                runOnUiThread(() -> {

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
                });
            };

            new Thread(runnable).start();
        });
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

    private void addFuncButton(int id) {
        final Button button = findViewById(id);
        button.setOnClickListener(view -> {
            String text = mCode.getText().toString();
            text += button.getHint();
            mCode.setText(text);
            mCode.setSelection(text.length());
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        if (CardAnalyzer.getFilterString().equals("Back")) {
            CardAnalyzer.setFilter(loadFilter());
        }

        mCode = findViewById(R.id.code);
        mCode.setText(loadCode());

        mOutput = findViewById(R.id.output);
        mOutput.setText(CardAnalyzer.getFilterString());

        final Button initButton = findViewById(R.id.init_button);
        initButton.setOnClickListener(view -> initDatabase(null));
        initButton.setOnLongClickListener(view -> {
            CardAnalyzer.clearResults();
            Toast.makeText(SearchActivity.this, "Clear Results", Toast.LENGTH_SHORT).show();
            ((TextView) findViewById(R.id.output)).setText(mInitOutput);
            return true;
        });

        final Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(view -> {
            if (trimCode()) {
                return;
            }
            searchDatabase(false);
        });
        searchButton.setOnLongClickListener(view -> {
            if (trimCode()) {
                return true;
            }
            searchDatabase(true);
            return true;
        });

        final Button sortButton = findViewById(R.id.sort_button);
        sortButton.setText(CardAnalyzer.getSortType());
        sortButton.setOnClickListener(view -> {
            String sort = CardAnalyzer.switchSortType(true);
            sortButton.setText(sort);
        });
        sortButton.setOnLongClickListener(view -> {
            String sort = CardAnalyzer.switchSortType(false);
            sortButton.setText(sort);
            return true;
        });

        final Button uniqueButton = findViewById(R.id.unique_button);
        uniqueButton.setText(CardAnalyzer.getUniqueMode() ? "Unique" : "All");
        uniqueButton.setOnClickListener(view -> {
            boolean single = CardAnalyzer.switchUniqueMode();
            uniqueButton.setText(single ? "Unique" : "All");
        });

        final Button otherButton = findViewById(R.id.other_button);
        otherButton.setText(CardAnalyzer.getIncludeOther() ? "Other" : "Face");
        otherButton.setOnClickListener(view -> {
            boolean other = CardAnalyzer.switchIncludeOther();
            otherButton.setText(other ? "Other" : "Face");
        });

        final Button cleanButton = findViewById(R.id.clear_button);
        cleanButton.setOnClickListener(view -> {
            String text = mCode.getText().toString();
            text = text.substring(0, text.length() - 1);
            mCode.setText(text);
            mCode.setSelection(text.length());
        });
        cleanButton.setOnLongClickListener(view -> {
            mCode.setText("");
            return true;
        });


        final Button helpButton = findViewById(R.id.help_button);
        helpButton.setOnClickListener(view -> new AlertDialog.Builder(SearchActivity.this)
                .setTitle("Help")
                .setMessage("name -> String\n" +
                        "simpleName -> String\n" +
                        "otherPart -> StringArray\n" +
                        "partIndex -> Integer\n" +
                        "isSplit -> Boolean\n" +
                        "isDoubleFaced -> Boolean\n" +
                        "isMDFC -> Boolean\n" +
                        "isFlip -> Boolean\n" +
                        "isAdventure -> Boolean\n" +
                        "isLegendary -> Boolean\n" +
                        "isFun -> Boolean\n" +
                        "types -> StringArray\n" +
                        "subTypes -> StringArray\n" +
                        "superTypes -> StringArray\n" +
                        "mana -> String\n" +
                        "value -> Integer\n" +
                        "colorIndicator -> String\n" +
                        "power -> String\n" +
                        "toughness -> String\n" +
                        "loyalty -> String\n" +
                        "text -> String\n" +
                        "rules -> String\n" +
                        "legal -> StringArray\n" +
                        "restricted -> StringArray\n" +
                        "banned -> StringArray\n" +
                        "reserved -> Boolean\n" +
                        "reprintTimes -> Integer\n" +
                        "\n" +
                        "multiverseId -> Integer\n" +
                        "set -> String\n" +
                        "code -> String\n" +
                        "folder -> String\n" +
                        "number -> String\n" +
                        "flavor -> String\n" +
                        "artist -> String\n" +
                        "rarity -> String\n" +
                        "watermark -> String\n" +
                        "picture -> String\n" +
                        "sameIndex -> Integer\n" +
                        "formattedNumber -> String\n" +
                        "setOrder -> Integer\n" +
                        "reprintIndex -> Integer\n" +
                        "\n" +
                        "function string_contains(s, str)\n" +
                        "function string_contains_case(s, str)\n" +
                        "function table_contains_str(t, str)\n" +
                        "function table_contains_str_case(t, str)\n" +
                        "function table_contains(t, element)\n" +
                        "function table_contains_case(t, element)").show());

        int[] buttons = {
                R.id.search_name_button,
                R.id.return_button,
                R.id.space_button,
                R.id.left_button,
                R.id.right_button,
                R.id.and_button,
                R.id.or_button,
                R.id.not_button,
                R.id.equal_button,
                R.id.greater_equal_button,
                R.id.greater_button,
                R.id.lesser_button,
                R.id.lesser_equal_button,
                R.id.not_equal_button,
                R.id.quote_button,
                R.id.apos_button,
                R.id.comma_button,
                R.id.white_button,
                R.id.blue_button,
                R.id.black_button,
                R.id.red_button,
                R.id.green_button,
                R.id.colors_button,
                R.id.multicolor_button,
                R.id.text_button,
                R.id.mana_button,
                R.id.value_button,
                R.id.name_button,
                R.id.flavor_button,
                R.id.clean_button,
                R.id.creature_button,
                R.id.instant_button,
                R.id.sorcery_button,
                R.id.enchantment_button,
                R.id.land_button,
                R.id.artifact_button,
                R.id.planeswalker_button,
                R.id.tribal_button,
                R.id.permanent_button,
                R.id.power_button,
                R.id.toughness_button,
                R.id.loyalty_button,
                R.id.pn_button,
                R.id.tn_button,
                R.id.ln_button,
                R.id.has_button,
                R.id.hasname_button,
                R.id.hastext_button,
                R.id.containstype_button,
                R.id.hastype_button,
                R.id.legendary_button,
                R.id.cw_button,
                R.id.cu_button,
                R.id.cb_button,
                R.id.cr_button,
                R.id.cg_button,
                R.id.ccolors_button,
                R.id.common_button,
                R.id.uncommon_button,
                R.id.rare_button,
                R.id.mythic_button,
                R.id.rare_and_mythic_button,
                R.id.reprint_index_button,
                R.id.part_index_button,
                R.id.double_button,
                R.id.mdfc_button,
                R.id.split_button,
                R.id.flip_button,
                R.id.number_button,
                R.id.code_button,
                R.id.set_button,
                R.id.artist_button,
                R.id.watermark_button,
                R.id.id_button,
                R.id.string_contains_button,
                R.id.string_contains_case_button,
                R.id.count_str_button,
                R.id.table_contains_str_button,
                R.id.table_contains_str_case_button,
        };
        for (int button : buttons) {
            addFuncButton(button);
        }
    }
}
