package com.arllansantiagodev.concursoquizbasic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;

import com.arllansantiagodev.concursoquizbasic.Adapter.CategoryAdapter;
import com.arllansantiagodev.concursoquizbasic.Common.SpaceDecoration;
import com.arllansantiagodev.concursoquizbasic.DBHelper.DBHelper;


public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recycler_category;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Concurso Quiz");
        setSupportActionBar(toolbar);

        recycler_category = (RecyclerView)findViewById(R.id.recycler_category);
        recycler_category.setHasFixedSize(true);
        recycler_category.setLayoutManager(new GridLayoutManager(this,2));

        //Get Screen height
        CategoryAdapter adapter = new CategoryAdapter(MainActivity.this,DBHelper.getInstance(this).getAllCategories());
        int spaceInPixel = 4;
        recycler_category.addItemDecoration(new SpaceDecoration(spaceInPixel));
        recycler_category.setAdapter(adapter);


    }

}
