JCC = javac

JFLAGS = -g

default: Document.class NaiveBayesClassifier.class

NaiveBayesClassifier.class: NaiveBayesClassifier.java
	$(JCC) $(JFLAGS) NaiveBayesClassifier.java

Document.class: Document.java
	$(JCC) $(JFLAGS) Document.java

clean: 
	$(RM) *.class

