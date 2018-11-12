import java.io.*;
import java.net.URL;
import java.util.*;


// This class will represent a document category in the form of a 'bag of words'.
// A category can be represented as a set of features, or words.
class Category{
    Category(int label){
        this.classNumber = label;
    }

    // map the word to its number of occurrences
    ArrayList<String> words = new ArrayList<>();
    Set<String> vocab = new HashSet<>();
    int classNumber;
    Map<String, Double> wordProbs = new HashMap<>();

    public double getProb(String word){
        // if word isn't in vocab, return 0
        if(!vocab.contains(word)) return 0.0;

        return wordProbs.get(word);
    }

    public void calculateWordProbabilities(){
        for (String word : vocab){
            double wordProb = probWordGivenClass(word);
            wordProbs.put(word, wordProb);
        }
    }

    public double probWordGivenClass(String word){
        return (Collections.frequency(words, word) + 1) / Math.pow(words.size() + vocab.size(), 1);
    }
}

// Represents the Model for program
class Model {

    // initialize labels 1-15
    Model() {
        for (int i = 1; i <= 15; i++){ documentMap.put(i, new ArrayList<>()); }

        // build list of stopwords from stopwords.txt (found on google)
        buildStopwordList();
    }

    ArrayList<String> stopwords = new ArrayList<>();

    // store the label of each document in here when seen
    ArrayList<Integer> documentLabelsSeen = new ArrayList<>();

    // each label will have a concatenation of all its documents
    Map<Integer, ArrayList<String>> documentMap = new HashMap<>();

    Map<Integer, Category> categoryMap = new HashMap<>();

    Set<String> vocab = new HashSet<>();



    // return the label of the most probable class given list of words
    // = ArgMax( P(c|d) )
    // = ArgMax( P(d|c)P(c) )
    public int guessClassLabel(ArrayList<String> doc){

        int mostProbableLabel = 1;
        double currentHighestProb = probClassGivenDoc(doc, 1);
        for (int i = 1; i <= 15; i++){
            double prob = probClassGivenDoc(doc, i);
            if (prob > currentHighestProb){
                currentHighestProb = prob;
                mostProbableLabel = i;
            }
            //System.out.println("probability of class " + i +" is: " + prob);
        }

        return mostProbableLabel;
    }

    // P(c|d)
    // = P(d|c)P(c)
    public double probClassGivenDoc(ArrayList<String> doc, int label){
        //System.out.println("probability of class " + label + " is: " + probClass(label));
        return probDocGivenClass(doc, label) * probClass(label);
    }

    // P(d|c)
    // = P(w1|c)P(w2|c)....P(wn|c)
    public double probDocGivenClass(ArrayList<String> doc, int label) {
        double currentProb = 0.0;
        for(String word : doc){
            currentProb += Math.log(probWordGivenClass(label, word)) / Math.log(10.0);
            //currentProb += probWordGivenClass(label, word);
            //System.out.println("prob: " + Math.log(probWordGivenClass(label, word)) / Math.log(1000000));
            //System.out.println("prob: " + currentProb);
        }

        return currentProb;
    }

    // P(c)
    public double probClass(int label){
        //System.out.println(Collections.frequency(documentLabelsSeen, label));
        //System.out.println(documentLabelsSeen.size());
        double freq = (double) Collections.frequency(documentLabelsSeen, label);
        double totalSize = documentLabelsSeen.size();
        return freq/totalSize;
    }

    // P(w|c)
    public double probWordGivenClass(int label, String word){
        //ArrayList<String> document = documentMap.get(label);
        //return (Collections.frequency(document, word) + 1) / (document.size() + vocab.size());
        //System.out.println((Collections.frequency(document, word) + 1) / Math.pow(document.size() + vocab.size(), 1));
        //return (Collections.frequency(document, word) + 1) / Math.pow(document.size() + vocab.size(), 1);
        return categoryMap.get(label).getProb(word);
    }

    private void buildStopwordList(){
        URL url = getClass().getResource("stopwords.txt");
        File file = new File(url.getPath());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                stopwords.add(line);
            }
        } catch (IOException ioException) {
            System.err.println("Cannot open training file");
            System.exit(1);
        }
    }

    // remove stopwords from all the documents
    // How about remove words present in every document as well?
    public void trimDocuments(){
        for(int i = 1; i <= 15; i++){
            ArrayList<String> trimmedDoc = documentMap.get(i);
            for(String word : stopwords){
                trimmedDoc.remove(word);
            }
            documentMap.replace(i, trimmedDoc);
        }
    }

    public void optimize(){
        trimDocuments();

        // precalculate prob for each word in each document
        for(int i = 1; i <=15; i++){
            Category category = new Category(i);
            category.words = documentMap.get(i);
            category.vocab.addAll(documentMap.get(i));
            category.calculateWordProbabilities();
            categoryMap.put(i, category);
        }

    }

    public int vocabSize(){ return vocab.size(); }

    // update the frequency of the word for the category
    public void updateWord(int label, String word){

    }
}
