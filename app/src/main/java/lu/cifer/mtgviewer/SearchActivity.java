package lu.cifer.mtgviewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by cidana on 2017/1/20 020.
 */

public class SearchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        Button initButton = (Button) findViewById(R.id.init_button);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardAnalyzer.initData();
            }
        });

        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] cards = CardAnalyzer.searchCard("if(mana) then return 1string.find(mana, 'R') else return false end");
                if(cards == null) {
                    Toast.makeText(SearchActivity.this, LuaScript.output, Toast.LENGTH_LONG).show();
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
    }
}
