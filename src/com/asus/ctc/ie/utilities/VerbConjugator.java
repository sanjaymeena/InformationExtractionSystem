package com.asus.ctc.ie.utilities;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.Dictionary;
import static com.asus.ctc.ie.config.GlobalProperties.*;

/**
 * Verb Conjugator Class
 * 
 * @author Sanjay_Meena
 * 
 */
public class VerbConjugator {

    /**
	 * 
	 */
    public VerbConjugator() {
	baseFormCountMap = new HashMap<String, Long>();
	conjugationMap = new HashMap<String, String>();
	try {
	    JWNL.initialize(new FileInputStream(wordnet_file_properties));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * @param filePath
     */
    public void save(String filePath) {
	try {
	    PrintWriter pw = new PrintWriter(new FileOutputStream(filePath));
	    for (Map.Entry<String, String> entry : conjugationMap.entrySet()) {
		String key = entry.getKey();
		String[] parts = key.split("/");
		String token = entry.getValue();
		pw.println(parts[0] + "\t" + parts[1] + "\t" + token);
	    }

	    pw.println("*");

	    for (Map.Entry<String, Long> entry : baseFormCountMap.entrySet()) {
		String key = entry.getKey();
		Long count = entry.getValue();
		pw.println(key + "\t" + count);
	    }

	    pw.flush();
	    pw.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * @param filePath
     */
    public void load(String filePath) {
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    new FileInputStream(filePath)));
	    String buf;
	    int state = 0;
	    while ((buf = br.readLine()) != null) {
		if (buf.equals("*")) {
		    state++;
		    continue;
		}
		if (state == 0) {
		    String[] parts = buf.split("\\t");
		    if (parts.length != 3) {
			continue;
		    }
		    String key = parts[0].toLowerCase() + "/" + parts[1];
		    String token = parts[2].toLowerCase();
		    conjugationMap.put(key, token);
		} else if (state == 1) {
		    String[] parts = buf.split("\\t");
		    if (parts.length != 2) {
			continue;
		    }
		    String key = parts[0].toLowerCase();
		    Long count = new Long(parts[1]);
		    baseFormCountMap.put(key, count);
		}
	    }
	    br.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * @param lemma
     * @param pos
     * @return string
     */
    public String getSurfaceForm(String lemma, String pos) {
	String result = new String(lemma);
	String key = lemma + "/" + pos;
	if (conjugationMap.containsKey(key)) {
	    result = conjugationMap.get(key);
	} else if (pos.equals("VBD") || pos.equals("VBZ")) {
	    if (!lemma.matches("^.*[aieou]$")) {
		// char lastChar = lemma.charAt(lemma.length()-1);
		// if(lastChar == 'a'
		// || lastChar == 'e'
		// || lastChar == 'i'
		// || lastChar == 'o'
		// || lastChar == 'u')
		// {
		result += "e";
	    }
	    if (pos.equals("VBD")) {
		result += "d";
	    } else if (pos.equals("VBZ")) {
		result += "s";
	    }
	}

	return result;
    }

    /**
     * @param lemma
     * @return int
     */
    public int getBaseFormCount(String lemma) {
	Long result = baseFormCountMap.get(lemma);
	if (result == null) {
	    result = new Long(0);
	}

	return result.intValue();
    }

    /**
     * @param path
     */
    public void readFromTreebankFile(String path) {
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    new FileInputStream(path)));
	    String buf;
	    while ((buf = br.readLine()) != null) {
		Pattern p = Pattern.compile("\\((VB\\w*) (\\w+)\\)");
		Matcher m = p.matcher(buf);
		while (m.find()) {
		    String pos = m.group(1);
		    String token = m.group(2);
		    if (pos.equals("VB")) {
			Long count = baseFormCountMap.get(token);
			if (count == null) {
			    count = new Long(0);
			}
			count++;
			baseFormCountMap.put(token, count);
		    } else {
			String lemma = "";
			try {
			    IndexWord iw = Dictionary.getInstance()
				    .lookupIndexWord(POS.VERB, token);
			    if (iw == null) {
				continue;
			    }
			    lemma = iw.getLemma();
			} catch (Exception e) {
			    e.printStackTrace();
			}

			String key = lemma + "/" + pos;
			System.err.println("adding\t" + key + "\t" + token);
			conjugationMap.put(key, token);
		    }
		}

		br.close();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	VerbConjugator vc = new VerbConjugator();
	vc.readFromTreebankFile(args[0]);
	vc.save("verbConjugations.txt");

	// vc.load("verbConjugations.txt");

	System.err.println(vc.getSurfaceForm("walk", "VBZ"));
	System.err.println(vc.getSurfaceForm("walk", "VBD"));
	System.err.println(vc.getSurfaceForm("alleviate", "VBZ"));
	System.err.println(vc.getSurfaceForm("alleviate", "VBD"));
	System.err.println(vc.getBaseFormCount("walk"));
	System.err.println(vc.getBaseFormCount("alleviate"));
    }

    // map from lemma+pos to surface form (e.g., walk+VBZ => walks)
    Map<String, String> conjugationMap;

    Map<String, Long> baseFormCountMap;

}
