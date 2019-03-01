package com.arllansantiagodev.concursoquizbasic.Common;

import android.os.CountDownTimer;

import com.arllansantiagodev.concursoquizbasic.Adapter.AnswerSheetAdapter;
import com.arllansantiagodev.concursoquizbasic.Model.Category;
import com.arllansantiagodev.concursoquizbasic.Model.CurrentQuestion;
import com.arllansantiagodev.concursoquizbasic.Model.Question;
import com.arllansantiagodev.concursoquizbasic.QuestionFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Common {

    public static Category selectedCategory = new Category();
    public static List<Question> questionList = new ArrayList<>();
    public static List<CurrentQuestion> answerSheetList = new ArrayList<>();
    public static CountDownTimer countDownTimer;
    public static final int TOTAL_TIME = 20*60*1000; // 20min
    public static int right_answer_count = 0 ;
    public static int wrong_answer_count = 0 ;
    public static ArrayList<QuestionFragment> framentsList =  new ArrayList<>();
    public static TreeSet<String> selected_values = new TreeSet<>();

    public enum ANSWER_TYPE{
        NO_ANSWER,
        WRONG_ANSWER,
        RIGHT_ANSWER

    }
}
