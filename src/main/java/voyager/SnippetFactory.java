package voyager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.lang.Math;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Stack;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;

class SnippetFactory {



    public static String getSnippet(File file, String query) {

        StringBuilder sb = new StringBuilder();

        Set<String> queryTerms = new HashSet<String>(Arrays.asList(query.toLowerCase().split(SPLIT_REGEX)));

        try {

            Document doc = Jsoup.parse(file, null);

            String[] bodyTextWords = doc.body().text().split(SPLIT_REGEX);
            List<Integer> queryMatches = getQueryMatches(bodyTextWords, queryTerms);

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

        } catch (IOException e) {

            System.err.println("Jsoup encountered an error parsing " + file.getName());
            sb.append("A snippet couldn't be produced for the file " + file.getName());
        }

        return sb.toString();
    }

    private static String clustersToSnippet(String[] bodyTextWords, Set<String> queryTerms,
                                            List<int[]> clusters,
                                            int targetCharCount, int range) {

        StringBuilder snippet = new StringBuilder();
        snippet.append(clusters.get(0)[0] == 0 ? "" : "... ");  // Prepend ... unless clusters start at beginning
        int lo = targetCharCount - range;
        int hi = targetCharCount + range;

        // Sort clusters by their density,
        // in descending order (i.e. most dense first)
        Collections.sort(clusters, new Comparator<int[]>() {

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

                snippet.append(bodyTextWords[i]);
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

                snippet.append(" " + bodyTextWords[i]);
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
        for (int i = 0; i < k; i++)
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

    public static void test(String query) {

        // File dir = new File("/mnt/c/Ryan/Work/College/UCR/Spring 2020/CS_172/Projects/sample_scrape/output");
        // File[] directoryListing = dir.listFiles();

        // if (directoryListing == null) {
        //     System.err.println(dir.toString() + " not found");

        // } else {

        //     System.out.println("Query: " + query);

        //     for (File f : directoryListing) {

        //         System.out.println("File: " + f.getName());
        //         System.out.println(SnippetFactory.getSnippetTrivial(f, query) + "\n");
        //     }
        // }

        File testFile = new File("/mnt/c/Ryan/Work/College/UCR/Spring 2020/CS_172/Projects/sample_scrape/output/https___www.cs.ucr.edu_~mchow009_teaching_cs147_winter20_lab4");
        String snippet = SnippetFactory.getSnippet(testFile, query);
        System.out.println("Snippet:\n" + snippet);
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

        if (SnippetFactory.stopwords != null)
            return SnippetFactory.stopwords;

        SnippetFactory.stopwords = new HashSet<String>();

        SnippetFactory.stopwords.add("i");
        SnippetFactory.stopwords.add("me");
        SnippetFactory.stopwords.add("my");
        SnippetFactory.stopwords.add("myself");
        SnippetFactory.stopwords.add("we");
        SnippetFactory.stopwords.add("our");
        SnippetFactory.stopwords.add("ours");
        SnippetFactory.stopwords.add("ourselves");
        SnippetFactory.stopwords.add("you");
        SnippetFactory.stopwords.add("your");
        SnippetFactory.stopwords.add("yours");
        SnippetFactory.stopwords.add("yourself");
        SnippetFactory.stopwords.add("yourselves");
        SnippetFactory.stopwords.add("he");
        SnippetFactory.stopwords.add("him");
        SnippetFactory.stopwords.add("his");
        SnippetFactory.stopwords.add("himself");
        SnippetFactory.stopwords.add("she");
        SnippetFactory.stopwords.add("her");
        SnippetFactory.stopwords.add("hers");
        SnippetFactory.stopwords.add("herself");
        SnippetFactory.stopwords.add("it");
        SnippetFactory.stopwords.add("its");
        SnippetFactory.stopwords.add("itself");
        SnippetFactory.stopwords.add("they");
        SnippetFactory.stopwords.add("them");
        SnippetFactory.stopwords.add("their");
        SnippetFactory.stopwords.add("theirs");
        SnippetFactory.stopwords.add("themselves");
        SnippetFactory.stopwords.add("what");
        SnippetFactory.stopwords.add("which");
        SnippetFactory.stopwords.add("who");
        SnippetFactory.stopwords.add("whom");
        SnippetFactory.stopwords.add("this");
        SnippetFactory.stopwords.add("that");
        SnippetFactory.stopwords.add("these");
        SnippetFactory.stopwords.add("those");
        SnippetFactory.stopwords.add("am");
        SnippetFactory.stopwords.add("is");
        SnippetFactory.stopwords.add("are");
        SnippetFactory.stopwords.add("was");
        SnippetFactory.stopwords.add("were");
        SnippetFactory.stopwords.add("be");
        SnippetFactory.stopwords.add("been");
        SnippetFactory.stopwords.add("being");
        SnippetFactory.stopwords.add("have");
        SnippetFactory.stopwords.add("has");
        SnippetFactory.stopwords.add("had");
        SnippetFactory.stopwords.add("having");
        SnippetFactory.stopwords.add("do");
        SnippetFactory.stopwords.add("does");
        SnippetFactory.stopwords.add("did");
        SnippetFactory.stopwords.add("doing");
        SnippetFactory.stopwords.add("a");
        SnippetFactory.stopwords.add("an");
        SnippetFactory.stopwords.add("the");
        SnippetFactory.stopwords.add("and");
        SnippetFactory.stopwords.add("but");
        SnippetFactory.stopwords.add("if");
        SnippetFactory.stopwords.add("or");
        SnippetFactory.stopwords.add("because");
        SnippetFactory.stopwords.add("as");
        SnippetFactory.stopwords.add("until");
        SnippetFactory.stopwords.add("while");
        SnippetFactory.stopwords.add("of");
        SnippetFactory.stopwords.add("at");
        SnippetFactory.stopwords.add("by");
        SnippetFactory.stopwords.add("for");
        SnippetFactory.stopwords.add("with");
        SnippetFactory.stopwords.add("about");
        SnippetFactory.stopwords.add("against");
        SnippetFactory.stopwords.add("between");
        SnippetFactory.stopwords.add("into");
        SnippetFactory.stopwords.add("through");
        SnippetFactory.stopwords.add("during");
        SnippetFactory.stopwords.add("before");
        SnippetFactory.stopwords.add("after");
        SnippetFactory.stopwords.add("above");
        SnippetFactory.stopwords.add("below");
        SnippetFactory.stopwords.add("to");
        SnippetFactory.stopwords.add("from");
        SnippetFactory.stopwords.add("up");
        SnippetFactory.stopwords.add("down");
        SnippetFactory.stopwords.add("in");
        SnippetFactory.stopwords.add("out");
        SnippetFactory.stopwords.add("on");
        SnippetFactory.stopwords.add("off");
        SnippetFactory.stopwords.add("over");
        SnippetFactory.stopwords.add("under");
        SnippetFactory.stopwords.add("again");
        SnippetFactory.stopwords.add("further");
        SnippetFactory.stopwords.add("then");
        SnippetFactory.stopwords.add("once");
        SnippetFactory.stopwords.add("here");
        SnippetFactory.stopwords.add("there");
        SnippetFactory.stopwords.add("when");
        SnippetFactory.stopwords.add("where");
        SnippetFactory.stopwords.add("why");
        SnippetFactory.stopwords.add("how");
        SnippetFactory.stopwords.add("all");
        SnippetFactory.stopwords.add("any");
        SnippetFactory.stopwords.add("both");
        SnippetFactory.stopwords.add("each");
        SnippetFactory.stopwords.add("few");
        SnippetFactory.stopwords.add("more");
        SnippetFactory.stopwords.add("most");
        SnippetFactory.stopwords.add("other");
        SnippetFactory.stopwords.add("some");
        SnippetFactory.stopwords.add("such");
        SnippetFactory.stopwords.add("no");
        SnippetFactory.stopwords.add("nor");
        SnippetFactory.stopwords.add("not");
        SnippetFactory.stopwords.add("only");
        SnippetFactory.stopwords.add("own");
        SnippetFactory.stopwords.add("same");
        SnippetFactory.stopwords.add("so");
        SnippetFactory.stopwords.add("than");
        SnippetFactory.stopwords.add("too");
        SnippetFactory.stopwords.add("very");
        SnippetFactory.stopwords.add("s");
        SnippetFactory.stopwords.add("t");
        SnippetFactory.stopwords.add("can");
        SnippetFactory.stopwords.add("will");
        SnippetFactory.stopwords.add("just");
        SnippetFactory.stopwords.add("don");
        SnippetFactory.stopwords.add("should");
        SnippetFactory.stopwords.add("now");

        return SnippetFactory.stopwords;
    }

    private static final String SPLIT_REGEX = " ";

    // Please access through getStopwords(),
    // even from within SnippetFactory
    private static Set<String> stopwords = null;
}