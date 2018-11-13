import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
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

    // used to print double values in better format
    static DecimalFormat decFormat = new DecimalFormat("#0.000");

    static long timeTraining;
    static long timeLabeling;
    static double accTraining;
    static double accTesting;


    public static void main(String[] args) {

        if(args.length != 2) {
            System.out.println("Two files needed: input, output");
            System.exit(1);
        }

        // train model, time how long it takes
        File trainingFile = new File(args[0]);
        long startTime = System.currentTimeMillis();
        trainModel(trainingFile);
        long endTime = System.currentTimeMillis();
        timeTraining = (endTime-startTime)/1000;

        // evaluate model on the training file
        testOnTrainingFile(trainingFile);

        // evaluate model on test file, time how long it takes
        startTime = System.currentTimeMillis();
        File testFile = new File(args[1]);
        testModel(testFile);
        endTime = System.currentTimeMillis();
        timeLabeling = (endTime-startTime)/1000;

        // print results
        printResults();
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

        model.optimize();
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

        // only consider 1/3 of the document to speed up training.
        ArrayList<String> splitDoc = new ArrayList<>();
        for (int i = 0; i<words.length/3; i++){
            splitDoc.add(words[i]);
        }

        model.documentMap.get(label).addAll(splitDoc);

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

        accTesting = (double) numTestCorrect/numTestLines;
    }

    static int numTestLines = 0;
    static int numTestCorrect = 0;
    // process each document in test file and print the guessed label
    private static void processTestLine(String line){
        numTestLines++;
        String[] parsedLine = splitLine(line);
        String[] words = parsedLine[0].split("\\s+");
        int label = Integer.parseInt(parsedLine[1]);

        ArrayList<String> wordsList = new ArrayList<>(Arrays.asList(words));
        int guessedLabel = model.guessClassLabel(wordsList);
        if(label == guessedLabel) numTestCorrect++;
        System.out.println(guessedLabel);
    }

    // run model on training file
    static int numTrainingLines = 0;
    static int numCorrect = 0;
    private static void testOnTrainingFile(File file){
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                processTrainingTestLine(line);
            }
        } catch (IOException ioException) {
            System.err.println("Cannot open test file.");
            System.exit(1);
        }

        accTraining = (double) numCorrect/numTrainingLines;
    }

    private static void processTrainingTestLine(String line){
        numTrainingLines++;
        String[] parsedLine = splitLine(line);
        String[] words = parsedLine[0].split("\\s+");
        int label = Integer.parseInt(parsedLine[1]);
        if(model.guessClassLabel(new ArrayList<>(Arrays.asList(words))) == label)
            numCorrect++;
    }

    private static void printResults(){
        System.out.println(timeTraining + " seconds (training)");
        System.out.println(timeLabeling + " seconds (labeling)");
        System.out.println(decFormat.format(accTraining) + " (training)");
        System.out.println(decFormat.format(accTesting) + " (testing)");
    }
}
