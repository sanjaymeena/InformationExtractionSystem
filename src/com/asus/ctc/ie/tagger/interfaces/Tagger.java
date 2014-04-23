package com.asus.ctc.ie.tagger.interfaces;

import com.asus.ctc.ie.datastructures.TextNode;

public interface Tagger {
    public TextNode performSyntacticTagging(String doc);
    public TextNode performSyntacticTagging(TextNode doc);
}
