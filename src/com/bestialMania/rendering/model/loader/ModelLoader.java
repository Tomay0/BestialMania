package com.bestialMania.rendering.model.loader;

import com.bestialMania.animation.*;
import com.bestialMania.MemoryManager;
import com.bestialMania.animation.AnimatedModel;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.xml.XmlNode;
import com.bestialMania.xml.XmlParser;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;

public class ModelLoader {
    /**
     * Load a model in the BMM format
     */
    public static Model loadModel(MemoryManager mm, String fileName) {
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(inputStream);

            //get the header
            char[] chars = new char[3];
            for (int i = 0; i < 3; i++) chars[i] = ois.readChar();
            if (chars[0] != 'B' || chars[1] != 'M' || chars[2] != 'M') {
                System.err.println("Invalid format for the model: " + fileName + ". Must be bmm!");
                return null;
            }

            //Start building the Model
            Model model = new Model(mm);
            char sectionHeader = ois.readChar();

            if(sectionHeader=='j') {
                System.err.println("Invalid format for the model: " + fileName + ". Must not be animated!");
                return null;
            }

            //Work out the indices. Tne next character should say 'i' if they exist
            if(sectionHeader=='i') {
                //start with size of indices
                int indexCount = ois.readInt();

                //build indices array
                int[] indices = new int[indexCount];
                for(int i = 0;i<indexCount;i++) {
                    indices[i] = ois.readInt();
                }
                model.bindIndices(indices);

                sectionHeader = ois.readChar();
            }

            //Work out the vertices, etc. The next character should say 'v'
            if(sectionHeader=='v') {
                //number of attributes to read from
                int nAttributes = ois.readInt();
                //number of vertices
                int vertexCount = ois.readInt();

                //loop through all the attributes
                for(int n = 0;n<nAttributes;n++) {
                    int position = ois.readInt();
                    int size = ois.readInt();
                    boolean isFloat = ois.readBoolean();

                    //float data
                    if(isFloat) {
                        float[] data = new float[vertexCount*size];
                        for(int i=0;i<data.length;i++) {
                            data[i] = ois.readFloat();
                        }
                        model.genFloatAttribute(position,size,data);
                    }
                    //integer data
                    else {
                        int[] data = new int[vertexCount*size];
                        for(int i=0;i<data.length;i++) {
                            data[i] = ois.readInt();
                        }
                        model.genIntAttribute(position,size,data);

                    }
                }
                sectionHeader = ois.readChar();
            }
            if(sectionHeader=='e') {
                System.out.println("Loaded model: " + fileName + " successfully!");
            }

            ois.close();
            inputStream.close();

            return model;

        }catch(FileNotFoundException e) {
            System.err.println("Could not find the file for the model: " + fileName);
            return null;
        }catch(EOFException  e) {
            System.err.println("Model file " + fileName + " appears to be incomplete, end of file reached.");
            e.printStackTrace();
            return null;
        }catch(IOException e) {
            System.err.println("Unable to load model: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load an animated model in the BMM format
     */
    public static AnimatedModel loadAnimatedModel(MemoryManager mm, String fileName) {
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(inputStream);

            //get the header
            char[] chars = new char[3];
            for (int i = 0; i < 3; i++) chars[i] = ois.readChar();
            if (chars[0] != 'B' || chars[1] != 'M' || chars[2] != 'M') {
                System.err.println("Invalid format for the model " + fileName + " must be bmm!");
                return null;
            }

            //Build the armature
            Armature armature = null;
            char sectionHeader = ois.readChar();
            if(sectionHeader=='j') {
                //build a list of all the joint names
                int nJoints = ois.readInt();
                Map<String,Joint> jointMap = new HashMap<>();
                Joint[] jointArray = new Joint[nJoints];
                for(int i = 0;i<nJoints;i++) {
                    //get the name
                    char[] charArray = new char[ois.readInt()];
                    for(int j=0;j<charArray.length;j++) {
                        charArray[j] = ois.readChar();
                    }
                    String jointName = new String(charArray);

                    //get the inverse bind transform
                    float[] matrixArray = new float[16];
                    for(int j=0;j<16;j++) matrixArray[j] = ois.readFloat();

                    Matrix4f invBindTransform = new Matrix4f(matrixArray[0],matrixArray[1],matrixArray[2],matrixArray[3],
                            matrixArray[4],matrixArray[5],matrixArray[6],matrixArray[7],
                            matrixArray[8],matrixArray[9],matrixArray[10],matrixArray[11],
                            matrixArray[12],matrixArray[13],matrixArray[14],matrixArray[15]);

                    //build the joint object
                    Joint joint = new Joint(jointName,i,invBindTransform);
                    jointMap.put(jointName,joint);
                    jointArray[i] = joint;
                }
                //build the joint hierarchy
                int rootJointID = ois.readInt();
                ois.readInt();
                Joint rootJoint = jointArray[rootJointID];
                parseJointHierarchy(ois,rootJointID,jointArray,1);

                //create the armature object
                armature = new Armature(jointMap,jointArray,rootJoint);

                sectionHeader = ois.readChar();

            }


            //Build the model
            Model model = new Model(mm);

            //Work out the indices. Tne next character should say 'i' if they exist
            if(sectionHeader=='i') {
                //start with size of indices
                int indexCount = ois.readInt();

                //build indices array
                int[] indices = new int[indexCount];
                for(int i = 0;i<indexCount;i++) {
                    indices[i] = ois.readInt();
                }
                model.bindIndices(indices);

                sectionHeader = ois.readChar();
            }

            //Work out the vertices, etc. The next character should say 'v'
            if(sectionHeader=='v') {
                //number of attributes to read from
                int nAttributes = ois.readInt();
                //number of vertices
                int vertexCount = ois.readInt();

                //loop through all the attributes
                for(int n = 0;n<nAttributes;n++) {
                    int position = ois.readInt();
                    int size = ois.readInt();
                    boolean isFloat = ois.readBoolean();

                    //float data
                    if(isFloat) {
                        float[] data = new float[vertexCount*size];
                        for(int i=0;i<data.length;i++) {
                            data[i] = ois.readFloat();
                        }
                        model.genFloatAttribute(position,size,data);
                    }
                    //integer data
                    else {
                        int[] data = new int[vertexCount*size];
                        for(int i=0;i<data.length;i++) {
                            data[i] = ois.readInt();
                        }
                        model.genIntAttribute(position,size,data);

                    }
                }
                sectionHeader = ois.readChar();
            }

            //build the animated model
            AnimatedModel animatedModel = new AnimatedModel(model,armature);

            //add the poses
            if(sectionHeader=='p') {
                int nPoses = ois.readInt();
                for(int i = 0;i<nPoses;i++) {
                    Pose pose = new Pose(i);
                    //add joints to the pose
                    int nJoints = ois.readInt();
                    for(int j=0;j<nJoints;j++) {
                        int jointID = ois.readInt();
                        Joint joint = armature.getJoint(jointID);

                        //position
                        float x = ois.readFloat();
                        float y = ois.readFloat();
                        float z = ois.readFloat();
                        Vector3f position = new Vector3f(x,y,z);

                        //rotation
                        float rx = ois.readFloat();
                        float ry = ois.readFloat();
                        float rz = ois.readFloat();
                        float rw = ois.readFloat();
                        Vector4f rotation = new Vector4f(rx,ry,rz,rw);

                        //matrix
                        float[] matrixArray = new float[16];
                        for(int k=0;k<16;k++) matrixArray[k] = ois.readFloat();

                        Matrix4f matrix = new Matrix4f(matrixArray[0],matrixArray[1],matrixArray[2],matrixArray[3],
                                matrixArray[4],matrixArray[5],matrixArray[6],matrixArray[7],
                                matrixArray[8],matrixArray[9],matrixArray[10],matrixArray[11],
                                matrixArray[12],matrixArray[13],matrixArray[14],matrixArray[15]);

                        pose.addTransform(joint,position, rotation, matrix);

                    }

                    animatedModel.addPose(pose);
                }

                sectionHeader = ois.readChar();
            }

            if(sectionHeader=='e') {
                System.out.println("Loaded model: " + fileName + " successfully!");
            }

            ois.close();
            inputStream.close();

            return animatedModel;

        }catch(FileNotFoundException e) {
            System.err.println("Could not find the file for the model: " + fileName);
            return null;
        }catch(EOFException  e) {
            System.err.println("Model file " + fileName + " appears to be incomplete, end of file reached.");
            e.printStackTrace();
            return null;
        }catch(IOException e) {
            System.err.println("Unable to load model: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recursively build the joint hierarchy
     */
    private static void parseJointHierarchy(ObjectInputStream ois, int root, Joint[] jointArray, int jointCount) throws IOException{
        int nextChild = root;
        while(jointCount<jointArray.length) {
            int nextInt = ois.readInt();
            if(nextInt==-2) return;//close of list of children
            // parse children of the child
            else if(nextInt==-1) {
                jointCount++;
                parseJointHierarchy(ois,nextChild,jointArray,jointCount);
            }
            //add the child to the root
            else {
                jointCount++;
                nextChild = nextInt;
                jointArray[root].addChild(jointArray[nextChild]);
            }
        }
        ois.readInt();
    }
}
