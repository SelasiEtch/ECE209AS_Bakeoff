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

/* A class with the main function and Processing visualizations to run the demo */

public class ClassifyVibration extends PApplet {

	FFT fft;
	FFT knuckle_fft, nail_fft, pad_fft,silence_fft;
	AudioIn in;
	Waveform waveform;
	Waveform knuckle_wav, nail_wav, pad_wav,silence_wav;
	int bands = 512;
	int nsamples = 1024;
	float[] spectrum = new float[bands];
	float[] tracker_spec = new float[bands];
	float[] tracker_spec2 = new float[bands];
	float[] tracker_spec3 = new float[bands];
	float[] mea = new float[bands];
	float[] fftFeatures = new float[bands];
	String[] classNames = {"knuckle","nail", "silence"};
	int temp = 0;
	int classIndex = 0;
	int dataCount = 0;
	SoundFile knuckle, nail, pad,silence;
	MLClassifier classifier;
	
	Map<String, List<DataInstance>> trainingData = new HashMap<>();
	{for (String className : classNames){
		trainingData.put(className, new ArrayList<DataInstance>());
	}}
	
	DataInstance captureInstance (String label){
		DataInstance res = new DataInstance();
		res.label = label;
		
		
		/*if(temp==1)
		{
			mea=tracker_spec.clone();
			println("cloning knuckle");
		}
		else if(temp==2)
		{
			mea= tracker_spec2.clone();
		}
		else if(temp==4)
		{
			mea= tracker_spec3.clone();
		}
		else if(temp==3)
		{
			mea=fftFeatures.clone();
		}*/
		res.measurements = fftFeatures.clone();
		/*if(mea==tracker_spec3)
		{
			println("success");
		}*/
		return res;
	}
	
	public static void main(String[] args) {
		PApplet.main("ClassifyVibration");
	}
	
	public void settings() {
		size(512, 400);
	}

	public void setup() {
		
		 knuckle = new SoundFile(this, "right_forefinger_knuckle_1-5inches_away.wav");
		 nail = new SoundFile(this, "right_forefinger_nail_1-5inches_away.wav");
		 silence = new SoundFile(this, "silence.wav");
		 //pad = new SoundFile(this, "right_forefinger_pad_1-5inches_away.wav");
		 knuckle_wav = new Waveform(this, nsamples);
		 nail_wav = new Waveform(this, nsamples);
		 silence_wav = new Waveform(this, nsamples);
		 //pad_wav = new Waveform(this, nsamples);
		 knuckle_fft = new FFT(this, bands);
		 nail_fft = new FFT(this, bands);
		 silence_fft = new FFT(this, bands);
		 //pad_fft = new FFT(this, bands);
		 
		 
		
		// pad_wav.input(pad);
		 //knuckle.play();
		 
		 //nail.play();
			
		 nail_wav.input(nail);
		 nail_fft.input(nail);
		 knuckle_wav.input(knuckle);
			knuckle_fft.input(knuckle);	
			silence_wav.input(silence);
			silence_fft.input(silence);	
			 
		
		 
		
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
		 /*if(temp==1)
		 {
					 	knuckle_fft.analyze(tracker_spec);
					 	
						trainingData.get("knuckle").add(captureInstance("knuckle"));
				
		 }
		if (temp==2) {
		
		nail_fft.analyze(tracker_spec2);	
		println(tracker_spec2);
		trainingData.get("nail").add(captureInstance("nail"));
		
		}
		if (temp==4) {
			
			silence_fft.analyze(tracker_spec3);	
			//println(tracker_spec2);
			trainingData.get("silence").add(captureInstance("silence"));
			
			}*/
			
		
			 
			
				
		
		
		//println(max(fftFeatures));
		fill(255);
		textSize(30);
		if(classifier != null) {
			String guessedLabel = classifier.classify(captureInstance(null));
			
			// Yang: add code to stabilize your classification results
			
			text("classified as: " + guessedLabel, 20, 30);
			println(guessedLabel);
		}else {
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
			/*temp=3;
			knuckle.stop();
			 nail.stop();
			 silence.stop();*/
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
		}
		
		else if (key == 'l') {
			// Yang: add code to load your previously trained model
		}
			
		
		/*else if (key == 'k')
		{
			knuckle.play();
			nail.stop();
			silence.stop();
			temp =1;
			
		}
		else if (key == 'n')
		{
			
			nail.play();
			knuckle.stop();
			silence.stop();
			temp=2;
			
		}
		else if(key =='m'){
		
			silence.play();
			knuckle.stop();
			nail.stop();
			temp=4;
	}*/
		else
		{
		 
			trainingData.get(classNames[classIndex]).add(captureInstance(classNames[classIndex]));
		}
		
	}

}
