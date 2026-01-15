import java.io.*;
public class testdoc {
    public static void main(String[] args){

        BufferedReader words = null;
        try{
            words = new BufferedReader(new FileReader("wordlist.txt"));
        }catch(FileNotFoundException e){
            System.out.println("No file");
        }

        try{
            System.out.println(words.readLine());
        }catch(IOException e){

        }
    }
}
