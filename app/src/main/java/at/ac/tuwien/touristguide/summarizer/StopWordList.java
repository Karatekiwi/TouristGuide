package at.ac.tuwien.touristguide.summarizer;

public class StopWordList {

    public static String[] stopWords = {"a", "a's", "able", "about", "above", "according", "accordingly", "across", "actually", "after",
            "afterwards", "again", "against", "ain't", "all", "allow", "allows", "almost", "alone", "along", "already", "als", "also", "although",
            "always", "am", "am", "among", "amongst", "an", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway",
            "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "aren't", "around", "as", "aside", "ask", "asking",
            "associated", "at", "auch", "auf", "aus", "available", "away", "awfully", "b", "be", "became", "because", "become", "becomes",
            "becoming", "been", "before", "beforehand", "behind", "bei", "being", "believe", "below", "beside", "besides", "best", "better",
            "between", "beyond", "bin", "bis", "bist", "both", "brief", "but", "by", "c", "c'mon", "c's", "came", "can", "can't", "cannot",
            "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently",
            "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldn't", "course", "currently", "d",
            "da", "dadurch", "daher", "darum", "das", "dass", "da&szlig;", "definitely", "dein", "deine", "dem", "den", "der", "des", "described",
            "deshalb", "despite", "dessen", "did", "didn't", "die", "dies", "dieser", "dieses", "different", "do", "doch", "does", "doesn't",
            "doing", "don't", "done", "dort", "down", "downwards", "du", "durch", "during", "e", "each", "edu", "eg", "eight", "ein", "eine",
            "einem", "einen", "einer", "eines", "either", "else", "elsewhere", "enough", "entirely", "er", "es", "especially", "et", "etc",
            "euer", "eure", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example",
            "except", "f", "far", "few", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth",
            "four", "from", "further", "furthermore", "f&uuml;r", "g", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone",
            "got", "gotten", "greetings", "h", "had", "hadn't", "happens", "hardly", "has", "hasn't", "hatte", "hatten", "hattest", "hattet",
            "have", "haven't", "having", "he", "he's", "hello", "help", "hence", "her", "here", "here's", "hereafter", "hereby", "herein",
            "hereupon", "hers", "herself", "hi", "hier", "him", "himself", "hinter", "his", "hither", "hopefully", "how", "howbeit", "however",
            "i", "i'd", "i'll", "i'm", "i've", "ich", "ie", "if", "ignored", "ihr", "ihre", "im", "immediate", "in", "in", "inasmuch", "inc",
            "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isn't", "ist", "it", "it'd",
            "it'll", "it's", "its", "itself", "j", "ja", "jede", "jedem", "jeden", "jeder", "jedes", "jener", "jenes", "jetzt", "just", "k",
            "kann", "kannst", "keep", "keeps", "kept", "know", "known", "knows", "k&ouml;nnen", "k&ouml;nnt", "l", "last", "lately", "later", "latter",
            "latterly", "least", "less", "lest", "let", "let's", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "m",
            "machen", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "mein", "meine", "merely", "might", "mit", "more", "moreover",
            "most", "mostly", "much", "musst", "must", "mu&szlig;", "mu&szlig;t", "my", "myself", "m&uuml;ssen", "n", "nach", "nachdem", "name", "namely",
            "nd", "near", "nearly", "necessary", "need", "needs", "nein", "neither", "never", "nevertheless", "new", "next", "nicht", "nine",
            "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "nun", "o", "obviously",
            "oder", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others",
            "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "p", "particular", "particularly",
            "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "q", "que", "quite", "qv", "r",
            "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "s",
            "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems",
            "seen", "seid", "sein", "seine", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she",
            "should", "shouldn't", "sich", "sie", "since", "sind", "six", "so", "soll", "sollen", "sollst", "sollt", "some", "somebody",
            "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "sonst", "soon", "sorry", "soweit", "sowie",
            "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "t", "t's", "take", "taken", "tell", "tends", "th",
            "than", "thank", "thanks", "thanx", "that", "that's", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence",
            "there", "there's", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "they'd", "they'll",
            "they're", "they've", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru",
            "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "u", "un",
            "und", "under", "unfortunately", "unless", "unlikely", "unser", "unsere", "unter", "until", "unto", "up", "upon", "us", "use", "used",
            "useful", "uses", "using", "usually", "uucp", "v", "value", "various", "very", "via", "viz", "vom", "von", "vor", "vs", "w", "wann",
            "want", "wants", "warum", "was", "was", "wasn't", "way", "we", "we'd", "we'll", "we're", "we've", "weiter", "weitere", "welcome",
            "well", "wenn", "went", "wer", "werde", "werden", "werdet", "were", "weren't", "weshalb", "what", "what's", "whatever", "when",
            "whence", "whenever", "where", "where's", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which",
            "while", "whither", "who", "who's", "whoever", "whole", "whom", "whose", "why", "wie", "wieder", "wieso", "will", "willing", "wir",
            "wird", "wirst", "wish", "with", "within", "without", "wo", "woher", "wohin", "won't", "wonder", "would", "would", "wouldn't", "x",
            "y", "yes", "yet", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "z", "zero", "zu", "zum",
            "zur", "&uuml;ber"};

}
