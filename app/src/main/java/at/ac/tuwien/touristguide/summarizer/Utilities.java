/*
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 Nick Lothian. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        developers of Classifier4J (http://classifier4j.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The name "Classifier4J" must not be used to endorse or promote 
 *    products derived from this software without prior written 
 *    permission. For written permission, please contact   
 *    http://sourceforge.net/users/nicklothian/.
 *
 * 5. Products derived from this software may not be called 
 *    "Classifier4J", nor may "Classifier4J" appear in their names 
 *    without prior written permission. For written permission, please 
 *    contact http://sourceforge.net/users/nicklothian/.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

package at.ac.tuwien.touristguide.summarizer;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import at.ac.tuwien.touristguide.tools.NLPHelper;

/**
 * @author Nick Lothian
 * @author Peter Leschev
 * @author Manu Weilharter: modified this class with an own sentence separation algorithm
 */
@SuppressLint({"UseValueOf", "DefaultLocale"})
@SuppressWarnings({"rawtypes", "unchecked"})
public class Utilities {

    public static Map getWordFrequency(String input) {
        return getWordFrequency(input, false);
    }

    public static Map getWordFrequency(String input, boolean caseSensitive) {
        return getWordFrequency(input, caseSensitive, new DefaultTokenizer(), new DefaultStopWordsProvider());
    }

    /**
     * Get a Map of words and Integer representing the number of each word
     *
     * @param input             The String to get the word frequency of
     * @param caseSensitive     true if words should be treated as separate if they have different case
     * @param tokenizer         a junit.framework.TestCase#run()
     * @param stopWordsProvider
     * @return
     */

    public static Map getWordFrequency(String input, boolean caseSensitive, DefaultTokenizer tokenizer, IStopWordProvider stopWordsProvider) {
        String convertedInput = input;
        if (!caseSensitive) {
            convertedInput = input.toLowerCase(Locale.getDefault());
        }

        // tokenize into an array of words
        String[] words = tokenizer.tokenize(convertedInput);
        Arrays.sort(words);

        String[] uniqueWords = getUniqueWords(words);

        Map result = new HashMap();
        for (int i = 0; i < uniqueWords.length; i++) {
            if (stopWordsProvider == null) {
                // no stop word provider, so add all words
                result.put(uniqueWords[i], new Integer(countWords(uniqueWords[i], words)));
            } else if (isWord(uniqueWords[i]) && !stopWordsProvider.isStopWord(uniqueWords[i])) {
                // add only words that are not stop words
                result.put(uniqueWords[i], new Integer(countWords(uniqueWords[i], words)));
            }
        }

        return result;
    }

    private static String[] findWordsWithFrequency(Map wordFrequencies, Integer frequency) {
        if (wordFrequencies == null || frequency == null) {
            return new String[0];
        } else {
            List results = new ArrayList();
            Iterator it = wordFrequencies.keySet().iterator();

            while (it.hasNext()) {
                String word = (String) it.next();
                if (frequency.equals(wordFrequencies.get(word))) {
                    results.add(word);
                }
            }

            return (String[]) results.toArray(new String[results.size()]);

        }
    }

    public static Set getMostFrequentWords(int count, Map wordFrequencies) {
        Set result = new LinkedHashSet();

        Integer max = (Integer) Collections.max(wordFrequencies.values());

        int freq = max.intValue();
        while (result.size() < count && freq > 0) {
            // this is very icky
            String words[] = findWordsWithFrequency(wordFrequencies, new Integer(freq));
            result.addAll(Arrays.asList(words));
            freq--;
        }

        return result;
    }


