package src.Main;
//7025927
//Jordan Wallace

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import javax.swing.plaf.synth.SynthEditorPaneUI;

public class Main{
    int _populationSize = 500; //the population size
    int _chromosomeLength; //the chromosome length
    int _crossoverRate = 100; //crossover rate
    int _maximumGenerationSpan = 50; //generation span
    int _seed = 1; //the seed
    int _k = 3; //k for tournament selection
    
    double _mutationRate = 10; //the % chance of a mutation happening
    
    String _data; //the data that needs to be decrypted

    Scanner _scanner; //the scanner that reads from the file
    
    Chromosome[] _population; //array that holds the population
    Chromosome _elite; //the elite chromosome

    static Random _random; //random

    BufferedWriter _csvWriter; //the bufferedwriter that writes to a csv file 

    //main constructor
    public Main(){
        try {
            _csvWriter = new BufferedWriter(new FileWriter("average_fitness.csv"));
            _csvWriter.write("Generation,Average Fitness,Best Fitness\n"); // Write header to the CSV file
        } catch (IOException e) {
            e.printStackTrace();
        }

        ReadFile(); //read from the file and save parameters
        _random = new Random(_seed); //initialize random
        InitializePopulation(); //initialize the population
        StartAlgorithm(); //start the algorithm
    }

    //this method reads the data from the file
    private void ReadFile(){
        try (FileInputStream file = new FileInputStream("Data2.txt")){ //try to read from given file
            _scanner = new Scanner(file); //initialize scanner
            _chromosomeLength = _scanner.nextInt(); //set the chromosome length to the first int in the file
            _scanner.nextLine(); //skip to the next line
            
            while(_scanner.hasNextLine()){ //while there is a line to be read
                _data += _scanner.nextLine(); //add the line to the data
            }
        }
        catch(IOException error){ //if exception hit
            System.out.println("Invalid File"); //the error message
        }
    }

    private void InitializePopulation(){
        //this method initializes the population of chromosomes
        _population = new Chromosome[_populationSize]; //create the initial population

        for (int i = 0; i < _population.length; i++) //for each chromosome in the population
            _population[i] = new Chromosome(_chromosomeLength); //initialize the chromosome
    }

    private void StartAlgorithm(){
        //for (int j = 0; j < 5; j++){
            _seed = 1;
            for (int i = 0; i < 5; i++){ //the number of trials to run
                for (int generation = 1; generation <= _maximumGenerationSpan; generation++) {
                    //select parents for crossover
                    Chromosome[] parents = GenerateNewParents(); //generate new parents

                    //create a new population through crossover and mutation
                    Chromosome[] newPopulation = GenerateNewPopulation(parents);

                    //replace the old population with the new one
                    _population = newPopulation;

                    //print information about the best chromosome in this generation
                    _elite = GetBestChromosome(_population);

                    if (_elite != null) InsertElite();

                    System.out.println("Generation " + generation + ": Average Fitness = " + CalculateAverageFitness(newPopulation) + " Best Fitness = " + Evaluation.fitness(_elite.GetKey(), _data));

                    //try to write to csv
                    try {_csvWriter.write(generation + "," + CalculateAverageFitness(newPopulation) + "," + Evaluation.fitness(_elite.GetKey(), _data) + "\n");}// Write generation and average fitness to the CSV file
                    catch (IOException e){e.printStackTrace();} //catch
                }
                _seed++; //increment seed
                _random = new Random(_seed); //update the seed
                InitializePopulation();
            }
            //_populationSize -= 100;
        //}

        //close the CSV file when done
        try {_csvWriter.close();} 
        catch (IOException e) {e.printStackTrace();} //catch
        System.out.println(_elite.GetKey()); //print the key after finished
    }

    //this method inserts the elite into the population
    private void InsertElite(){
        _population[_random.nextInt(_populationSize)] = _elite;
    }

    //this method calculates the average fitness of the given population
    private double CalculateAverageFitness(Chromosome[] population) {
        double totalFitness = 0.0; //the total fitness
    
        //foreach chromosome in the population
        for (Chromosome chromosome : population) {
            double fitness = Evaluation.fitness(chromosome.GetKey(), _data); //evaluate and save the fitness
            totalFitness += fitness; //add the fitness to the total
        }
    
        return totalFitness / population.length; //return the avverage fitness
    }

