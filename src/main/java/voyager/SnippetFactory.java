package voyager;

import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Stack;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;

public class SnippetFactory {
    // Wrapper for getSnippet(bodyText, query)
    public static String getSnippet(URL url, String query) {
        StringBuilder sb = new StringBuilder();

        try {

            Document doc = Jsoup.connect(url.toString()).get();
            sb.append(getSnippet(doc.body().text(), query));

        } catch (IOException e) {

            System.err.println("Jsoup couldn't connect to \"" + url + "\"");
            sb.append("A snippet couldn't be produced for the url ").append(url);
        }

        return sb.toString();
    }

    // Wrapper for getSnippet(bodyText, query)
    public static String getSnippet(File file, String query) {
        StringBuilder sb = new StringBuilder();

        try {

            Document doc = Jsoup.parse(file, null);
            sb.append(getSnippet(doc.body().text(), query));

        } catch (IOException e) {

            System.err.println("Jsoup encountered an error parsing " + file.getName());
            sb.append("A snippet couldn't be produced for the file ").append(file.getName());
        }

        return sb.toString();
    }

    public static String getSnippet(String bodyText, String query) {
        StringBuilder sb = new StringBuilder();

        String[] bodyTextWords = bodyText.split(SPLIT_REGEX);

        Set<String> queryTerms = new HashSet<String>(Arrays.asList(query.toLowerCase().split(SPLIT_REGEX)));
        queryTerms.removeAll(getStopwords());

        List<Integer> queryMatches = getQueryMatches(bodyTextWords, queryTerms);

        // If there are no query matches,
        // then generate a snippet based solely on the document
        // (not the query)
        if (queryMatches.isEmpty())
            sb.append(uninformedSnippet(bodyTextWords, 255, 30));

        else {
            // Try different numbers of clusters, 1 thru 4
            // Then, pick the best k value from the clusters generated
            List< List<int[]> > setsOfClusters = new ArrayList< List<int[]> >();
            for (int i = 1; i <= 4; i++) {

                List<int[]> clusters = getClustersKMeans(queryMatches, i);
                setsOfClusters.add(clusters);
            }
            int k = pickK(setsOfClusters);

            List<int[]> chosenCluster = setsOfClusters.get(k - 1);
            sb.append(clustersToSnippet(bodyTextWords,
                                        queryTerms,
                                        chosenCluster,
                                        255,    // Avg length of snippet
                                        30));   // Range in either direction of avg
        }

        return sb.toString();
    }

    private static String uninformedSnippet(String[] bodyTextWords,
                                            int targetCharCount, int range) {

        StringBuilder snippet = new StringBuilder();
        int charCount = 0;
        int lo = Math.max(0, targetCharCount - range);
        int hi = targetCharCount + range;
        hi -= 4;    // account for trailing "... "

        for (String word : bodyTextWords) {

            if (charCount + word.length() + 1 < hi) {

                snippet.append(word).append(" ");
                charCount += word.length() + 1;
            }

            else break;
        }

        snippet.append("... ");

        return snippet.toString();
    }

