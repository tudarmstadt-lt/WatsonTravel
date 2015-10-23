package model;

import jwatson.AWatson;
import jwatson.answer.WatsonAnswer;
import jwatson.question.WatsonQuestion;

import java.net.MalformedURLException;

public class WatsonRequestHandler {

    public static WatsonAnswer askWatson(String question) {
        WatsonAnswer answer = null;
        AWatson watson  = null;

        try {
            watson = new AWatson("b73a0d5f-d436-42d5-aead-5b66217a025a", "aC8eYL2gFzs9", "https://gateway.watsonplatform.net/question-and-answer-beta/api/v1/question/travel");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        WatsonQuestion watsonQuestion = new WatsonQuestion.QuestionBuilder(question)
                .setNumberOfAnswers(10) // Provide ten possible answers
                .formatAnswer()        // Instruct Watson to deliver answers in HTML
                .create();
        if (watson != null) {
            answer = watson.askQuestion(watsonQuestion);
        }
        return answer;
    }

}
