package com.bestialMania.rendering.model;

import com.bestialMania.rendering.MemoryManager;
import com.bestialMania.rendering.model.armature.Armature;
import com.bestialMania.rendering.model.armature.Joint;
import com.bestialMania.xml.XmlNode;
import com.bestialMania.xml.XmlParser;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.File;
import java.util.*;

public class DAELoader {
    /**
     * TODO Loads a model from a DAE format
     */
    public static Model loadDAEModel(MemoryManager mm, String fileName) {
        XmlNode rootNode = XmlParser.loadXmlFile(new File(fileName));

        List<VertexWeightData> vertexWeightData = new ArrayList<>();
        Armature armature = loadArmature(rootNode, vertexWeightData);

        Model model = loadMesh(mm,rootNode,vertexWeightData);

        if(model==null) {
            System.err.println("Could not load model: " + fileName + ". Invalid format.");
            System.exit(-1);
        }

        return model;
    }

    /**
     * Load armature from a DAE model
     */
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
        Map<String,Joint> joints = new HashMap<>();
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
        return armature;

    }

    /**
     * Recursively build the joint hierarchy
     */
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
                floats[0],floats[1],floats[2],floats[3],
                floats[4],floats[5],floats[6],floats[7],
                floats[8],floats[9],floats[10],floats[11],
                floats[12],floats[13],floats[14],floats[15]);
        joint.setMatrix(matrix);

        //add children recursively
        for(XmlNode child : root.getChildren("node")) {
            Joint j = parseJointHierarchy(child,jointMap);
            if(j!=null) joint.addChild(j);
        }

        return joint;
    }

    public static Model loadMesh(MemoryManager mm, XmlNode rootNode, List<VertexWeightData> vertexWeightData){
        XmlNode meshNode = rootNode.getChild("library_geometries");
        if(meshNode==null) return null;
        meshNode = meshNode.getChild("geometry");
        if(meshNode==null) return null;
        meshNode = meshNode.getChild("mesh");
        if(meshNode==null) return null;

        //get the sources of the texture coords and normals
        XmlNode polyList = meshNode.getChild("polylist");

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
        Map<String,ModelVertex> vertexMap = new HashMap<>();//map of vertex/uv/normals sets by a string to identify them
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
                ids[i] = OBJLoader.processVertex(string,vertexMap,vertexList,vertices,uvs,normals);
            }

            //calculate tangent vectors if uvs and normals are both present
            if(uvs.size()>0 && normals.size()>0) {
                OBJLoader.processTangents(vertexList.get(ids[0]),vertexList.get(ids[1]),vertexList.get(ids[2]));
            }
            //face using ids of vertices
            Vector3i face = new Vector3i(ids[0],ids[1],ids[2]);
            indices.add(face);

        }

        //build the model
        Model model = OBJLoader.buildModel(mm,indices,vertexList,uvs.size()>0,normals.size()>0);



        return model;
    }
}
