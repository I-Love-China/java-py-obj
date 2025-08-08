package com.github.typist;

public class App {
    public static void main(String[] args) {
        PythonObjectParser parser = new PythonObjectParser();
        
        if (args.length > 0) {
            try {
                String json = parser.parseToJson(args[0]);
                System.out.println(json);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }
        } else {
            System.out.println("Usage: java -jar java-py-obj.jar \"<python_object_string>\"");
            System.out.println("Examples:");
            System.out.println("  java -jar java-py-obj.jar \"[1, 2, 3]\"");
            System.out.println("  java -jar java-py-obj.jar \"{'name': 'John', 'age': 30}\"");
        }
    }
}
