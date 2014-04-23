package com.asus.ctc.ie.utilities;



import static com.asus.ctc.ie.config.GlobalProperties.verbConjugations;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asus.ctc.ie.config.GlobalProperties;
import com.asus.ctc.ie.datastructures.senserelation.SuperSenseWord;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeReader;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

/**
 * Utility Class which provides many functions useful for question generation.
 * 
 * @author Sanjay_Meena
 */
public class AnalysisUtilities {
    private final static Logger log = LoggerFactory
	    .getLogger(AnalysisUtilities.class);

    private AnalysisUtilities() {
	parser = null;

	conjugator = new VerbConjugator();
	conjugator.load(verbConjugations);
	headfinder = new CollinsHeadFinder();
	tree_factory = new LabeledScoredTreeFactory();
	tlp = new PennTreebankLanguagePack();
    }

    /**
     * Adds period at the end of input sentence tree if required.
     * 
     * @param input
     *            input sentence tree
     */
    public void addPeriodIfNeeded(Tree input) {
	String tregexOpStr = "ROOT < (S=mainclause !< /\\./)";
	TregexPattern matchPattern = TregexPatternFactory
		.getPattern(tregexOpStr);
	TregexMatcher matcher = matchPattern.matcher(input);

	if (matcher.find()) {
	    TsurgeonPattern p;
	    List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
	    List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();

	    ps.add(Tsurgeon.parseOperation("insert (. .) >-1 mainclause"));
	    p = Tsurgeon.collectOperations(ps);
	    ops.add(new Pair<TregexPattern, TsurgeonPattern>(matchPattern, p));
	    Tsurgeon.processPatternsOnTree(ops, input);
	}
    }

    /**
     * Returns number of matches of regular expression in the input tree
     * 
     * @param tregexExpression
     *            tree regular expression to match
     * @param t
     *            Tree in which pattern needs to be searched.
     * @return res number of matches
     */
    public int getNumberOfMatchesInTree(String tregexExpression, Tree t) {
	int res = 0;
	TregexMatcher m = TregexPatternFactory.getPattern(tregexExpression)
		.matcher(t);
	while (m.find()) {
	    res++;
	}
	return res;
    }

    /**
     * Extract list of sentences from the source document.
     * 
     * @param document
     *            Source Document
     * @return list of sentences
     */
    public List<String> getSentences(String document) {

	/**
	 * The format for DocumentPreprocessor has changed. CHanging the dp to
	 * accept a reader.
	 */

	// StringReader stringreader=new StringReader(document);

	List<String> res = new ArrayList<String>();
	document = preprocess(document);
	StringReader reader = new StringReader(document);
	DocumentPreprocessor dp = new DocumentPreprocessor(reader);
	// dp.setElementDelimiter(" ");
	for (List<HasWord> sentence1 : dp) {

	    // Stanford code converts "" to `` when converting EmailSentence
	    // DataStrucure
	    // Simple hack for avoiding "" converted to ``

	    String temp = Sentence.listToString(sentence1, true, " ");
	    temp = temp.replaceAll("``", "\"");
	    temp = temp.replaceAll("''", "\"");
	    temp = temp.replaceAll("\\\\/", "/");
	    temp = temp.replaceAll(" 's", "'s");
	    if (temp.endsWith(".")) {
		temp = temp.replace(" .", ".");
		if (temp.endsWith("..")) {
		    temp = temp.replace("..", ".");
		}
	    }
	    res.add(temp);
	    System.out.println(temp);
	}
	return res;
    }

    /**
     * Condense the tree
     * 
     * @param tree
     * @return abbreviated tree
     */
    public String abbrevTree(Tree tree) {
	ArrayList<String> toks = new ArrayList<String>();
	for (Tree L : tree.getLeaves()) {
	    toks.add(L.label().value());
	}
	return tree.label().value() + "[" + StringUtils.join(toks, " ") + "]";
    }

