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

        Button initButton = (Button) findViewById(R.id.init_button);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardAnalyzer.initData();
                Toast.makeText(SearchActivity.this, "Done!", Toast.LENGTH_SHORT).show();
            }
        });

        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = (EditText) findViewById(R.id.code);
                TextView output = (TextView) findViewById(R.id.output);

                saveCode(code.getText().toString());

                String[] cards = CardAnalyzer.searchCard(code.getText().toString());
                output.setText(LuaScript.output);
                if(cards == null) {
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
                code.setText("name -> String\notherPart -> String\npartIndex -> Integer\nisSplit -> Boolean\n" +
                        "isDoubleFaced -> Boolean\nisFlip -> Boolean\nisFun -> Boolean\nisInCore -> Boolean\n" +
                        "types -> StringArray\nsubTypes -> StringArray\nsuperTypes -> StringArray\nmana -> String\n" +
                        "converted -> Integer\ncolorIndicator -> String\npower -> String\ntoughness -> String\n" +
                        "loyalty -> String\ntext -> String\nrules -> String\nlegal -> StringArray\n" +
                        "restricted -> StringArray\nbanned -> StringArray\nreserved -> Boolean\nrarityChanged -> Boolean\n" +
                        "\nmultiverseid -> Integer\nrating -> Float\nvotes -> Integer\nset -> String\ncode -> String\n" +
                        "folder -> String\naltCode -> String\nnumber -> String\nflavor -> String\nartist -> String\n" +
                        "rarity -> String\nwatermark -> String\nspecialType -> String\npicture -> String\n" +
                        "sameIndex -> Integer\nformatedNumber -> String");
            }
        });
    }
}
