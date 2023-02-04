import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import processing.core.PApplet;
import processing.sound.AudioIn;
import processing.sound.FFT;
import processing.sound.Sound;
import processing.sound.SoundFile;
import processing.sound.Waveform;

import weka.core.SerializationHelper;

/* A class with the main function and Processing visualizations to run the demo */

public class ClassifyVibration extends PApplet {

	FFT fft;
	AudioIn in;
	Waveform waveform;
	Waveform knuckle_wav, nail_wav, pad_wav,silence_wav;
	int bands = 512;
	int nsamples = 1024;
	float[] spectrum = new float[bands];
	float[] fftFeatures = new float[bands];
	String[] classNames = {"knuckle","nail", "silence","tap","slide"};
	int temp = 0;
	int classIndex = 0;
	int dataCount = 0;
	String temp_store;
	MLClassifier classifier;
	boolean startFrame=false;
	String[] framelabels = new String[100];
	int numlabels=0;
	SoundFile drum;
	String classification;
	Map<String, List<DataInstance>> trainingData = new HashMap<>();
	{for (String className : classNames){
		trainingData.put(className, new ArrayList<DataInstance>());
	}}
	
	DataInstance captureInstance (String label){
		DataInstance res = new DataInstance();
		res.label = label;
		
		res.measurements = fftFeatures.clone();
		
		return res;
	}
	
	public static void main(String[] args) {
		PApplet.main("ClassifyVibration");
	}
	
	public void settings() {
		size(512, 400);
	}

	public void setup() {
		
		 
		drum = new SoundFile(this,"582758__martina_leitschuh__moan_outdoors_man.wav");
		/* list all audio devices */
		Sound.list();
		Sound s = new Sound(this);
		  
		/* select microphone device */
		s.inputDevice(8);
		    
		/* create an Input stream which is routed into the FFT analyzer */
		fft = new FFT(this, bands);
		in = new AudioIn(this, 0);
		waveform = new Waveform(this, nsamples);
		
		waveform.input(in);
		
		/* start the Audio Input */
		in.start();
		
		/* patch the AudioIn */
		fft.input(in);
		
	}

	public void draw() {
		background(0);
		fill(0);
		stroke(255);
		
		waveform.analyze();

		beginShape();
		  
		for(int i = 0; i < nsamples; i++)
		{
			vertex(
					map(i, 0, nsamples, 0, width),
					map(waveform.data[i], -1, 1, 0, height)
					);
		}
		
		endShape();

		fft.analyze(spectrum);
		
		for(int i = 0; i < bands; i++){

			/* the result of the FFT is normalized */
			/* draw the line for frequency band i scaling it up by 40 to get more amplitude */
			line( i, height, i, height - spectrum[i]*height*40);
			fftFeatures[i] = spectrum[i];
		}
	
		
			 
			
				
		
		
		//println(max(fftFeatures));
		fill(255);
		textSize(30);
		if(classifier != null) {
			String guessedLabel = classifier.classify(captureInstance(null));
			
			// Yang: add code to stabilize your classification results
			
			text("classified as: " + guessedLabel, 20, 30);
			if(guessedLabel.charAt(1)!='i' && startFrame==false)
			{
				startFrame=true;
				framelabels[numlabels]=guessedLabel;
				numlabels++;
				
			//println(guessedLabel);
				
			}
			else if(guessedLabel.charAt(1)!='i' && startFrame==true)
			{
				framelabels[numlabels]=guessedLabel;
				numlabels++;
			}
			else
			{
				startFrame=false;
				if(numlabels>1)
				{
					classification = framelabels[0];
					numlabels=0;
					println(classification);
					if(classification.charAt(0)=='t')
					{
						drum.play();
					}
					
				}
				else
				{
					classification = "silent";
					numlabels=0;
				}
				
			}
			temp_store = guessedLabel;
		}
		/*else if(add_cl==true){
			text(classNames[classIndex], 20, 30);
			dataCount = trainingData.get(classNames[classIndex]).size();
			text("Data collected: " + dataCount, 20, 60);
		}*/
		else {
			text(classNames[classIndex], 20, 30);
			dataCount = trainingData.get(classNames[classIndex]).size();
			text("Data collected: " + dataCount, 20, 60);
		}
	}
	
	public void keyPressed() {
		

		if (key == CODED && keyCode == DOWN) {
			classIndex = (classIndex + 1) % classNames.length;
		}
		
		else if (key == 't') {
			if(classifier == null) {
				println("Start training ...");
				classifier = new MLClassifier();
				classifier.train(trainingData);
			}else {
				classifier = null;
			}
		}
		/*else if(key=='a')
		{
			if (add_cl==false)
			{
				add_cl=true;
			}
			else if(add_cl == true)
			{
				add_cl=false;
				println("Start training ...");
				//classifier = new MLClassifier();
				classifier.train(trainingData);
			}
		}*/
		
		else if (key == 's') {
			// Yang: add code to save your trained model for later use

			
			/*try {
				ObjectOutputStream oos = new ObjectOutputStream(
			            new FileOutputStream("C:\\Users\\emaga\\OneDrive\\Desktop\\training.model"));
				oos.writeObject(classifier);
				oos.flush();
				oos.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			;*/
			try {
				weka.core.SerializationHelper.write("C:\\Users\\emaga\\OneDrive\\Desktop\\training.model", classifier);
				System.out.println("Saving done!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if (key == 'l') {
			// Yang: add code to load your previously trained model
			try {
				 classifier = (MLClassifier) weka.core.SerializationHelper.read("C:\\Users\\emaga\\OneDrive\\Desktop\\training.model");
				 System.out.println("Loading done!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
	
		else
		{
		 
			trainingData.get(classNames[classIndex]).add(captureInstance(classNames[classIndex]));
		}
		
	}

}
