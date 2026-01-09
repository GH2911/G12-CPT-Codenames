//Get a randomizer like with the poker game, and get 25 random words for the wordlist
public class WordSorter{
    public static int[][] word(){
        
        int intWord[][] = new int[400][2];
        int intRandom;
        int intCount;
        
        int intCount2;
		int intWordTemp;
		int intRandTemp;
		for(intCount2 = 0; intCount2 < 52-1; intCount2++){
			for(intCount = 0; intCount < 52-1; intCount++){
				if((intWord[intCount][1]) > (intWord[intCount+1][1])){
					intWordTemp = intWord[intCount][0];
					intWord[intCount][0] = intWord[intCount+1][0];
					intWord[intCount+1][0] = intWordTemp;
					intRandTemp = intWord[intCount][2];
					intWord[intCount][1] = intWord[intCount+1][1];
					intWord[intCount+1][1] = intRandTemp;
				}
			}
		}

        return intWord;
    }
}