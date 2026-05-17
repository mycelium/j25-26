package jsonlib;

public enum NamingPolicy {
    KEEP_ORIGINAL {
        @Override
        public String apply(String fieldName) {
            return fieldName;
        }
    },
    LOWER_CASE_WITH_UNDERSCORES {
        @Override
        public String apply(String fieldName) {
            StringBuilder result = new StringBuilder(fieldName.length() + 4);
            for (int i = 0; i < fieldName.length(); i++) {
                char current = fieldName.charAt(i);
                if (Character.isUpperCase(current)) {
                    if (i > 0) {
                        result.append('_');
                    }
                    result.append(Character.toLowerCase(current));
                } else {
                    result.append(current);
                }
            }
            return result.toString();
        }
    };

    public abstract String apply(String fieldName);
}
