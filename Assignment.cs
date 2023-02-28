using System;
using System.IO;
using UnityEngine;
using UnityEngine.Assertions;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using UnityEditor;
using UnityEngine.Networking;
using Oculus.Interaction.Grab;
using Oculus.Interaction.GrabAPI;
using Oculus.Interaction.Input;
using Random=UnityEngine.Random;

namespace Oculus.Interaction.HandPosing
{
    public class DataCollection : MonoBehaviour
    {
        
        static DataCollection instance;

        [Header("Participant Index (e.g., P0)")]
        public string _participant = "P0"; // type in P0, P1,P2 ... to help you distinguish data from different users

        [Header("Enable Logging")] 
        public bool enable = true; // this script only collects data when enable is true
        [SerializeField] private HandGrabInteractor handGrab; // drag the dominant hand into this blank in the inspector
        
        [SerializeField] private GameObject cube; // drag the target cube into this blank in the inspector
        
        private bool isGrabbed = false; // if the object is grabbed this frame, isGrabbed is true
        private bool wasGrabbed = false; // if the object was grabbed last frame, wasGrabbed is true
        private bool isStart = false; // true when starting to grab
        private bool isEnd = false; // true when starting to release
        private float grabTime = Mathf.Infinity; // elapsed time of moving the cube from point A to point B
        private float grabDistance = Mathf.Infinity; // the distance between point A to point B
        private float grabSize = Mathf.Infinity; // the size of the cube
        private float initialPos; // the initial position of the cube
        private float initialTime; // the initial timestamp of user interaction (moving the cube from A to B)
        
        /*
        This template script only creates one cube. To investigate Fitts' Law, 
        we need to create many more cubes of various sizes, and move them to various distances.
        
        Please add variables here as per your need.  
        */
        [SerializeField] private GameObject cube2;
        private float grabSize2 = Mathf.Infinity; 
        private float comparePos = Mathf.Infinity; 
        private float count = 0;

        private float short_scale = 0.1f;
        private float medium_scale = 0.2f;
        private float large_scale = 0.3f;

       //private float short_dis = 0.5f;
        private float medium_dis = 0.6f;
       private float large_dis = 0.7f; 

        private StreamWriter _writer; // to write data into a file
        private string _filename; // the name of the file

        // Ensure this script will run across scenes. Do not delete it.
        private void Awake()
        {
            
            if (instance != null)
            {
                Destroy(gameObject);
            }
            else{
                instance = this;
                DontDestroyOnLoad(gameObject);
            }

        }

        // Save all data into the file when we quit the app
        private void OnApplicationQuit() {
            Debug.Log("On application quit");
            if (_writer != null) {
                _writer.Flush();
                _writer.Close();
                _writer.Dispose();
                _writer = null;
            }
        }
        
        // Save all data into the file when we pause the app
        private void OnApplicationPause(bool pauseStatus)
        {
            Debug.Log("On application pause");
            if (_writer != null) {
                _writer.Flush();
                _writer.Close();
                _writer.Dispose();
                _writer = null;
            }
        }
        
        private void Start()
        {
            // Create a csv file to save the data
            string filename = $"{_participant}-{Now}.csv";
            string path = Path.Combine(Application.persistentDataPath, filename); 
            // if you run it in the Unity Editor on Windows, the path is %userprofile%\AppData\LocalLow\<companyname>\<productname>
            // if you run it on Mac, the path is the ~/Library/Application Support/company name/product name
            // if you run it on a standalone VR headset, the path is Oculus/Android/data/<packagename>/files
            // reference here: https://docs.unity3d.com/ScriptReference/Application-persistentDataPath.html
            _writer = new StreamWriter(path);
            string msg = $"grabTime" +
                        $"grabSize" +
                        $"grabDistance" +
                        $"count";
            _writer.WriteLine(msg);
            Debug.Log(msg);
            _writer.Flush();
        }

