//Get a randomizer like with the poker game, and get 25 random words for the wordlist

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.*;

public class WordSorter{
    public static String word(){
        
        int intWord[][] = new int[400][2];
        int intRandom;
        int intCount;

        for(intCount = 1; intCount <= 400; intCount++){
			intWord[intCount][0] = intCount;
			intRandom = (int)(Math.random() * 100 + 1);
			intWord[intCount][1] = intRandom;
		}

		//Bubble Sorter
        int intCount2;
		int intWordTemp;
		int intRandTemp;
		for(intCount2 = 0; intCount2 < 400-1; intCount2++){
			for(intCount = 0; intCount < 400-1; intCount++){
				if((intWord[intCount][1]) < (intWord[intCount+1][1])){
					intWordTemp = intWord[intCount][0];
					intWord[intCount][0] = intWord[intCount+1][0];
					intWord[intCount+1][0] = intWordTemp;
					intRandTemp = intWord[intCount][2];
					intWord[intCount][1] = intWord[intCount+1][1];
					intWord[intCount+1][1] = intRandTemp;
				}
			}
		}

		BufferedReader words = null;
		try{
			words = new BufferedReader(new FileReader("wordlist.txt"));
		}catch(FileNotFoundException e){
			System.out.println("No file");
		}

		String strWord="";

		try{
			strWord = words.readLine();
		}catch(IOException e){

		}
		//while(words.eof() != true){
		//	String strWord;
		//	intCount = 1;
		//	if(intCount == intWord[intCount-1][0]){
		//		words.readLine();
		//	}
		//}
        return strWord;
    }
	public static void main(String[] args){
		String strWord;
		strWord = 
		System.out.println(word.strWord);
}
}