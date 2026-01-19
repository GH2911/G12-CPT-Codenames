import java.io.*;
public class testdoc {
    public static void main(String[] args){

        int intWord[][] = new int[400][2];
        int intRandom;
        int intCount;
		Boolean blnTF = false;

        for(intCount = 0; intCount < 400; intCount++){
			intWord[intCount][0] = intCount;
			intRandom = (int)(Math.random() * 400 + 1);
			intWord[intCount][1] = intRandom;
		}

		//Bubble Sorter
        int intCount2;
		int intWordTemp;
		int intRandTemp;
		for(intCount2 = 0; intCount2 < 400-1; intCount2++){
			for(intCount = 0; intCount < 400-1; intCount++){
				if((intWord[intCount][1]) > (intWord[intCount+1][1])){
					intWordTemp = intWord[intCount][0];
					intWord[intCount][0] = intWord[intCount+1][0];
					intWord[intCount+1][0] = intWordTemp;
					intRandTemp = intWord[intCount][1];
					intWord[intCount][1] = intWord[intCount+1][1];
					intWord[intCount+1][1] = intRandTemp;
				}
			}
		}

		System.out.println(intWord[23][0]);

		BufferedReader wordfile = null;
		try{
            wordfile = new BufferedReader(new FileReader("wordlist.txt"));
        }catch(FileNotFoundException e){
            System.out.println("No file");
        }
		String strWord[] = new String[400];
		for(intCount = 0; intCount<400; intCount++){
				try{
					strWord[intCount] = wordfile.readLine();
				}catch(IOException e){
					System.out.println("Error");
				}
		}

		String strWordList[] = new String[25];
		for(intCount = 0; intCount<25; intCount++){
			strWordList[intCount] = strWord[intWord[intCount][0]];
		}

		for(intCount = 0; intCount<25; intCount++){
			System.out.println(strWordList[intCount]);
		}

        PrintWriter WordList = null;
        try{
            WordList = new PrintWriter(new FileWriter("gamelist.txt"));
        }catch(IOException e){
            System.out.println("Error 2");
        }

        for(intCount=0; intCount<25; intCount++){
            WordList.println(strWordList[intCount]);
        }
        WordList.close();

    }
}