    /**
     * Downcase the first token in the input tree
     * 
     * @param inputTree
     */
    public void downcaseFirstToken(Tree inputTree) {
	Tree firstWordTree = inputTree.getLeaves().get(0);
	if (firstWordTree == null)
	    return;
	Tree preterm = firstWordTree.parent(inputTree);
	String firstWord = treeToString(firstWordTree);
	if (!preterm.label().value().matches("^NNP.*")
		&& !firstWord.equals("I")) {
	    // if(firstWord.indexOf('-') == -1 && !firstWord.equals("I")){
	    firstWord = firstWord.substring(0, 1).toLowerCase()
		    + firstWord.substring(1);
	    firstWordTree.label().setValue(firstWord);
	}

	// if(IEProperties.DEBUG)
	// System.err.println("downcaseFirstToken: "+inputTree.toString());
    }

    /**
     * Capitalize the first token in the input tree
     * 
     * @param inputTree
     */
    public void upcaseFirstToken(Tree inputTree) {
	Tree firstWordTree = inputTree.getLeaves().get(0);
	if (firstWordTree == null)
	    return;

	String firstWord = treeToString(firstWordTree);
	firstWord = firstWord.substring(0, 1).toUpperCase()
		+ firstWord.substring(1);
	firstWordTree.label().setValue(firstWord);

	// if(IEProperties.DEBUG)
	// System.err.println("upcaseFirstToken: "+inputTree.toString());
    }

    /**
     * Remove unwanted symbols from the input sentence.
     * 
     * @param sentence
     *            input sentence
     * @return Filtered sentence
     */
    public String preprocess(String sentence) {
	// remove trailing whitespace

	// String biuNormalizerFile = null;

	/*
	 * File stringRulesFile = null; try { stringRulesFile = new File(
	 * GlobalProperties.getProperties(1).getProperty("biuNormalizerFile",
	 * biuNormalizerFile)); } catch (Exception e) {
	 * System.out.println("File error"); }
	 */
	sentence = sentence.trim();

	// remove single words in parentheses.
	// the stanford parser api messed up on these
	// by removing the parentheses but not the word in them
	// sentence = sentence.replaceAll("\\(\\S*\\)","");
	// sentence = sentence.replaceAll("\\(\\s*\\)","");

	// Replacing the paranthesis contents.
	sentence = sentence.replaceAll("\\(.*?\\)", "");

	// some common unicode characters that the tokenizer throws out
	// otherwise
	sentence = sentence.replaceAll("—", "--");
	sentence = sentence.replaceAll("’", "'");
	sentence = sentence.replaceAll("”", "\"");
	sentence = sentence.replaceAll("\"", "\"");
	sentence = sentence.replaceAll("“", "\"");
	sentence = sentence.replaceAll("é|è|ë|ê", "e");
	sentence = sentence.replaceAll("É|È|Ê|Ë", "E");
	sentence = sentence.replaceAll("ì|í|î|ï", "i");
	sentence = sentence.replaceAll("Ì|Í|Î|Ï", "I");
	sentence = sentence.replaceAll("à|á|â|ã|ä|æ|å", "a");
	sentence = sentence.replaceAll("À|Á|Â|Ã|Ä|Å|Æ", "A");
	sentence = sentence.replaceAll("ò|ó|ô|õ|ö", "o");
	sentence = sentence.replaceAll("Ò|Ó|Ô|Õ|Ö", "O");
	sentence = sentence.replaceAll("ù|ú|û|ü", "u");
	sentence = sentence.replaceAll("Ù|Ú|Û|Ü", "U");
	sentence = sentence.replaceAll("ñ", "n");

	// contractions

	sentence = sentence.replaceAll("'d", " would");
	sentence = sentence.replaceAll("'re", " are");
	sentence = sentence.replaceAll("'ve", " have");
	sentence = sentence.replaceAll("'ll", " will");
	sentence = sentence.replaceAll("it's", "it is");
	sentence = sentence.replaceAll("'m", " am");

	sentence = sentence.replaceAll("US dollars", "dollars");
	sentence = sentence.replaceAll("\\$", "dollars ");

	sentence = sentence.replaceAll("kilograms", "kg");
	sentence = sentence.replaceAll("kilogram", " kg");
	sentence = sentence.replaceAll("kilometers", "kms");
	sentence = sentence.replaceAll("kilometer", "km");
	sentence = sentence.replaceAll("kilometers per hour", "kmh");
	sentence = sentence.replaceAll("miles per hour", "mph");
	sentence = sentence.replaceAll("centimeters", "cm");
	sentence = sentence.replaceAll("centimeter", "cm");
	sentence = sentence.replaceAll("grams", "gram");

	sentence = sentence.replaceAll("doesn't", "does not");
	sentence = sentence.replaceAll("wouldn't", "would not");
	sentence = sentence.replaceAll("couldn't", "could not");
	sentence = sentence.replaceAll("shouldn't", "should not");
	sentence = sentence.replaceAll("didn't", "did not");

	sentence = sentence.replaceAll("don't", "do not");
	sentence = sentence.replaceAll("can't", "can not");
	sentence = sentence.replaceAll("won't", "will not");
	sentence = sentence.replaceAll("n't", " not"); // aren't shouldn't don't
						       // isn't

	sentence = sentence.replaceAll("Mr.", "Mr ");
	sentence = sentence.replaceAll("Dr.", "Dr ");
	sentence = sentence.replaceAll("Miss.", "Miss ");
	sentence = sentence.replaceAll("Mrs.", " Mrs ");
	sentence = sentence.replaceAll("&", "and");

	// simply remove other unicode characters
	// if not, the tokenizer replaces them with spaces,
	// which wreaks havoc on the final parse sometimes
	for (int i = 0; i < sentence.length(); i++) {
	    if (sentence.charAt(i) > 'z') {
		sentence = sentence.substring(0, i) + " "
			+ sentence.substring(i + 1);
	    }
	}

	// add punctuation to the end if necessary
	/*
	 * Matcher matcher = Pattern.compile(".*\\.['\"\n ]*$",
	 * Pattern.DOTALL).matcher(sentence); if(!matcher.matches()){ sentence
	 * += "."; }
	 */

	/*
	 * BiuNormalizer n;
	 * 
	 * try { // n = new BiuNormalizer(stringRulesFile); //sentence =
	 * n.normalize(sentence); //System.out.println(sentence);
	 * 
	 * } catch (Exception e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 */
	return sentence;
    }

