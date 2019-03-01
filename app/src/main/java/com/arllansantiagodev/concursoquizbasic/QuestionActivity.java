package com.arllansantiagodev.concursoquizbasic;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arllansantiagodev.concursoquizbasic.Adapter.AnswerSheetAdapter;
import com.arllansantiagodev.concursoquizbasic.Adapter.QuestionFragmentAdapter;
import com.arllansantiagodev.concursoquizbasic.Common.Common;
import com.arllansantiagodev.concursoquizbasic.DBHelper.DBHelper;
import com.arllansantiagodev.concursoquizbasic.Model.CurrentQuestion;
import com.arllansantiagodev.concursoquizbasic.Model.Question;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    int time_play = Common.TOTAL_TIME;
    boolean isAnswerModeView =false;
    CountDownTimer countDownTimer;
    TextView txt_right_answer, txt_timer, txt_wrong_answer;
    RecyclerView answer_sheet_view;
    AnswerSheetAdapter answerSheetAdapter;

    ViewPager viewPager;
    TabLayout tabLayout;
    @Override
    protected void onDestroy() {
        if(Common.countDownTimer != null){
            Common.countDownTimer.cancel();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(Common.selectedCategory.getName());
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        takeQuestion();

        if(Common.questionList.size()> 0){

            //Show TextView right answer and Text Viwer Timer
            txt_right_answer = (TextView)findViewById(R.id.txt_question_right);
            txt_timer = (TextView)findViewById(R.id.txt_timer);
            txt_right_answer.setVisibility(View.VISIBLE);

            txt_right_answer.setText(new StringBuilder(String.format("%d/%d", Common.right_answer_count,Common.questionList.size())));
            txt_timer.setVisibility(View.VISIBLE);

            countTimer();
            //View
            answer_sheet_view =(RecyclerView)findViewById(R.id.grid_answer);
            answer_sheet_view.setHasFixedSize(true);
            if(Common.questionList.size()> 5){
                answer_sheet_view.setLayoutManager(new GridLayoutManager(this, Common.questionList.size()/2));
            }
            answerSheetAdapter = new AnswerSheetAdapter(this,Common.answerSheetList);
            answer_sheet_view.setAdapter(answerSheetAdapter);

            viewPager =(ViewPager)findViewById(R.id.viewpager);
            tabLayout =(TabLayout)findViewById(R.id.slinding_tabs);
            genFragmentList();

            QuestionFragmentAdapter questionFragmentAdapter = new QuestionFragmentAdapter(getSupportFragmentManager(),
                    this,Common.framentsList);

            viewPager.setAdapter(questionFragmentAdapter);
            tabLayout.setupWithViewPager(viewPager);

            //Event
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
                int SCROLLING_RIGHT = 0;
                int SCROLLING_LEFT  = 1;
                int SCROLLING_UNDETERMINED = 2;

                int currentScrollDirection = 2;

                private void setScrollingDirection(float positionOffset){
                    if((1-positionOffset) >= 0.5){
                        this.currentScrollDirection = SCROLLING_RIGHT;
                    }else if((1-positionOffset) <= 0.5){
                        this.currentScrollDirection = SCROLLING_LEFT;
                    }
                }
                private boolean isScrollDirectionUndetermined(){
                    return currentScrollDirection == SCROLLING_UNDETERMINED;
                }
                private boolean isScrollingRight(){
                    return currentScrollDirection == SCROLLING_RIGHT;
                }
                private boolean isScrollingLeft(){
                    return currentScrollDirection == SCROLLING_LEFT;
                }

                @Override
                public void onPageScrolled(int i, float v, int i1) {
                     if(isScrollDirectionUndetermined())
                         setScrollingDirection(v);
                }

                @Override
                public void onPageSelected(int i) {
                    QuestionFragment questionFragment;
                    int position = 0;
                    if(i>0) {
                        if (isScrollingRight()) {
                            //If user scroll to right, get previous fragment to calculte result.
                            questionFragment = Common.framentsList.get(i - 1);
                            position = i - 1;
                        } else if (isScrollingLeft()) {
                            //If user scroll to left, get next fragment to calculte result.
                            questionFragment = Common.framentsList.get(i + 1);
                            position = i + 1;
                        } else {
                            questionFragment = Common.framentsList.get(position);
                        }
                    }else{
                        questionFragment = Common.framentsList.get(0);
                        position = 0;
                    }

                    //If you want to show correct answer, just call function here
                    CurrentQuestion question_state = questionFragment.getSelectedAnswer();
                    Common.answerSheetList.set(position,question_state);//Set question answer for answersheet
                    answerSheetAdapter.notifyDataSetChanged();// Change color in answer sheet
                    countCorrectAnswer();
                    txt_right_answer.setText(new StringBuilder(String.format("%d",Common.right_answer_count))
                    .append("/")
                    .append(String.format("%d",Common.questionList.size())).toString());
                    txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));
                    if(question_state.getType()== Common.ANSWER_TYPE.NO_ANSWER){
                        questionFragment.showCorrectAnswer();
                        questionFragment.disableAnswer();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                    if(i==ViewPager.SCROLL_STATE_IDLE){
                        this.currentScrollDirection = SCROLLING_UNDETERMINED;
                    }
                }

            });
        }

    }

    private void finishQuiz() {
        int position = viewPager.getCurrentItem();
        QuestionFragment questionFragment = Common.framentsList.get(position);
        //If you want to show correct answer, just call function here
        CurrentQuestion question_state = questionFragment.getSelectedAnswer();
        Common.answerSheetList.set(position,question_state);//Set question answer for answersheet
        answerSheetAdapter.notifyDataSetChanged();// Change color in answer sheet

        countCorrectAnswer();
        txt_right_answer.setText(new StringBuilder(String.format("%d",Common.right_answer_count))
                .append("/")
                .append(String.format("%d",Common.questionList.size())).toString());
        txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

        if(question_state.getType()== Common.ANSWER_TYPE.NO_ANSWER){
            questionFragment.showCorrectAnswer();
            questionFragment.disableAnswer();
        }

        //We will navigate to new Result Activity here

    }

    private void countCorrectAnswer() {
      //Reset varible
      Common.right_answer_count = Common.wrong_answer_count = 0;
      for(CurrentQuestion item:Common.answerSheetList){
          if(item.getType()== Common.ANSWER_TYPE.RIGHT_ANSWER){
              Common.right_answer_count++;
          }else if(item.getType() == Common.ANSWER_TYPE.WRONG_ANSWER){
              Common.wrong_answer_count++;
          }
      }
    }

    private void genFragmentList() {
        Common.framentsList.clear();
        for(int i =0 ; i<Common.questionList.size();i++){
            Bundle bundle = new Bundle();
            bundle.putInt("index",i);
            QuestionFragment fragment = new QuestionFragment();
            fragment.setArguments(bundle);

            Common.framentsList.add(fragment);
        }
    }

    private void countTimer() {
       if(Common.countDownTimer == null ) {
            Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME,1000) {
                @Override
                public void onTick(long l) {
                    txt_timer.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(l),
                            TimeUnit.MILLISECONDS.toSeconds(l) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                            time_play-=1000;
                }

                @Override
                public void onFinish() {
                    //FINISH GAME
                }
            }.start();
       } else{
           Common.countDownTimer.cancel();
           Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME,1000) {
               @Override
               public void onTick(long l) {
                   txt_timer.setText(String.format("%02d:%02d",
                           TimeUnit.MILLISECONDS.toMinutes(l),
                           TimeUnit.MILLISECONDS.toSeconds(l) -
                                   TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                   time_play-=1000;
               }

               @Override
               public void onFinish() {
                   //FINISH GAME
               }
           }.start();
       }

    }

    private void takeQuestion() {
        Common.questionList = DBHelper.getInstance(this).getQuestionByCategory(Common.selectedCategory.getId());

        if(Common.questionList.size() == 0){
            new MaterialStyledDialog.Builder(this)
                    .setTitle("Oppps!")
                    .setIcon(R.drawable.ic_sentiment_very_dissatisfied_black_24dp)
                    .setDescription("We don't have any question in this "+ Common.selectedCategory.getName() + " category")
                    .setPositiveText("OK")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        } else{
            //Gen answerSheet item from question
            //30 questions =  30 answer sheet item
            //1  question  =   1 answer sheet item
            Common.answerSheetList.clear();
            for(int i = 0; i < Common.questionList.size() ; i++){
                Common.answerSheetList.add(new CurrentQuestion(i,Common.ANSWER_TYPE.NO_ANSWER)); // Default All answer is no_answer
            }


        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_wrong_answer);
        ConstraintLayout constraintLayout = (ConstraintLayout)item.getActionView();
        txt_wrong_answer = (TextView)constraintLayout.findViewById(R.id.txt_wrong_answer);
        txt_wrong_answer.setText(String.valueOf(0));
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_finish_quiz) {
            if(!isAnswerModeView){
                new MaterialStyledDialog.Builder(this).setTitle("Finish?")
                .setIcon(R.drawable.ic_mood_black_24dp)
                .setDescription("Do you really want to finish?")
                .setNegativeText("No")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                       dialog.dismiss();
                    }
                })
                .setPositiveText("Yes")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finishQuiz();
                    }
                }).show();

            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
