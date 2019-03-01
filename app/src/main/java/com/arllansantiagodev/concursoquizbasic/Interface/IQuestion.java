package com.arllansantiagodev.concursoquizbasic.Interface;

import com.arllansantiagodev.concursoquizbasic.Model.CurrentQuestion;

public interface IQuestion {
    CurrentQuestion getSelectedAnswer();
    void showCorrectAnswer(); //Bold correct Answer text
    void disableAnswer(); //Disable all check box
    void resetQuestion(); // Reset all function on question
}
