package com.asus.ctc.ie.utilities;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import com.asus.ctc.ie.datastructures.TextNode;



public class Serialization {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		TextNode abk=new TextNode();
		
		//abk.setConceptSentenceKnowledgeMap(conceptSentenceKnowledgeMap);
		
		String filename="IEandQGResources/temp/testSerialazationOfArticleKnowledge.ser";
		
		Serialization ser=new Serialization();
		ser.serializeDataStructure(abk,filename);
		
		
		TextNode abk1=ser.deSerializeDataStructure(filename);
		
		System.out.println(abk1.getContent());
		
		
	}

	
	public void serializeDataStructure(TextNode abk,String filename)
	{
		
		try
	      {
	         FileOutputStream fileOut =
	         new FileOutputStream(filename);
	         ObjectOutputStream out =
	                            new ObjectOutputStream(fileOut);
	         out.writeObject(abk);
	         out.close();
	          fileOut.close();
	      }catch(IOException i)
	      {
	          i.printStackTrace();
	      }
	}
	
	public TextNode deSerializeDataStructure(String filename)
	{
	    TextNode e = null;
	      try
	      {
	         FileInputStream fileIn =
	                          new FileInputStream(filename);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         e = (TextNode) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();
	         return null;
	      }catch(ClassNotFoundException c)
	      {
	         System.out.println("Article Based Knowledge cannot be found");
	         c.printStackTrace();
	         return null;
	      }
		
	      
	      return e;
	}
	
	
	
}
