import java.io.*;
import java.net.URL;
import java.util.*;


// This class will represent a document category in the form of a 'bag of words'.
// A category can be represented as a set of features, or words.
// List of stop words obtained from: http://www.lextek.com/manuals/onix/stopwords1.html
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
        if(!vocab.contains(word)) return probWordGivenClass(word);

        return wordProbs.get(word);
    }

    public void calculateWordProbabilities(){
        for (String word : vocab){
            double wordProb = probWordGivenClass(word);
            wordProbs.put(word, wordProb);
        }
    }

    public double probWordGivenClass(String word){
        double prob = (Collections.frequency(words, word) + .2) / (words.size() + Model.vocab.size());
        //System.out.println("Model.vocab.size: "+ Model.vocab.size());
        return prob;
    }

    // remove the most frequent word from vocab and words
    public void removeMostFrequent(){
        double max = Collections.max(wordProbs.values());
        String toRemove = "";
        for(String string : wordProbs.keySet()){
            if(wordProbs.get(string) == max) toRemove = string;
        }
        wordProbs.remove(toRemove);
        vocab.remove(toRemove);
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

    // a list of stop words created from stopwords.txt
    ArrayList<String> stopwords = new ArrayList<>();

    // each label will have a concatenation of all its documents
    Map<Integer, ArrayList<String>> documentMap = new HashMap<>();

    // map label to it's Category
    Map<Integer, Category> categoryMap = new HashMap<>();

    // vocabulary of all words seen in training set
    static Set<String> vocab = new HashSet<>();



    // return the label of the most probable class given list of words
    // = ArgMax( P(c|d) )
    // = ArgMax( P(d|c)P(c) )
    public int guessClassLabel(ArrayList<String> doc){

        // remove stop words from test doc
        doc = trimDocument(doc);

        // cut out half of doc to improve speed
        // note the accuracy decreases as the list is shrinked
        // dividing in half will allow for completion in ~13 min with accuracy ~0.8
        doc.subList((doc.size()/2), doc.size()-1).clear();

        int mostProbableLabel = 1;
        double currentHighestProb = probClassGivenDoc(doc, 1);
        for (int i = 1; i <= 15; i++){
            double prob = probClassGivenDoc(doc, i);
            if (prob > currentHighestProb){
                currentHighestProb = prob;
                mostProbableLabel = i;
            }
        }

        return mostProbableLabel;
    }

    // P(c|d)
    // = P(d|c)P(c)
    public double probClassGivenDoc(ArrayList<String> doc, int label){
        double prob = probDocGivenClass(doc, label) * probClass();
        return prob;
    }

    // P(d|c)
    // = P(w1|c)P(w2|c)....P(wn|c)
    public double probDocGivenClass(ArrayList<String> doc, int label) {
        double currentProb = 0.0;
        for(String word : doc){
            currentProb += Math.log(probWordGivenClass(label, word)) / Math.log(10.0);
        }

        return currentProb;
    }

    // P(c)
    public double probClass(){
        return 1.0/15.0;
    }

    // P(w|c)
    public double probWordGivenClass(int label, String word){
        return categoryMap.get(label).getProb(word);
    }

    // put words in stopwords.txt into ArrayList
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
    public void trimDocuments(){
        for(int i = 1; i <= 15; i++){
            ArrayList<String> trimmedDoc = documentMap.get(i);
            trimmedDoc.removeAll(stopwords);
            documentMap.replace(i, trimmedDoc);
        }
    }

    public ArrayList<String> trimDocument(ArrayList<String> words){
        words.removeAll(stopwords);
        return words;
    }

    public void optimize(){
        trimDocuments();

        // precalculate prob for each word in each document
        for(int i = 1; i <=15; i++){
            Category category = new Category(i);
            category.words = documentMap.get(i);
            category.vocab.addAll(documentMap.get(i));
            category.calculateWordProbabilities();

            // remove some of the most frequent words
            // handle case where there aren't enough words
            if(category.vocab.size()>20) {
                category.removeMostFrequent();
                category.removeMostFrequent();
                category.removeMostFrequent();
                category.removeMostFrequent();
                category.removeMostFrequent();
                category.removeMostFrequent();
            }

            categoryMap.put(i, category);
        }
    }

}
