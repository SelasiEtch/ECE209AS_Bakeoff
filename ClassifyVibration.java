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

import java.util.Timer;
import java.util.TimerTask;





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
	int Instrument = 0;
	SoundFile drum_1;
	SoundFile drum_2;
	SoundFile keyboard_1;
	SoundFile keyboard_2;
	SoundFile guitar_1;
	SoundFile guitar_2;
	String classification;
	String[] Select_Sequence = new String[2];
	int Select_index = 0;
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
			
		 
		drum_1 = new SoundFile(this,"33179__mattlohkamp__tom_snr.wav");
		drum_2 = new SoundFile(this,"587245__michatroschka__05_when.wav");
		
		keyboard_1 = new SoundFile(this,"316901__jaz_the_man_2__do-octave.wav");
		keyboard_2 = new SoundFile(this,"409480__dwengomes__pianofff6-audiotrimmercom.mp3");
		
		guitar_1 = new SoundFile(this,"36280__johnnypanic__plucked1.wav");
		guitar_2 = new SoundFile(this,"11897__medialint__vp795_7thfret_d_a.wav");
		
		
		/* list all audio devices */
		Sound.list();
		Sound s = new Sound(this);
		  
		/* select microphone device */
		s.inputDevice(9);
		    
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
				// Silent and Start Frame True;
				startFrame=false;
				if(numlabels>1)
				{
					classification = framelabels[0];
					
					if(Instrument != 0)
					{
						if(classification == "slide")
						{
							Instrument = 0;
						}
						else if(Instrument == 1)
						{
							if(classification == "tap")
							{
								drum_1.play();
							}
							else if(classification == "knuckle")
							{
								drum_2.play();
							}
						}
						else if(Instrument == 2)
						{
							if(classification == "tap")
							{
								guitar_1.play();
							}
							else if(classification == "knuckle")
							{
								guitar_2.play();
							}
						}
					}
					
					if((Select_index < 2) && (Instrument == 0))
					{
						Select_Sequence[Select_index] = classification;
						Select_index++;
						if(Select_index == 2)
						{
							numlabels=10;
						}
					}
					else if (Instrument == 0)
					{
						Select_index = 0;
						if((Select_Sequence[0] == "tap") && (Select_Sequence[1] == "tap"))
						{
							Instrument = 1; // Drum
							println("Drum Selected");
							
						}
						else if((Select_Sequence[0] == "knuckle") && (Select_Sequence[1] == "knuckle"))
						{
							Instrument = 2; // Guitar
							println("Guitar Selected");
						}
						Select_Sequence[0] = "Empty";
						Select_Sequence[1] = "Empty";
					}
					numlabels=0;
					println(classification);
					
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
				weka.core.SerializationHelper.write("/Users/selasi/Desktop/training.model", classifier);
				System.out.println("Saving done!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if (key == 'l') {
			// Yang: add code to load your previously trained model
			try {
				 classifier = (MLClassifier) weka.core.SerializationHelper.read("/Users/selasi/Desktop/training.model");
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