    //this method returns a random chromosome
    private Chromosome GetRandomChromosome(){
        return _population[_random.nextInt(_population.length)];
    }

    //this method returns the best chromosome from the given chromosome population
    private Chromosome GetBestChromosome(Chromosome[] population) {
        Chromosome bestChromosome = population[0]; //set best chromosome to first one
        for (int i = 0; i < population.length; i++) { //foreach chromosome in the population
            if (Evaluation.fitness(population[i].GetKey(), _data) < Evaluation.fitness(bestChromosome.GetKey(), _data)) //if the current chromosome is better than the best one
                bestChromosome = population[i]; //replace the best one with the current one ALIASING ISSUE HERE
        }
        return bestChromosome; //return the best chromosome
    }

    //this method generates new parents
    private Chromosome[] GenerateNewParents(){
        Chromosome[] parentPopulation = new Chromosome[_populationSize]; //generate a new array of parents
        //for the size of the population
        for (int i = 0; i < _populationSize; i++){
            parentPopulation[i] = TournamentSelection(_k); //select a new parent
        }
        return parentPopulation; //return the new population of parents
    }

    //tournament selection
    private Chromosome TournamentSelection(int k){
        Chromosome[] participants = new Chromosome[k]; //make a new array for the tournament participants
        //for the amount of chromosomes to choose
        for (int i = 0; i < k; i++)
            participants[i] = GetRandomChromosome(); //return a random chomosome

        return GetBestChromosome(participants); //return the best chromosome from the tournament selection participants
    }

    //this method generates a new population
    private Chromosome[] GenerateNewPopulation(Chromosome[] parents) {
        Chromosome[] newPopulation = new Chromosome[_populationSize]; //new population
        Chromosome[] currentParents = new Chromosome[2]; //the current parents being crossed over

        for (int i = 0; i < _populationSize; i += 2){ //for the population size (-1 to account for using parent [i] and [i+1])
            currentParents[0] = parents[i]; //current parent 1
            currentParents[1] = parents[i+1]; //current parent 2

            //if crossover is required
            if (_random.nextDouble() <= ((double) _crossoverRate / 100)){
                Chromosome[] childChromosomes = UniformCrossover(currentParents); //sent the new

                newPopulation[i] = childChromosomes[0]; //add the crossover child to new population
                newPopulation[i+1] = childChromosomes[1]; //add the crossover child to new population
            }
            //if no crossover
            else{
                newPopulation[i] = currentParents[0]; //pass the parent through
                newPopulation[i+1] = currentParents[1]; //pass the other parent through
            }
        }

        MutatePopulation(); //mutate the population

        return newPopulation; //return the new population
    }

    //(scramble) mutate population
    private void MutatePopulation(){
        int mutatedGenesStartPoint = _chromosomeLength-1; //the index of the gene to start the mutation from
        int mutatedGenesEndPosition = 0; //the index of the gene to end the mutation at

        //selecting the gene mutation range
        for (int i = 0; i < _populationSize; i++){ //for the size of the population
            if (_random.nextDouble() <= (_mutationRate / 100)){ //if a chromosome must mutate
                mutatedGenesStartPoint = _random.nextInt(_chromosomeLength-1); //pick a random number of genes to mutate

                while (mutatedGenesEndPosition < mutatedGenesStartPoint) //while the end position appears before the start position
                    mutatedGenesEndPosition = _random.nextInt(_chromosomeLength); //set the end position of the mutated genes to a random index

                //mutating the genes
                char[] genes = new char[mutatedGenesEndPosition-mutatedGenesStartPoint]; //create a new array to hold the mutated genes
                char[] chromosomeKey = _population[i].GetKey().toCharArray(); //get the key of the chosen chromosome
                int indexCounter = 0; //the index counter for the genes index

                //take the genes from the chromosome
                for (int j = mutatedGenesStartPoint; j < mutatedGenesEndPosition; j++){ //for the range of the gene segment
                    genes[indexCounter] = chromosomeKey[j]; //save the value from the chromosome's key to the genes segment array
                    indexCounter++; //increment the index counter
                }

                genes = ShuffleCharArray(genes); //scramble the genes segment array
                indexCounter = 0; //reset the index counter int

                //put the scrambled genes back into the chromosome
                for (int j = mutatedGenesStartPoint; j < mutatedGenesEndPosition; j++){ //for the range of the gene segment
                    chromosomeKey[j] = genes[indexCounter]; //save the scrambled genes back into the chromosome
                    indexCounter++; //increment the index counter
                }

                _population[i].SetKey(String.valueOf(chromosomeKey)); //save the chromosome key
            }
        }
    }

