import tokenizer.*;

public static void main(String[] args) {

    String[] tests = {
            "{}",

            "{\"a\":1}",

            "{ \"a\" : 1 }",

            """
            {
              "name": "Ivan",
              "age": 20
            }
            """,

            """
            {
              "active": true,
              "deleted": false,
              "history": null
            }
            """,

            """
            [1,2,3]
            """,

            """
            [1, "text", true, false, null]
            """,

            """
            {
              "skills": ["java", "go", "react"]
            }
            """,

            """
            {
              "user": {
                "name": "Ivan",
                "age": 20.2
              }
            }
            """,

            """
            {
              "text": "hello\\nworld"
            }
            """
    };

    for (String json : tests) {
        System.out.println("====== NEW TEST ======");

        Tokenizer tokenizer = new Tokenizer(json);
        Tokenizer tokenizerMap = new Tokenizer(json);

        System.out.println(json);
        System.out.println(tokenizer.getJSON());

//        Token token;
//        do {
//            token = tokenizer.nextToken();
//            System.out.println(token.getType() + " : " + token.getValue());
//        } while (token.getType() != TokenType.EOF);

        try {
            Object result = tokenizer.parseJSON();

            System.out.println("PARSED:");
            System.out.println(result);
            System.out.println(tokenizerMap.parseJSONToMap());

            System.out.println("TYPE:");
            System.out.println(result.getClass().getName());

            User user = JsonMapper.fromMap(map, User.class);


        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        System.out.println();


    }
}