    private static String clustersToSnippet(String[] bodyTextWords, Set<String> queryTerms,
                                            List<int[]> clusters,
                                            int targetCharCount, int range) {

        StringBuilder snippet = new StringBuilder();
        snippet.append(clusters.get(0)[0] == 0 ? "" : "... ");  // Prepend ... unless clusters start at beginning
        int lo = Math.max(0, targetCharCount - range);
        int hi = targetCharCount + range;

        // Sort clusters by their density,
        // in descending order (i.e. most dense first)
        clusters.sort(new Comparator<int[]>() {
            public int compare(int[] interval1, int[] interval2) {

                double density1 = clusterDensity(bodyTextWords, queryTerms, interval1);
                double density2 = clusterDensity(bodyTextWords, queryTerms, interval2);

                return (int) Math.signum(density2 - density1);
            }
        });

        int clusterIndex = 0;

        // Prioritize the densest clusters first
        do {
            int[] currentCluster = clusters.get(clusterIndex);
            clusterIndex++;

            // Add surrounding text before the interval

            // If there's space (factoring in the interval),
            // prepend up to 7 words
            Stack<String> surroundingTextLeft = new Stack<String>();
            int leftCharCount = 0;

            // Start before the current interval, and go left,
            // pushing all eligible strings into the stack
            for (int i = currentCluster[0] - 1;
                i >= 0 && i >= currentCluster[0] - 7;
                i--) {

                int charCount = bodyTextWords[i].length();
                charCount++;    // account for space " "

                // Break out if bodyTextWords[i]
                // would be out of range,
                // i.e. if the length would be above hi
                if (snippet.length() + leftCharCount + charCount > hi)
                    break;

                surroundingTextLeft.push(bodyTextWords[i] + " ");
                leftCharCount += charCount;
            }
            // Then, pop stack onto the end of the snippet
            while (!surroundingTextLeft.isEmpty())
                snippet.append(surroundingTextLeft.pop());

            // Add the words inside the interval
            for (int i = currentCluster[0]; i <= currentCluster[1]; i++) {

                // If adding this word would bring us out of range,
                // break out
                if (snippet.length() + bodyTextWords[i].length() + 4 > hi)    // account for "... "
                    break;

                // If this word is a query term,
                // bold it in the snippet
                snippet.append(queryTerms.contains(bodyTextWords[i]) ?
                    "<b>" + bodyTextWords[i] + "</b>" :
                    bodyTextWords[i];
                snippet.append(i == currentCluster[1] ? "" : " ");  // only append spaces in the middle
            }

            // If possible, append up to 7 words after the interval
            for (int i = currentCluster[1] + 1;
                i < bodyTextWords.length && i < currentCluster[1] + 1 + 7;
                i++) {

                int charCount = bodyTextWords[i].length();
                charCount++;    // account for space " "

                if (snippet.length() + charCount > hi)
                    break;

                snippet.append(" ").append(bodyTextWords[i]);
            }

            // Separate clusters of words with ...
            snippet.append("... ");

        } while (lo > snippet.length() && clusterIndex < clusters.size());

        return snippet.toString();
    }


    private static List<int[]> getClustersKMeans(List<Integer> queryMatches, int k) {
        // Algorithm adapted from the following tutorial:
        // https://www.youtube.com/watch?v=4b5d3muPQmA

        // Each int[] is an interval of indices representing a cluster
        List<int[]> clusters = new ArrayList<int[]>();
        
        // Pick k random data points from the line
        Collections.shuffle(queryMatches);  // this way, guaranteed no duplicates
        List<Integer> centroids = new ArrayList<Integer>();
        for (int i = 0; i < Math.min(queryMatches.size(), k); i++)
            centroids.add(queryMatches.get(i)); // fetches in random order
        Collections.sort(centroids);
        Collections.sort(queryMatches); // clean up after yourself

        List<Integer> prevCentroids;

        // Recluster until centroid locations do not change
        do {

            // Record previous centroids
            prevCentroids = new ArrayList<Integer>(centroids);

            clusters.clear();
            int centroidIndex = 0;

            // pointers for defining clusters
            int a = queryMatches.get(0);    // start
            int b = a;                      // end

            // Linearly scan queryMatches.
            // For each match, find its distance to each centroid,
            // and set its value to the centroid it's closest to
            for (Integer currentQueryMatch : queryMatches) {

                // If we're on the last random point,
                // just include all remaining queryMatches
                // into this last interval
                if (centroidIndex == centroids.size() - 1) {

                    b = queryMatches.get(queryMatches.size() - 1);
                    clusters.add(new int[] {a, b});
                    break;
                }

                // If the current query match is closer to the current centroid,
                // keep a constant, and move b
                else if (Math.abs(currentQueryMatch - centroids.get(centroidIndex))
                    <=   Math.abs(currentQueryMatch - centroids.get(centroidIndex + 1))) {

                    b = currentQueryMatch;
                }

                // Else, the current query match is closer to the next random point,
                // so record this past current interval,
                // then begin a new cluster at the current query match
                else {

                    clusters.add(new int[] {a, b});
                    a = b = currentQueryMatch;

                    centroidIndex++;

                    // Edge case:
                    // if we're about to normally exit this for loop,
                    // make sure to record this newly relocated single-element interval here,
                    // otherwise it won't get added
                    if (currentQueryMatch == queryMatches.get(queryMatches.size() - 1))
                        clusters.add(new int[] {a, b});
                }
            }

            // Attempt to adjust centroids for this past iteration
            // by using the average of each interval
            centroids.clear();
            for (int[] cluster : clusters) {

                centroids.add((cluster[1] + cluster[0]) / 2);
            }

        } while (!centroids.equals(prevCentroids));

        return clusters;   
    }

    // For debug printing
    private static String clustersToString(List<int[]> clusters) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        for (int i = 0; i < clusters.size(); i++) {

            sb.append(Arrays.toString(clusters.get(i)));
            sb.append(i == clusters.size() - 1 ? "}" : ", ");
        }

        return sb.toString();
    }

