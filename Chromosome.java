package src.Main;
import java.util.Random;

public class Chromosome {
    private String _key;  // The key for decryption
    private int _length; //the length of the chromosome
    Random _random;

    //chromosome constructor
    public Chromosome(int length) {
        _random = new Random(Main._random.nextInt()); //set the same seed as main
        this._length = length; //save the length
        _key = GenerateRandomKey(length);
    }

    public Chromosome(String key){
        this._length = key.length(); //set the length to the length of the key string
        this._key = key; //set this key to key parameter
    }

    //this method generates and returns a random key for the chromosome
    private String GenerateRandomKey(int length){
        String key = ""; //string to return
        for (int i = 0; i < length; i++) //for the length of the chromosome
            key += GenerateRandomCharacter(); //add a random character to the string
        return key; //return the string
    }

    //this method generates and returns a random character from A to Z
    private char GenerateRandomCharacter(){
        return (char) (_random.nextInt(26) + 97); //return a random char in between ASCII 97 (a) and ASCII 122 (z) *inclusive
    }

    //this method returns the key
    public String GetKey() {
        return _key;
    }

    //this method sets the key to the given parameter
    public void SetKey(String key) {
        this._key = key;
    }

    //this method gets the length of the chromosome
    public int GetLength(){
        return this._length;
    }
}