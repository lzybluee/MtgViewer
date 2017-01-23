package lu.cifer.mtgviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {

    EditText code;
    TextView output;

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
                String ret = CardAnalyzer.initData();
                ((TextView) findViewById(R.id.output)).setText(ret);
            }
        });

        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCode(code.getText().toString());

                String[] cards = CardAnalyzer.searchCard(code.getText().toString());
                output.setText(LuaScript.output);
                if (cards == null) {
                    ((Button) findViewById(R.id.help_button)).setText("Show");
                    return;
                }
                if (cards.length == 0) {
                    Toast.makeText(SearchActivity.this, "Found no card", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putStringArray("pictures", cards);
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
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
                    Bundle bundle = new Bundle();
                    bundle.putStringArray("pictures", new String[]{picture});
                    Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                    intent.putExtras(bundle);
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
