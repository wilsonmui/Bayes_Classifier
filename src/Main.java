import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    /*
        Take in two files as input: training.txt and testing.txt

        Calculate P(class | document) for every possible class. Return the class that is most probable.

        Method:

        for document in training file
            process document to train model

     */

    // Contains all the words found in training documents.
    static Model model = new Model();

    public static void main(String[] args) {

        if(args.length != 2) {
            System.out.println("Two files needed: input, output");
            System.exit(1);
        }

        // Read training file
        File trainingFile = new File(args[0]);
        long startTime = System.currentTimeMillis();
        trainModel(trainingFile);
        long endTime = System.currentTimeMillis();
        System.out.println("That took " + (endTime - startTime) + " milliseconds");

        // Read test file
        File testFile = new File(args[1]);
        testModel(testFile);

    }

    // load training file to train model
    private static void trainModel(File file){

        // process each line
        // build model.vocab, model.documentMap, model.documentLabelSeen
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                processTrainingLine(line);
            }
        } catch (IOException ioException) {
            System.err.println("Cannot open training file");
            System.exit(1);
        }

        System.out.println("Size of document for class 1: " + model.documentMap.get(1).size());
        model.optimize();
        System.out.println("Size for class 1 post-trim: " + model.documentMap.get(1).size());

    }

    // returns string[], where [0] is the words and [1] is the class
    private static String[] splitLine(String line) {
        String splitLine[] = line.split(",");
        return splitLine;
    }

    // process line by building model
    private static void processTrainingLine(String line) {
        String[] parsedLine = splitLine(line);
        String[] words = parsedLine[0].split("\\s+");
        int label = Integer.parseInt(parsedLine[1]);

        // add the words to the corresponding label
        ArrayList<String> updatedWords = model.documentMap.get(label);
        updatedWords.addAll(Arrays.asList(words));
        model.documentMap.replace(label, updatedWords);
        model.documentLabelsSeen.add(label);

        // add any new words into model.vocab
        model.vocab.addAll(Arrays.asList(words));
    }

    // pass in test file to be used to test model
    private static void testModel(File testFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(testFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                processTestLine(line);
            }
        } catch (IOException ioException) {
            System.err.println("Cannot open test file.");
            System.exit(1);
        }
    }

    private static void processTestLine(String line){
        String[] parsedLine = splitLine(line);
        String[] words = parsedLine[0].split("\\s+");
        int label = Integer.parseInt(parsedLine[1]);

        ArrayList<String> wordsList = new ArrayList<>(Arrays.asList(words));
        int guessedLabel = model.guessClassLabel(wordsList);
        System.out.println("Expected label: " + label);
        System.out.println("Guessed label: " + guessedLabel);
    }

}
