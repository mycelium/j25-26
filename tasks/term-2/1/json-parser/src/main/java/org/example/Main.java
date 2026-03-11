package org.example;

import org.example.json.Json;
import org.example.json.JsonConfig;

import java.util.Map;

public class Main {

    public static class Learner {
        public String last_name;
        public String first_name;
        public String middle_name;
        public int grade;
        public String city;
        public String study_goal;
        public Learner() {}
    }

    public static class Tutor {
        public String last_name;
        public String first_name;
        public String middle_name;
        public String city;
        public String subject;
        public Tutor() {}
    }

    public static class LessonInfo {
        public String date;
        public String time;
        public LessonInfo() {}
    }

    public static class Statuses {
        public boolean tutor_ready;
        public boolean tutor_confirmed;
        public boolean parent_confirmed;
        public Statuses() {}
    }

    public static class LessonRequest {
        public int lesson_request_id;
        public Learner learner;
        public Tutor tutor;
        public LessonInfo lesson_info;
        public Statuses statuses;
        public LessonRequest() {}
    }

    public static void main(String[] args) {
        String jsonText = """
                {
                  "lesson_request_id": 1,
                  "learner": {
                    "last_name": "Ivanov",
                    "first_name": "Petr",
                    "middle_name": "Alexandrovich",
                    "grade": 8,
                    "city": "Moscow",
                    "study_goal": "Preparation for exam"
                  },
                  "tutor": {
                    "last_name": "Sidorov",
                    "first_name": "Michael",
                    "middle_name": "Ivanovich",
                    "city": "Moscow",
                    "subject": "Math"
                  },
                  "lesson_info": {
                    "date": "2025-05-20",
                    "time": "16:00"
                  },
                  "statuses": {
                    "tutor_ready": true,
                    "tutor_confirmed": true,
                    "parent_confirmed": false
                  }
                }
                """;

        Json json = new Json();

        LessonRequest request = json.fromJson(jsonText, LessonRequest.class);
        System.out.println("Learner: " + request.learner.last_name + " " + request.learner.first_name);
        System.out.println("Tutor:   " + request.tutor.last_name + " / " + request.tutor.subject);
        System.out.println("Lesson:  " + request.lesson_info.date + " " + request.lesson_info.time);
        System.out.println("Parent confirmed: " + request.statuses.parent_confirmed);

        Map<String, Object> map = json.toMap(jsonText);
        System.out.println("\nAs map: " + map);

        Json prettyJson = new Json(new JsonConfig(true, true, false));
        System.out.println("\nPretty JSON:\n" + prettyJson.toJson(request));
    }
}