    private static boolean isWord(String word) {
        if (word != null && !word.trim().equals("")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Find all unique words in an array of words
     *
     * @param input an array of Strings
     * @return an array of all unique strings. Order is not guarenteed
     */
    public static String[] getUniqueWords(String[] input) {
        if (input == null) {
            return new String[0];
        } else {
            Set result = new TreeSet();
            for (int i = 0; i < input.length; i++) {
                result.add(input[i]);
            }
            return (String[]) result.toArray(new String[result.size()]);
        }
    }

    /**
     * Count how many times a word appears in an array of words
     *
     * @param word  The word to count
     * @param words non-null array of words
     */
    public static int countWords(String word, String[] words) {
        // find the index of one of the items in the array.
        // From the JDK docs on binarySearch:
        // If the array contains multiple elements equal to the specified object, there is no guarantee which one will be found.
        int itemIndex = Arrays.binarySearch(words, word);

        // iterate backwards until we find the first match
        if (itemIndex > 0) {
            while (itemIndex > 0 && words[itemIndex].equals(word)) {
                itemIndex--;
            }
        }

        // now itemIndex is one item before the start of the words
        int count = 0;
        while (itemIndex < words.length && itemIndex >= 0) {
            if (words[itemIndex].equals(word)) {
                count++;
            }

            itemIndex++;
            if (itemIndex < words.length) {
                if (!words[itemIndex].equals(word)) {
                    break;
                }
            }
        }

        return count;
    }

    /**
     * @param input a String which may contain many sentences
     * @return an array of Strings, each element containing a sentence
     */
    public static String[] getSentences(String text) {
        // remove html tags not supported by edittext
        text = text.replace("<p>", "").replace("</p>", "");
        text = text.replace("<ul>", "").replace("</ul>", "");
        text = text.replace("<ol>", "").replace("</ol>", "");
        text = text.replace("<li>", "").replace("</li>", "");

        text = text.replaceAll("[\\d]+!", "");

        String pattern = "\\. ";
        String[] splitText = text.split(pattern);

        List<String> result = new ArrayList<String>();

        if (splitText.length == 1) {
            if (splitText[0].endsWith("."))
                splitText[0] = splitText[0].substring(0, splitText[0].length() - 1);

            return splitText;
        }

        boolean newSentence = true;
        String sentence = "";

        for (int i = 0; i < splitText.length; i++) {
            if (newSentence)
                sentence = "";

            splitText[i] = splitText[i].trim();

            try {
                String lastWord = splitText[i].substring(splitText[i].lastIndexOf(" ") + 1);
                Integer.parseInt(lastWord);
                sentence += splitText[i] + ". ";
                newSentence = false;
                continue;
            } catch (Exception e) {
            }

            boolean abbrFound = false;
            for (String abbr : NLPHelper.ABBREVATIONS) {
                if (splitText[i].endsWith(abbr)) {
                    sentence += splitText[i] + ". ";
                    newSentence = false;
                    abbrFound = true;
                    break;
                } else {
                    newSentence = true;
                }
            }

            if (!abbrFound) {
                if (i < splitText.length - 1) {
                    char c = splitText[i + 1].charAt(0);
                    if (Character.isLowerCase(c)) {
                        sentence += splitText[i] + ". ";
                        newSentence = false;
                    } else if (splitText[i + 1].split(" ", 2)[0].equals("Jahrhunderts") || splitText[i + 1].split(" ", 2)[0].equals("Jahrhundert")) {
                        sentence += splitText[i] + ". ";
                        newSentence = false;
                    }
                }
            }

            if (i == splitText.length - 1) {
                if (splitText[i].endsWith("."))
                    splitText[i] = splitText[i].substring(0, splitText[i].length() - 1);
            }

            if (newSentence)
                result.add(sentence += splitText[i]);
        }

        String[] result_array = new String[result.size()];

        for (int i = 0; i < result.size(); i++) {
            result_array[i] = result.get(i).replace("..", ".");
        }

        return result_array;
    }

    /**
     * Given an inputStream, this method returns a String. New lines are
     * replaced with " "
     */
    public static String getString(InputStream is) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = "";
        StringBuffer stringBuffer = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            stringBuffer.append(line);
            stringBuffer.append(" ");
        }

        reader.close();

        return stringBuffer.toString().trim();
    }
}