        void Update()
        {
            // only collect data when enable is true
            if (enable == false) return;
            
            // read the cube size
            grabSize = cube.transform.localScale.y;
            grabSize2 = cube2.transform.localScale.y;

            // read the grab status
            isGrabbed = (InteractorState.Select == handGrab.State);
            // print("isGrabbed: "+ isGrabbed);
            isStart = !wasGrabbed && isGrabbed;
            // print("isStart: "+ isStart);
            isEnd = wasGrabbed && !isGrabbed;
            // print("isEnd: "+ isEnd);
            

            // start counting time and distance once a user grabs the cube
            if (isStart){
                initialPos = cube.transform.position.x;
                initialTime = Time.time;
                comparePos = cube2.transform.position.x;
                //ChangColor(cube);
                
            }
            // stop counting time and distance once a user releases the cube
            if (isEnd){
                float endPos = cube.transform.position.x;
                grabDistance = Mathf.Abs(endPos - initialPos);
                grabTime = Time.time - initialTime;
                
                
                if((Mathf.Abs(comparePos-endPos)<(0.2*grabSize)))
                {
                    count++;
                    WriteToFile(grabTime, grabSize, grabDistance);
                    StartCoroutine(ChangColor(cube,0,1,0));
                    if(count>9 && count<=19)
                    {
                        cube.transform.localScale = new Vector3(medium_scale, medium_scale, medium_scale);
                        cube2.transform.localScale = new Vector3(medium_scale, medium_scale, medium_scale);
                    }
                   if(count>19 && count<=29)
                    {
                        cube.transform.localScale = new Vector3(large_scale, large_scale, large_scale);
                        cube2.transform.localScale = new Vector3(large_scale, large_scale, large_scale);
                    }
                    if(count>29 && count<=39)
                    {
                        cube.transform.localScale = new Vector3(short_scale, short_scale, short_scale);
                        cube2.transform.localScale = new Vector3(short_scale, short_scale, short_scale);
                        cube.transform.position = new Vector3(medium_dis, -0.15f,0.4f);
                        cube2.transform.position = new Vector3(medium_dis, -0.15f,0.4f);
                    }
                    if(count>39 && count<=49)
                    {
                        cube.transform.localScale = new Vector3(medium_scale, medium_scale, medium_scale);
                        cube2.transform.localScale = new Vector3(medium_scale, medium_scale, medium_scale);
                    }
                      if(count>49 && count<=59)
                    {
                        cube.transform.localScale = new Vector3(large_scale, large_scale, large_scale);
                        cube2.transform.localScale = new Vector3(large_scale, large_scale, large_scale);
                    }
                      if(count>59 && count<=69)
                    {
                        cube.transform.localScale = new Vector3(short_scale, short_scale, short_scale);
                        cube2.transform.localScale = new Vector3(short_scale, short_scale, short_scale);
                        cube.transform.position = new Vector3(large_dis, -0.15f,0.4f);
                        cube2.transform.position = new Vector3(large_dis, -0.15f,0.4f);
                    }
                    if(count>69 && count<=79)
                    {
                        cube.transform.localScale = new Vector3(medium_scale, medium_scale, medium_scale);
                        cube2.transform.localScale = new Vector3(medium_scale, medium_scale, medium_scale);
                    }
                      if(count>79 && count<=89)
                    {
                        cube.transform.localScale = new Vector3(large_scale, large_scale, large_scale);
                        cube2.transform.localScale = new Vector3(large_scale, large_scale, large_scale);
                    }
                    if(count>89)
                    {
                        Application.Quit();
                    }
                }
                else
                {
                    StartCoroutine(ChangColor(cube,1,0,0));
                    //cube.transform.position = new Vector3(initialPos,-0.15f,0.4f);
                }
                
                
            }   

            wasGrabbed = isGrabbed;
            
        }
        
        // write T, W, D into the file.
        private void WriteToFile(float grabTime, float grabSize, float grabDistance) {
            if (_writer == null) return;
            
            string msg = $"{grabTime}," +
                        $"{grabSize}," +
                        $"{grabDistance}"
                        +$"{count}";
            _writer.WriteLine(msg);
            Debug.Log("test msg: "+msg);
            _writer.Flush();
        }
        
        // generate the current timestamp for filename
        private double Now {
            get {
                System.DateTime epochStart = new System.DateTime(1970, 1, 1, 0, 0, 0, System.DateTimeKind.Utc);
                return (System.DateTime.UtcNow - epochStart).TotalMilliseconds;
            }
        }

        IEnumerator ChangColor(GameObject cube, float red, float green, float blue)
             {
                 //Get the Renderer component from the new cube
                 var cubeRenderer = cube.GetComponent<Renderer>();

                 Color oldColor = cubeRenderer.material.color;

                 //Call SetColor using the shader property name "_Color" and setting the color randomly
                 Color newColor = new Color(red,green,blue);

                 cubeRenderer.material.SetColor("_Color", newColor);

                 yield return new WaitForSeconds(1);

                 cubeRenderer.material.SetColor("_Color", oldColor);
                cube.transform.position = new Vector3(initialPos,-0.15f,0.4f);



            }
    
    }
}
