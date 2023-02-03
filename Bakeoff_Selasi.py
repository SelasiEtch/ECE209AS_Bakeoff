import soundfile as sf 
y, sr = sf.read('test.wav')
print(y.shape, y.dtype, sr)
sf.write('out.wav', y, sr)
