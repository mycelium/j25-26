package jsonlib;


public enum FieldNamingStrategy {

    /** No translation — JSON key == Java field name (default). */
    IDENTITY {
        @Override public String toJsonKey(String fieldName)  { return fieldName; }
        @Override public String toFieldName(String jsonKey)  { return jsonKey;   }
    },

   
    SNAKE_CASE {
        @Override
        public String toJsonKey(String fieldName) {
            StringBuilder sb = new StringBuilder();
            for (char c : fieldName.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    sb.append('_').append(Character.toLowerCase(c));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        @Override
        public String toFieldName(String jsonKey) {
            StringBuilder sb = new StringBuilder();
            boolean nextUpper = false;
            for (char c : jsonKey.toCharArray()) {
                if (c == '_') {
                    nextUpper = true;
                } else {
                    sb.append(nextUpper ? Character.toUpperCase(c) : c);
                    nextUpper = false;
                }
            }
            return sb.toString();
        }
    },

    
    UPPER_SNAKE_CASE {
        @Override
        public String toJsonKey(String fieldName) {
            return SNAKE_CASE.toJsonKey(fieldName).toUpperCase();
        }

        @Override
        public String toFieldName(String jsonKey) {
            return SNAKE_CASE.toFieldName(jsonKey.toLowerCase());
        }
    };

    /**
     * Translate a Java field name to the JSON object key used during
     * serialization.
     */
    public abstract String toJsonKey(String fieldName);

    /**
     * Translate a JSON object key back to the Java field name used during
     * deserialization.
     */
    public abstract String toFieldName(String jsonKey);
}
