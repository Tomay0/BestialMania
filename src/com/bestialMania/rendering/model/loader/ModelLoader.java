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
    /*

    BMM format

     */

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
            if (chars[0] != 'B' || chars[1] != 'M' || chars[1] != 'M') {
                System.err.println("Invalid format for the model " + fileName + " must be bmm!");
                return null;
            }

            //Start building the Model
            Model model = new Model(mm);
            char sectionHeader = ois.readChar();

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

    public static AnimatedModel loadAnimatedModel(MemoryManager mm, String fileName) {
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(inputStream);

            //get the header
            char[] chars = new char[3];
            for (int i = 0; i < 3; i++) chars[i] = ois.readChar();
            if (chars[0] != 'B' || chars[1] != 'M' || chars[1] != 'M') {
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

/*
    *//*

    DAE/BMM -- TODO remove all this and only use the conversion methods

     *//*



    *//**
     * Builds the model from the given data
     *//*
    private static Model buildModel(MemoryManager mm, List<Vector3i> indices,List<ModelVertex> vertexList, boolean hasUvs, boolean hasNormals, boolean hasArmature) {
        Model model = new Model(mm);

        //build indices
        int[] newIndices = new int[indices.size()*3];
        for(int i = 0;i<indices.size();i++) {
            Vector3i v = indices.get(i);
            newIndices[i*3] = v.x;
            newIndices[i*3+1] = v.y;
            newIndices[i*3+2] = v.z;
        }
        model.bindIndices(newIndices);

        //build vertices
        float[] newVertices = new float[vertexList.size()*3];
        for(int i = 0;i<vertexList.size();i++) {
            ModelVertex v = vertexList.get(i);
            newVertices[i*3] = v.getVertex().x;
            newVertices[i*3+1] = v.getVertex().y;
            newVertices[i*3+2] = v.getVertex().z;
        }
        model.genFloatAttribute(0,3, newVertices);

        //build uvs
        if(hasUvs) {
            float[] newUVs = new float[vertexList.size()*2];
            for(int i = 0;i<vertexList.size();i++) {
                ModelVertex v = vertexList.get(i);
                newUVs[i*2] = v.getUV().x;
                newUVs[i*2+1] = v.getUV().y;
            }
            model.genFloatAttribute(1,2, newUVs);

        }

        //build normals
        if(hasNormals) {
            float[] newNormals = new float[vertexList.size() * 3];
            for(int i = 0;i<vertexList.size();i++) {
                ModelVertex v = vertexList.get(i);
                newNormals[i*3] = v.getNormal().x;
                newNormals[i*3+1] = v.getNormal().y;
                newNormals[i*3+2] = v.getNormal().z;
            }
            model.genFloatAttribute(2,3, newNormals);
        }

        //tangents
        if(hasUvs && hasNormals) {
            float[] tangents = new float[vertexList.size() * 3];
            for(int i = 0;i<vertexList.size();i++) {
                ModelVertex v = vertexList.get(i);
                tangents[i*3] = v.getTangent().x;
                tangents[i*3+1] = v.getTangent().y;
                tangents[i*3+2] = v.getTangent().z;
            }
            model.genFloatAttribute(3,3, tangents);
        }

        //vertex weight data
        if(hasArmature) {
            int[] jointIds = new int[vertexList.size() * 3];
            for(int i = 0;i<vertexList.size();i++) {
                ModelVertex v = vertexList.get(i);
                jointIds[i*3] = v.getVertexWeightData().getJointId(0);
                jointIds[i*3+1] = v.getVertexWeightData().getJointId(1);
                jointIds[i*3+2] = v.getVertexWeightData().getJointId(2);

            }
            model.genIntAttribute(4,3, jointIds);


            float[] weights = new float[vertexList.size() * 3];
            for(int i = 0;i<vertexList.size();i++) {
                ModelVertex v = vertexList.get(i);
                weights[i*3] = v.getVertexWeightData().getWeight(0);
                weights[i*3+1] = v.getVertexWeightData().getWeight(1);
                weights[i*3+2] = v.getVertexWeightData().getWeight(2);

            }
            model.genFloatAttribute(5,3, weights);
        }


        return model;
    }

    *//*

    OBJ

     *//*

    *//**
     * Loads a model from an OBJ file format
     *//*
    public static Model loadOBJ(MemoryManager mm, String file) {
        try {
            Scanner scan = new Scanner(new File(file));

            List<Vector3f> vertices = new ArrayList<>();
            List<Vector2f> uvs = new ArrayList<>();
            List<Vector3f> normals = new ArrayList<>();
            List<Vector3i> indices = new ArrayList<>();

            Map<String, ModelVertex> vertexMap = new HashMap<>();//map of vertex/uv/normals sets by a string to identify them
            List<ModelVertex> vertexList = new ArrayList<>();//list of the vertex/uv/normal sets in order
            List<VertexWeightData> vertexWeightData = new ArrayList<>();//empty list

            while(scan.hasNext()) {
                String head = scan.next();
                //vertices
                if(head.equals("v")) {
                    float x = scan.nextFloat();
                    float y = scan.nextFloat();
                    float z = scan.nextFloat();
                    vertices.add(new Vector3f(x,y,z));
                }
                //uvs
                else if(head.equals("vt")) {
                    float x = scan.nextFloat();
                    float y = scan.nextFloat();
                    uvs.add(new Vector2f(x,y));
                }
                //normals
                else if(head.equals("vn")) {
                    float x = scan.nextFloat();
                    float y = scan.nextFloat();
                    float z = scan.nextFloat();
                    normals.add(new Vector3f(x,y,z));
                }
                //faces
                else if(head.equals("f")) {
                    int id1 = processVertex(scan.next(),vertexMap,vertexList,vertices,uvs,normals,vertexWeightData);
                    int id2 = processVertex(scan.next(),vertexMap,vertexList,vertices,uvs,normals,vertexWeightData);
                    int id3 = processVertex(scan.next(),vertexMap,vertexList,vertices,uvs,normals,vertexWeightData);

                    //calculate tangent vectors if uvs and normals are both present
                    if(uvs.size()>0 && normals.size()>0) {
                        processTangents(vertexList.get(id1),vertexList.get(id2),vertexList.get(id3));
                    }
                    //face using ids of vertices
                    Vector3i face = new Vector3i(id1,id2,id3);
                    indices.add(face);
                }
                else {
                    scan.nextLine();
                }
            }

            scan.close();

            //build the model
            Model model = buildModel(mm,indices,vertexList,uvs.size()>0,normals.size()>0,false);


            return model;
        }catch(Exception e) {
            System.err.println("Unable to load model: " + file);
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }

    *//*
    DAE
     *//*

    *//**
     * Loads a regular DAE Model without armature information
     *//*
    public static Model loadDAEModel(MemoryManager mm, String fileName) {
        XmlNode rootNode = XmlParser.loadXmlFile(new File(fileName));

        Model model = loadMesh(mm,rootNode,new ArrayList<>());

        if(model==null) {
            System.err.println("Could not load model: " + fileName + ". Invalid format.");
            System.exit(-1);
        }

        return model;
    }


    *//**
     * Loads an animated DAE Model
     *//*
    public static AnimatedModel loadAnimatedDAE(MemoryManager mm, String fileName) {
        try {

            XmlNode rootNode = XmlParser.loadXmlFile(new File(fileName));

            List<VertexWeightData> vertexWeightData = new ArrayList<>();
            Armature armature = loadArmature(rootNode, vertexWeightData);

            if(armature==null) {
                System.err.println("Could not load armature information for " + fileName);
                System.exit(-1);
            }

            Model model = loadMesh(mm,rootNode,vertexWeightData);

            if(model==null) {
                System.err.println("Could not load model: " + fileName + ". Invalid format.");
                System.exit(-1);
            }

            AnimatedModel object = new AnimatedModel(model,armature);
            loadPoses(rootNode,object);

            return object;
        }catch(Exception e) {
            System.err.println("Unable to load model: " + fileName);
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }


    *//**
     * Loads the model from a DAE file
     *//*
    private static Model loadMesh(MemoryManager mm, XmlNode rootNode, List<VertexWeightData> vertexWeightData){
        XmlNode meshNode = rootNode.getChild("library_geometries");
        if(meshNode==null) return null;
        meshNode = meshNode.getChild("geometry");
        if(meshNode==null) return null;
        meshNode = meshNode.getChild("mesh");
        if(meshNode==null) return null;

        //get the sources of the texture coords and normals
        XmlNode polyList = meshNode.getChild("polylist");
        if(polyList==null) polyList = meshNode.getChild("triangles");
        if(polyList==null) return null;

        String verticesSource = meshNode.getChild("vertices").getChild("input").getAttribute("source").substring(1);
        String normalSource = null, texSource = null;
        for(XmlNode input : polyList.getChildren("input")) {
            if(input.getAttribute("semantic").equals("NORMAL")) normalSource = input.getAttribute("source").substring(1);
            else if(input.getAttribute("semantic").equals("TEXCOORD")) texSource = input.getAttribute("source").substring(1);
        }

        //get all vertices, uvs and normals
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> uvs = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();

        for(XmlNode source : meshNode.getChildren("source")) {
            //vertices
            if(source.getAttribute("id").equals(verticesSource)) {
                Scanner scan = new Scanner(source.getChild("float_array").getData());
                while(scan.hasNextFloat()) {
                    float x = scan.nextFloat();
                    float y = scan.nextFloat();
                    float z = scan.nextFloat();
                    vertices.add(new Vector3f(x,y,z));
                }
                scan.close();
            }
            //uvs
            else if(source.getAttribute("id").equals(texSource)) {
                Scanner scan = new Scanner(source.getChild("float_array").getData());
                while(scan.hasNextFloat()) {
                    float x = scan.nextFloat();
                    float y = scan.nextFloat();
                    uvs.add(new Vector2f(x,y));
                }
                scan.close();
            }
            //normals
            else if(source.getAttribute("id").equals(normalSource)) {
                Scanner scan = new Scanner(source.getChild("float_array").getData());
                while(scan.hasNextFloat()) {
                    float x = scan.nextFloat();
                    float y = scan.nextFloat();
                    float z = scan.nextFloat();
                    normals.add(new Vector3f(x,y,z));
                }
                scan.close();
            }

        }


        //process indices
        List<Vector3i> indices = new ArrayList<>();
        Map<String, ModelVertex> vertexMap = new HashMap<>();//map of vertex/uv/normals sets by a string to identify them
        List<ModelVertex> vertexList = new ArrayList<>();//list of the vertex/uv/normal sets in order

        XmlNode p = polyList.getChild("p");
        Scanner scan = new Scanner(p.getData());
        while(scan.hasNextInt()) {
            //3 vertex ids
            int ids[] = new int[3];

            for(int i = 0;i<3;i++) {
                int vertex = scan.nextInt()+1;
                int normal = -1;
                if(normals.size()>0) normal = scan.nextInt()+1;
                int uv = -1;
                if(uvs.size()>0) uv = scan.nextInt()+1;

                //Create a string in the format used by OBJs to use the processVertex method.
                String string = vertex + "";
                if(uv!=-1 || normal!=-1) {
                    string+="/";
                    if(uv!=-1) {
                        string += uv;
                    }
                    if(normal!=-1) {
                        string += "/" + normal;
                    }
                }
                ids[i] = ModelLoader.processVertex(string,vertexMap,vertexList,vertices,uvs,normals,vertexWeightData);
            }

            //calculate tangent vectors if uvs and normals are both present
            if(uvs.size()>0 && normals.size()>0) {
                ModelLoader.processTangents(vertexList.get(ids[0]),vertexList.get(ids[1]),vertexList.get(ids[2]));
            }
            //face using ids of vertices
            Vector3i face = new Vector3i(ids[0],ids[1],ids[2]);
            indices.add(face);

        }

        //build the model
        Model model = buildModel(mm,indices,vertexList,uvs.size()>0,normals.size()>0, vertexWeightData.size()>0);



        return model;
    }

    *//**
     * Load armature from a DAE model
     *//*
    private static Armature loadArmature(XmlNode rootNode, List<VertexWeightData> vertexWeightData) {
        XmlNode skinNode = rootNode.getChild("library_controllers");
        if(skinNode==null) return null;
        skinNode = skinNode.getChild("controller");
        if(skinNode==null) return null;
        skinNode = skinNode.getChild("skin");//only the first skin

        //figure out the source locations of the joints and weights
        XmlNode vWeights = skinNode.getChild("vertex_weights");
        String jointSource = null, weightSource = null;
        for(XmlNode input : vWeights.getChildren("input")) {
            if(input.getAttribute("semantic").equals("JOINT")) jointSource = input.getAttribute("source").substring(1);
            else if(input.getAttribute("semantic").equals("WEIGHT")) weightSource = input.getAttribute("source").substring(1);
        }
        if(jointSource==null && weightSource==null) return null;//not found

        //load information from sources
        Map<String, Joint> joints = new HashMap<>();
        List<Float> weights = new ArrayList<>();

        for(XmlNode source : skinNode.getChildren("source")) {
            //Load joints
            if(source.getAttribute("id").equals(jointSource)) {
                Scanner scan = new Scanner(source.getChild("Name_array").getData());
                while(scan.hasNext()) {
                    Joint joint = new Joint(scan.next(),joints.size());
                    joints.put(joint.getName(),joint);
                }
                scan.close();
            }
            //load weights
            else if(source.getAttribute("id").equals(weightSource)) {
                Scanner scan = new Scanner(source.getChild("float_array").getData());
                while(scan.hasNextFloat()) {
                    float f = scan.nextFloat();
                    weights.add(f);
                }
                scan.close();
            }
        }

        //List of all joint/weight indices in order from joint,weight,joint,weight,etc.
        List<Integer> jointWeightIndices = new ArrayList<>();
        Scanner indexScan = new Scanner(vWeights.getChild("v").getData());
        while(indexScan.hasNextInt()) {
            jointWeightIndices.add(indexScan.nextInt());
        }
        indexScan.close();

        //Add to the vertex weight data list
        Scanner weightCountScan = new Scanner(vWeights.getChild("vcount").getData());
        int pointer = 0;
        while(weightCountScan.hasNextInt()) {
            int count = weightCountScan.nextInt();
            VertexWeightData weightData = new VertexWeightData();

            for(int i = 0;i<count;i++) {
                //first is the index of the joint id, then the index of the weight
                int jointID = jointWeightIndices.get(pointer);
                pointer++;
                float weight = weights.get(jointWeightIndices.get(pointer));
                pointer++;
                weightData.addJoint(jointID);
                weightData.addWeight(weight);
            }
            weightData.limitJointNumber();
            vertexWeightData.add(weightData);
        }
        weightCountScan.close();

        //build the armature hierarchy
        XmlNode visualScene = rootNode.getChild("library_visual_scenes");
        if(visualScene == null) return null;
        visualScene = visualScene.getChild("visual_scene");
        if(visualScene==null) return null;

        XmlNode armatureNode = visualScene.getChildWithAttribute("node","id","Armature");
        XmlNode root = armatureNode.getChild("node");
        Joint rootJoint = parseJointHierarchy(root,joints);

        Armature armature = new Armature(joints,rootJoint);
        armature.calculateInverseBindTransforms();
        return armature;

    }

    *//**
     * Recursively build the joint hierarchy
     *//*
    private static Joint parseJointHierarchy(XmlNode root, Map<String,Joint> jointMap) {
        String id = root.getAttribute("id");
        if(!jointMap.containsKey(id)) return null;
        Joint joint = jointMap.get(id);

        //set the matrix
        float[] floats = new float[16];
        Scanner matrixScan = new Scanner(root.getChild("matrix").getData());
        for(int i = 0;i<16;i++) {
            floats[i] = matrixScan.nextFloat();
        }
        Matrix4f matrix = new Matrix4f(
                floats[0],floats[4],floats[8],floats[12],
                floats[1],floats[5],floats[9],floats[13],
                floats[2],floats[6],floats[10],floats[14],
                floats[3],floats[7],floats[11],floats[15]);
        joint.setLocalBindTransform(matrix);

        //add children recursively
        for(XmlNode child : root.getChildren("node")) {
            Joint j = parseJointHierarchy(child,jointMap);
            if(j!=null) joint.addChild(j);
        }

        return joint;
    }

    *//**
     * Load poses
     *
     * Each "pose" is one keyframe of the animation in the DAE file. These may not necessarily be in order or at useful timestamp values.
     * Each pose is given an ID which is not guaranteed to be in order of time.
     *//*
    private static void loadPoses(XmlNode root, AnimatedModel object) {
        XmlNode node = root.getChild("library_animations");
        if(node==null) return;

        Map<Float, Integer> timestamps = new HashMap<>();//currently used timestamps with their respective pose ids
        int id = 0;//current pose id
        //loop through all animations
        for(XmlNode animation : node.getChildren("animation")) {

            //only look for "transform" animations
            String[] split = animation.getChild("channel").getAttribute("target").split("/");
            if(split.length<2) continue;
            if(!split[1].equals("transform")) continue;

            String jointName = split[0];
            Joint joint = object.getArmature().getJoint(jointName);
            if(joint==null) {
                System.err.println("Invalid joint name: " + jointName);
                continue;
            }
            XmlNode sampler = animation.getChild("sampler");
            String inputSource = sampler.getChildWithAttribute("input","semantic","INPUT").getAttribute("source").substring(1);
            String outputSource = sampler.getChildWithAttribute("input","semantic","OUTPUT").getAttribute("source").substring(1);

            //get the timestamps
            XmlNode input = animation.getChildWithAttribute("source","id",inputSource);
            List<Pose> posesToAddTo = new ArrayList<>();//poses to add transforms to
            Scanner scan = new Scanner(input.getChild("float_array").getData());
            while(scan.hasNextFloat()) {
                float timestamp = scan.nextFloat();
                if(!timestamps.containsKey(timestamp)) {
                    timestamps.put(timestamp,id);
                    Pose pose = new Pose(id);
                    object.addPose(pose);
                    posesToAddTo.add(pose);
                    id++;
                }else{
                    posesToAddTo.add(object.getPose(timestamps.get(timestamp)));
                }
            }
            scan.close();

            //get the matrix for each timestamp
            XmlNode output =animation.getChildWithAttribute("source","id",outputSource);
            scan = new Scanner(output.getChild("float_array").getData());
            float[] floats = new float[16];
            for(Pose pose : object.getPoses()) {
                for(int i = 0;i<16;i++) {
                    floats[i] = scan.nextFloat();
                }
                Matrix4f matrix = new Matrix4f(
                        floats[0],floats[4],floats[8],floats[12],
                        floats[1],floats[5],floats[9],floats[13],
                        floats[2],floats[6],floats[10],floats[14],
                        floats[3],floats[7],floats[11],floats[15]);
                pose.addTransform(joint,matrix);
            }
            scan.close();
        }
    }*/
}