    //this method randomly shuffles the given char array's indexes
    public static char[] ShuffleCharArray(char[] input) {
        char[] shuffledArray = input.clone(); //create a copy of the input array to shuffle

        Random random = new Random();
        for (int i = shuffledArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1); //generate a random index between 0 and i
            //swap the characters at indices i and j
            char temp = shuffledArray[i];
            shuffledArray[i] = shuffledArray[j];
            shuffledArray[j] = temp;
        }
        return shuffledArray;
    }

    //this method returns the (uniform) crossover children of 2 parents
    private Chromosome[] UniformCrossover(Chromosome[] parents){
        char[] childOneKey = parents[0].GetKey().toCharArray(); //child 1 key
        char[] childTwoKey = parents[1].GetKey().toCharArray(); //child 2 key
        Chromosome children[] = new Chromosome[parents.length]; //create a new child array of the same size as the parent input argument

        //the crossover
        //for the length of chromosomes
        for (int i = 0; i < _chromosomeLength-1; i++){
            if (_random.nextBoolean()){ //if the current char index needs to be masked (random boolean)
                char temp = childOneKey[i]; //save the character at i (in child 1)
                childOneKey[i] = childTwoKey[i]; //let child one take the chromosome from child two
                childTwoKey[i] = temp; //let child two take the saved chromosome from child one
            }
        }

        children[0] = new Chromosome(String.valueOf(childOneKey)); //set the first child's key to the new crossover key
        children[1] = new Chromosome(String.valueOf(childTwoKey)); //set the secon child's key to the new crossover key

        return children; //return the children
    }

    //this method returns the (two point) crossover children of 2 parents
    private Chromosome[] TwoPointCrossover(Chromosome[] parents) {
    int chromosomeLength = parents[0].GetLength(); //get the length of chromosomes
    int crossoverPoint1 = _random.nextInt(chromosomeLength); //first crossover point
    int crossoverPoint2 = _random.nextInt(chromosomeLength); //second crossover point
    Chromosome children[] = new Chromosome[parents.length]; //create a new child array of the same size as the parent input argument

    //ensure that crossoverPoint 2 is after crossoverPoint 1
    if (crossoverPoint1 > crossoverPoint2) {
        int temp = crossoverPoint1; //save temporarypoint 
        crossoverPoint1 = crossoverPoint2; //set point 1 to to
        crossoverPoint2 = temp; //set point 2 to the saved point
    }

    char[] childOneKey = new char[chromosomeLength]; //child 1 key
    char[] childTwoKey = new char[chromosomeLength]; //child 2 key

    //for genes before the first crossover point 
    for (int i = 0; i < crossoverPoint1; i++) {
        childOneKey[i] = parents[0].GetKey().charAt(i); //give child 1 the gene at current location
        childTwoKey[i] = parents[1].GetKey().charAt(i); //give child 2 the gene at current location
    }

    //for genes between the first and second crossover points
    for (int i = crossoverPoint1; i < crossoverPoint2; i++) {
        childOneKey[i] = parents[1].GetKey().charAt(i); //give child 1 the gene at current location
        childTwoKey[i] = parents[0].GetKey().charAt(i); //give child 2 the gene at current location
    }

    //for genes after the second crossover point 
    for (int i = crossoverPoint2; i < chromosomeLength; i++) {
        childOneKey[i] = parents[0].GetKey().charAt(i); //give child 1 the gene at current location
        childTwoKey[i] = parents[1].GetKey().charAt(i); //give child 2 the gene at current location
    }

    children[0] = new Chromosome(String.valueOf(childOneKey)); //set the first child's key to the new crossover key
    children[1] = new Chromosome(String.valueOf(childTwoKey)); //set the secon child's key to the new crossover key

    return children; //return the children
    }

    public static void main(String[] args){Main m = new Main();};
}