    // Selects a suitable value for k (regarding k-means).
    // Because the goal is to create a snippet only 255 chars or smaller,
    // k will be in the range [1, 4].
    // setsOfClusters needs to have exactly 1 cluster
    // for each value of k in the range [1, queryMatches.size()].
    private static int pickK(List< List<int[]>> setsOfClusters) {
        Map<Integer, Integer> kToVariance = new HashMap<Integer, Integer>();

        for (List<int[]> clusters : setsOfClusters) {

            if (1 <= clusters.size() && clusters.size() <= 4) {

                kToVariance.put(clusters.size(), variance(clusters));
            }
        }
        
        int maxDiff = 0;
        int kWithMaxDiff = 1;
        for (int i = 1; i < kToVariance.size(); i++) {

            // If any of these aren't found in the map,
            // setsOfClusters wasn't properly populated
            int varCurr = kToVariance.get(i);
            int varNext = kToVariance.get(i + 1);
            int varDiff = Math.abs(varCurr - varNext);

            if (varDiff > maxDiff) {
                maxDiff = varDiff;
                kWithMaxDiff = i + 1;   // i + 1 is the elbow
            }
        }

        return kWithMaxDiff;
    }

    private static int variance(List<int[]> clusters) {
        int v = 0;

        for (int[] cluster : clusters)
            v += cluster[1] - cluster[0]; // use each cluster's span as its variance

        return v;
    }

    private static int getCharCount(String[] bodyTextWords, int[] interval) {
        int charCount = 0;

        for (int i = interval[0]; i <= interval[1]; i++)
            charCount += bodyTextWords[i].length();
        
        return charCount;
    }

    private static List<Integer> getQueryMatches(String[] bodyTextWords, Set<String> queryTerms) {
        List<Integer> occurrences = new ArrayList<Integer>();

        for (int i = 0; i < bodyTextWords.length; i++)
            if (queryTerms.contains(bodyTextWords[i].toLowerCase()))
                occurrences.add(i);
        
        return occurrences;
    }

    private static double clusterDensity(String[] bodyTextWords, Set<String> queryTerms, int[] interval) {
        int freq = 0;

        for (int i = interval[0]; i <= interval[1]; i++) {

            if (queryTerms.contains(bodyTextWords[i]))
                freq++;
        }

        return (double) freq / (interval[1] - interval[0]);
    }

    // NLTK's list of english stopwords,
    // taken from here:
    // https://gist.github.com/sebleier/554280
    private static Set<String> getStopwords() {
        return stopwords;
    }

    private static final String SPLIT_REGEX = " ";

    private static final String[] stopwordsInitializer = {
            "i",
            "me",
            "my",
            "myself",
            "we",
            "our",
            "ours",
            "ourselves",
            "you",
            "your",
            "yours",
            "yourself",
            "yourselves",
            "he",
            "him",
            "his",
            "himself",
            "she",
            "her",
            "hers",
            "herself",
            "it",
            "its",
            "itself",
            "they",
            "them",
            "their",
            "theirs",
            "themselves",
            "what",
            "which",
            "who",
            "whom",
            "this",
            "that",
            "these",
            "those",
            "am",
            "is",
            "are",
            "was",
            "were",
            "be",
            "been",
            "being",
            "have",
            "has",
            "had",
            "having",
            "do",
            "does",
            "did",
            "doing",
            "a",
            "an",
            "the",
            "and",
            "but",
            "if",
            "or",
            "because",
            "as",
            "until",
            "while",
            "of",
            "at",
            "by",
            "for",
            "with",
            "about",
            "against",
            "between",
            "into",
            "through",
            "during",
            "before",
            "after",
            "above",
            "below",
            "to",
            "from",
            "up",
            "down",
            "in",
            "out",
            "on",
            "off",
            "over",
            "under",
            "again",
            "further",
            "then",
            "once",
            "here",
            "there",
            "when",
            "where",
            "why",
            "how",
            "all",
            "any",
            "both",
            "each",
            "few",
            "more",
            "most",
            "other",
            "some",
            "such",
            "no",
            "nor",
            "not",
            "only",
            "own",
            "same",
            "so",
            "than",
            "too",
            "very",
            "s",
            "t",
            "can",
            "will",
            "just",
            "don",
            "should",
            "now"
    };

    // Please access through getStopwords(),
    // even from within SnippetFactory
    private static final Set<String> stopwords =
            new HashSet<>(Arrays.asList(stopwordsInitializer));
}
