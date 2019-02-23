package at.ac.tuwien.touristguide.tools;


import android.app.Activity;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.ac.tuwien.touristguide.R;
import at.ac.tuwien.touristguide.entities.Poi;
import at.ac.tuwien.touristguide.entities.Section;
import at.ac.tuwien.touristguide.summarizer.SimpleSummariser;


/**
 * @author Manu Weilharter
 * prepares the text of the POI for the TextView
 */
public class NLPHelper {

    public static final List<String> ABBREVATIONS = Arrays.asList("hl", "k.u.k", "k.k", "k. k", "K.k", "vgl", " op", "usw", " z", "B", "z.b", "z.B", "bzw", "usw", "cf", "etc", "Dr", "Mag", "Dipl", "Ing", "techn");

    private Activity activity;

    public NLPHelper(Activity activity) {
        this.activity = activity;
    }

    public String structureTextForView(List<Section> sections, int infoLevel) {
        String result = "";

        for (Section sec : sections) {
            if (infoLevel != 2) {
                if (sec.getCategory().equals("Architektur") | sec.getCategory().equals("Architecture")) {
                    result += "<h3><font color='#32CD32'>| </font> " + sec.getHeader() + "</h3>";
                } else if (sec.getCategory().equals("Geschichte") | sec.getCategory().equals("History")) {
                    result += "<h3><font color='#8A2BE2'>| </font> " + sec.getHeader() + "</h3>";
                } else if (sec.getCategory().equals("Sport") | sec.getCategory().equals("Sports")) {
                    result += "<h3><font color='#7FFFD4'>| </font> " + sec.getHeader() + "</h3>";
                } else if (sec.getCategory().equals("Geografie") | sec.getCategory().equals("Geographie")) {
                    result += "<h3><font color='#FFA07A'>| </font> " + sec.getHeader() + "</h3>";
                } else {
                    result += "<h3>" + sec.getHeader() + "</h3>";
                }
            }

            result += sec.getContent()
                    .replace("<h3>", "<h4>").replace("</h3>", "</h4>")
                    .replace("<h4>", "<h5>").replace("</h4>", "</h5>")
                    .replace("<h5>", "<h6>").replace("</h5>", "</h6>");
            result = result.replace(">§", ">");
            result = result.replaceAll("[\\d]+!", "");
        }

        return result;
    }


    public String[] structureTextForTTS(Poi poi, List<Section> sections, boolean guider, int infoLevel) {

        ArrayList<String> resultArray = new ArrayList<>();

        if (guider) {
            resultArray.add(poi.getName() + " " + activity.getString(R.string.gdf8) + " "
                    + (int) poi.getDistance() + " " + activity.getString(R.string.gdf9));
        } else {
            resultArray.add(poi.getName());
        }

        for (Section sec : sections) {
            if (infoLevel != 2) {
                resultArray.add(sec.getHeader());
            }

            Document doc = Jsoup.parse(sec.getContent());
            String[] sectionText = getSentences(doc.text());

            for (String sentence : sectionText) {
                resultArray.add(sentence.trim());
            }
        }

        String[] result = new String[resultArray.size()];

        return resultArray.toArray(result);
    }


    private String[] getSentences(String text) {
        // remove html tags not supported by edittext
        text = text.replace("<p>", "").replace("</p>", "");
        text = text.replace("<ul>", "").replace("</ul>", "");
        text = text.replace("<ol>", "").replace("</ol>", "");
        text = text.replace("<li>", "").replace("</li>", "");

        text = text.replaceAll("[\\d]+!", "");

        String pattern = "\\. ";
        String[] splitText = text.split(pattern);

        List<String> result = new ArrayList<>();

        if (splitText.length == 1) {
            if (splitText[0].endsWith(".")) {
                splitText[0] = splitText[0].substring(0, splitText[0].length() - 1);
            }
            return splitText;
        }

        boolean newSentence = true;
        String sentence = "";

        for (int i = 0; i < splitText.length; i++) {
            if (newSentence) {
                sentence = "";
            }

            splitText[i] = splitText[i].trim();

            try {
                String lastWord = splitText[i].substring(splitText[i].lastIndexOf(" ") + 1);
                Integer.parseInt(lastWord);
                sentence += splitText[i] + ". ";
                newSentence = false;
                continue;
            } catch (Exception e) {
                Log.e("NLPHelper", e.toString());
            }

            boolean abbrFound = false;
            for (String abbr : ABBREVATIONS) {
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
                if (splitText[i].endsWith(".")) {
                    splitText[i] = splitText[i].substring(0, splitText[i].length() - 1);
                }
            }

            if (newSentence) {
                result.add(sentence += splitText[i]);
            }
        }

        String[] result_array = new String[result.size()];

        for (int i = 0; i < result.size(); i++) {
            result_array[i] = result.get(i).replace("..", ".");
        }

        return result_array;
    }


    public List<Section> summarizeSections(List<Section> sections) {
        SimpleSummariser summariser = new SimpleSummariser();

        for (Section sec : sections) {
            Document doc = Jsoup.parse(sec.getContent());

            // the heading tags are not necessary in the summarization - removing them
            Elements h4 = doc.select("h3, h4, h5, h6, h7, h9");

            for (Element el : h4) {
                el.remove();
            }

            sec.setContent(summariser.summarise(doc.text(), 5));
        }

        return sections;
    }


    public int getSectionBeginning(List<Section> sections, int counter, String[] splitspeech, boolean back) {
        int sectionFound = 0;
        int result = -1;

        if (sections == null) {
            return -1;
        }

        for (int i = 0; i < sections.size(); i++) {
            Section sec = sections.get(i);
            if (sec.getHeader().contains(splitspeech[counter]) || Jsoup.parse(sec.getContent()).text().contains(splitspeech[counter])) {
                sectionFound = sections.indexOf(sec);
                break;
            }
        }

        // jump to next/last section
        if (back) {
            sectionFound -= 1;
        } else {
            sectionFound += 1;
        }

        try {
            for (int i = 0; i < splitspeech.length; i++) {
                String part = splitspeech[i];
                if (part.equals(sections.get(sectionFound).getHeader())) {
                    result = i;
                    break;
                }
            }
        } catch (Exception e) {
            return result;
        }

        return result;
    }

    public String replacementsForTTS(String sentence) {
        sentence = sentence.replace("->", "")
                .replace("K.k", "k und k")
                .replace("k.k.", "k und k")
                .replace("k. k.", "k und k")
                .replace("vgl.", "vergleiche")
                .replace("z.B", "zum Beispiel")
                .replace("z. B.", "zum Beispiel")
                .replace("zB", "zum Beispiel")
                .replace("op.", "Opus")
                .replace("d. h.", "das heisst")
                .replace("d.h.", "das heisst")
                .replace("etc.", "et cetera")
                .replace("bzw.", "beziehungsweise")
                .replace("usw.", "und so weiter")
                .replace("Dr.", "Doktor")
                .replace("Mag.", "Magister")
                .replace("s.a.", "siehe auch")
                .replace("s. a.", "siehe auch")
                .replace("dem hl.", "dem heiligen")
                .replace("den hl.", "den heiligen")
                .replace("die hl.", "die heilige")
                .replace("der hl.", "der heiligen")
                .replace("das hl.", "das heilige");

        return sentence;
    }

}