    /**
     * Remove unwanted symbols from the input sentence parse tree.
     * 
     * @param sentence
     * @return filtered tree
     */
    public String preprocessTreeString(String sentence) {
	sentence = sentence.replaceAll(" n't", " not");
	sentence = sentence.replaceAll("\\(MD ca\\)", "(MD can)");
	sentence = sentence.replaceAll("\\(MD wo\\)", "(MD will)");
	sentence = sentence.replaceAll("\\(MD 'd\\)", "(MD would)");
	sentence = sentence.replaceAll("\\(VBD 'd\\)", "(VBD had)");
	sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
	sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
	sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
	sentence = sentence.replaceAll("\\(VBP 're\\)", "(VBP are)");

	return sentence;
    }

    /**
     * @return verbconjugator
     */
    public VerbConjugator getConjugator() {
	return conjugator;
    }

    /**
     * @return CollinsHeadFinder
     */
    public CollinsHeadFinder getHeadFinder() {
	return headfinder;
    }

    /**
     * Return instance of this class
     * 
     * @return AnalysisUtilities
     */
    public static AnalysisUtilities getInstance() {
	if (instance == null) {
	    instance = new AnalysisUtilities();
	}
	return instance;
    }

    /**
     * function which sends the sentence to the stanford parser and retrieves
     * the parse tree from it.
     * 
     * @param sentence
     *            source sentence
     * @return the parsed result
     */
    public ParseResult parseSentence(String sentence) {
	String result = "";
//	Tree parse = null;
//	double parseScore = Double.MIN_VALUE;
//	
	// System.err.println(sentence);
	// see if a parser socket server is available
	int port = new Integer(GlobalProperties.getProperties(1).getProperty(
		"parserServerPort", "5556"));
	String host = "127.0.0.1";
	Socket client;
	PrintWriter pw;
	BufferedReader br;
	String line;
	Tree parse = null;
	double parseScore = Double.MIN_VALUE;

	try {
	    client = new Socket(host, port);

	    pw = new PrintWriter(client.getOutputStream());
	    br = new BufferedReader(new InputStreamReader(
		    client.getInputStream()));
	    pw.println(sentence);
	    pw.flush(); // flush to complete the transmission

	    /**
	     * 1)Removed the ready method. It was giving issues 2)Removed the
	     * else condition and parseScore method
	     */
	    while ((line = br.readLine()) != null) {
		line = line.replaceAll("\n", "");
		line = line.replaceAll("\\s+", " ");
		result += line + " ";

	    }

	    br.close();
	    pw.close();
	    client.close();

	    if (parse == null) {
		parse = readTreeFromString("(ROOT (. .))");
		parseScore = -99999.0;
	    }

	    if (GlobalProperties.getDebug())
		System.err.println("result (parse):" + result);
	    parse = readTreeFromString(result);
	    return new ParseResult(true, parse, parseScore);

	} catch (Exception ex) {
	    if (GlobalProperties.getDebug())
		System.err.println("Could not connect to parser server.");
	    // ex.printStackTrace();
	}

	System.err.println("parsing:" + sentence);

	// if socket server not available, then use a local parser object
	if (parser == null) {
	    try {
		Options op = new Options();
		int maxLength = new Integer(GlobalProperties.getProperties(1)
			.getProperty("parserMaxLength", "40")).intValue();
		String[] options = { "-maxLength", Integer.toString(maxLength),
			"-outputFormat", "oneline" };

		String serializedInputFileOrUrl = GlobalProperties
			.getProperties(1).getProperty(
				"parserGrammarFile");

		op.setOptions(options);
		parser = LexicalizedParser.loadModel(serializedInputFileOrUrl,
			op);

		/**
		 * Not applicable in the new version of Stanford.
		 */
		// parser.setMaxLength();
		// parser.setOptionFlags("maxLength",
		// Integer.toString(maxLength),"-outputFormat", "oneline");
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	try {

	    parse = parser.parse(sentence);
	    if (parse != null) {

		// remove all the parent annotations (this is a hacky way to do
		// it)
		String ps = parse.toString().replaceAll(
			"\\[[^\\]]+/[^\\]]+\\]", "");
		// System.out.println("Hello......   " + ps);
		parse = AnalysisUtilities.getInstance().readTreeFromString(ps);

		parseScore = 0.0;
		return new ParseResult(true, parse, parseScore);
	    }
	} catch (Exception e) {
	}

	parse = readTreeFromString("(ROOT (. .))");
	parseScore = -99999.0;
	return new ParseResult(false, parse, parseScore);
    }

    /**
     * Get the lemma of a word
     * 
     * @param word
     *            the word for which lemma is to be found
     * @param pos
     *            part of speech of word
     * @return lemma
     */
    public String getLemma(String word, String pos) {
	if (!(pos.startsWith("N") || pos.startsWith("V") || pos.startsWith("J") || pos
		.startsWith("R")) || pos.startsWith("NNP")) {
	    return word.toLowerCase();
	}

	String res = word.toLowerCase();

	if (res.equals("is") || res.equals("are") || res.equals("were")
		|| res.equals("was")) {
	    res = "be";
	} else {
	    try {
		IndexWord iw;
		if (pos.startsWith("V"))
		    iw = Dictionary.getInstance().getMorphologicalProcessor()
			    .lookupBaseForm(POS.VERB, res);
		else if (pos.startsWith("N"))
		    iw = Dictionary.getInstance().getMorphologicalProcessor()
			    .lookupBaseForm(POS.NOUN, res);
		else if (pos.startsWith("J"))
		    iw = Dictionary.getInstance().getMorphologicalProcessor()
			    .lookupBaseForm(POS.ADJECTIVE, res);
		else
		    iw = Dictionary.getInstance().getMorphologicalProcessor()
			    .lookupBaseForm(POS.ADVERB, res);

		if (iw == null)
		    return res;
		res = iw.getLemma();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	return res;
    }

    /**
     * Remove traces and non-terminal decorations (e.g., "-SUBJ" in "NP-SUBJ")
     * from a Penn Treebank-style tree.
     * 
     * @param inputTree
     */
    public void normalizeTree(Tree inputTree) {
	inputTree.label().setFromString("ROOT");

	List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
	List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
	String tregexOpStr;
	TregexPattern matchPattern;
	TsurgeonPattern p;
	TregexMatcher matcher;

	tregexOpStr = "/\\-NONE\\-/=emptynode";
	matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
	matcher = matchPattern.matcher(inputTree);
	ps.add(Tsurgeon.parseOperation("prune emptynode"));
	matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
	p = Tsurgeon.collectOperations(ps);
	ops.add(new Pair<TregexPattern, TsurgeonPattern>(matchPattern, p));
	Tsurgeon.processPatternsOnTree(ops, inputTree);

	Label nonterminalLabel;

	tregexOpStr = "/.+\\-.+/=nonterminal < __";
	matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
	matcher = matchPattern.matcher(inputTree);
	while (matcher.find()) {
	    nonterminalLabel = matcher.getNode("nonterminal");
	    if (nonterminalLabel == null)
		continue;
	    nonterminalLabel.setFromString(tlp.basicCategory(nonterminalLabel
		    .value()));
	}

    }

    /**
     * remove extra quotation marks (a hack due to annoying PTB conventions by
     * which quote marks aren't in the same consituent)
     * 
     * @param input
     */
    public void removeExtraQuotes(Tree input) {
	List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
	String tregexOpStr;
	TregexPattern matchPattern;
	TsurgeonPattern p;
	List<TsurgeonPattern> ps;

	ps = new ArrayList<TsurgeonPattern>();
	tregexOpStr = "ROOT [ << (``=quote < `` !.. ('' < '')) | << (''=quote < '' !,, (`` < ``)) ] ";
	matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
	ps.add(Tsurgeon.parseOperation("prune quote"));
	p = Tsurgeon.collectOperations(ps);
	ops.add(new Pair<TregexPattern, TsurgeonPattern>(matchPattern, p));
	Tsurgeon.processPatternsOnTree(ops, input);

    }

    /**
     * filter the input tree
     * 
     * @param inputTree
     * @return filtered tree
     * @see #cleanUpSentenceString
     */
    public String getCleanedUpYield(Tree inputTree) {
	Tree copyTree = inputTree.deepCopy();

	// if(GlobalProperties.getDebug())
	// System.err.println("yield:"+copyTree.toString());

	return cleanUpSentenceString(treeToString(copyTree));
    }

    /**
     * Helper function to <a href="#getCleanedUpYield">getCleanedUpYield</a>
     * 
     * @param s
     *            input sentence
     * @return filtered sentence
     */
    public String cleanUpSentenceString(String s) {
	String res = s;
	// if(res.length() > 1){
	// res = res.substring(0,1).toUpperCase() + res.substring(1);
	// }

	res = res.replaceAll("\\s([\\.,!\\?\\-;:])", "$1");
	res = res.replaceAll("(\\$)\\s", "$1");
	res = res.replaceAll("can not", "cannot");
	res = res.replaceAll("\\s*-LRB-\\s*", " (");
	res = res.replaceAll("\\s*-RRB-\\s*", ") ");
	res = res.replaceAll("\\s*([\\.,?!])\\s*", "$1 ");
	res = res.replaceAll("\\s+''", "''");
	// res = res.replaceAll("\"", "");
	res = res.replaceAll("``\\s+", "``");
	res = res.replaceAll("\\-[LR]CB\\-", ""); // brackets, e.g., [sic]
	res = res.replaceAll("\\. \\?", ".?");
	res = res.replaceAll(" 's(\\W)", "'s$1");
	res = res.replaceAll("(\\d,) (\\d)", "$1$2"); // e.g., "5, 000, 000" ->
						      // "5,000,000"
	res = res.replaceAll("``''", "");

	// remove extra spaces
	res = res.replaceAll("\\s\\s+", " ");
	res = res.trim();

	return res;
    }

    /**
     * @param root
     * @param n1
     * @param n2
     * @return boolean
     */
    public boolean cCommands(Tree root, Tree n1, Tree n2) {
	if (n1.dominates(n2))
	    return false;

	Tree n1Parent = n1.parent(root);
	while (n1Parent != null && n1Parent.numChildren() == 1) {
	    n1Parent = n1Parent.parent(root);
	}

	if (n1Parent != null && n1Parent.dominates(n2))
	    return true;

	return false;
    }

    /**
     * Read tree from a string
     * 
     * @param parseStr
     *            input tree in form a string
     * @return tree
     */
    public Tree readTreeFromString(String parseStr) {
	// read in the input into a Tree data structure
	TreeReader treeReader = new PennTreeReader(new StringReader(parseStr),
		tree_factory);
	Tree inputTree = null;
	try {
	    inputTree = treeReader.readTree();
	    treeReader.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return inputTree;
    }

    /**
     * filter out a sentence by punctuation
     * 
     * @param sentence
     * @return boolean
     */
    public boolean filterOutSentenceByPunctuation(String sentence) {
	// return (sentence.indexOf("\"") != -1
	// || sentence.indexOf("''") != -1
	// || sentence.indexOf("``") != -1
	// || sentence.indexOf("*") != -1);
	if (sentence.indexOf("*") != -1) {
	    return true;
	}

	// if(sentence.matches("[^\\w\\-\\/\\?\\.,;:\\$\\#\\&\\(\\) ]")){
	// return true;
	// }

	return false;
    }

    /**
     * Convert from tree to string
     * 
     * @param tree
     * @return string
     */
    public String treeToString(Tree tree) {

	ArrayList<String> sentenceArray = treeToArrayList(tree);

	String sentence = Sentence.listToString(sentenceArray);
	return sentence;

    }

    /**
     * function to give length of an input parse tree
     * 
     * @param tree
     * @return length of tree
     */
    public int sentenceLength(Tree tree) {
	ArrayList<String> sentenceArray = treeToArrayList(tree);
	return sentenceArray.size();

    }

    /**
     * Convert form tree to ArrayList
     * 
     * @param tree
     * @return Tree
     */
    public ArrayList<String> treeToArrayList(Tree tree) {
	ArrayList<Label> test = tree.yield();
	ArrayList<String> arrayList = new ArrayList<String>();
	for (Object element : test) {
	    Label label = (Label) element;
	    arrayList.add(label.value());

	}

	return arrayList;

    }

    

    /**
     * @param senseList
     * @return
     */
    public String createSenseTags(List<SuperSenseWord> senseList) {

	String senseTagList = "";
	for (int i = 0; i < senseList.size(); i++) {
	    SuperSenseWord w = senseList.get(i);

	    if (!w.getSstag().equals("0"))
		senseTagList += w.getSstag() + ";";
	}

	return senseTagList;
	// TODO Auto-generated method stub

    }

    /**
     * Read a file and return back the text
     * 
     * @param filePath
     * @return
     */
    public String readDocument(String filePath) {
	ReadWriteTextFileWithEncoding ReadDocument = new ReadWriteTextFileWithEncoding(
		filePath, encoding);

	String document = "";

	try {
	    document = ReadDocument.read();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return document;

    }

    /**
     * write a string to the give file.
     * 
     * @param filePath
     * @return
     */
    public void WriteDocument(String filePath, String text) {
	try {
	    ReadWriteTextFileWithEncoding writeDocument = new ReadWriteTextFileWithEncoding(
		    filePath, encoding);

	    writeDocument.write(text);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    /**
     * function to get the surface form of a lemma.
     * 
     * @param lemma
     * @param pos
     * @return surface form a lemma
     */
    public String getSurfaceForm(String lemma, String pos) {
	return conjugator.getSurfaceForm(lemma, pos);
    }

    String encoding = "UTF-8";
    private LexicalizedParser parser;
    private static AnalysisUtilities instance;
    private VerbConjugator conjugator;
    private CollinsHeadFinder headfinder;
    private LabeledScoredTreeFactory tree_factory;
    private PennTreebankLanguagePack tlp;

}
