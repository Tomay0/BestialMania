package com.bestialMania.rendering.model.loader;

import com.bestialMania.MemoryManager;
import com.bestialMania.animation.AnimatedModel;
import com.bestialMania.animation.Armature;
import com.bestialMania.animation.Joint;
import com.bestialMania.animation.Pose;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.xml.XmlNode;
import com.bestialMania.xml.XmlParser;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class ModelConverter {
    /**
     * Appends the mesh data to the file
     */
    private static void buildModel(ObjectOutputStream oos, List<Vector3i> indices, List<ModelVertex> vertexList, boolean hasUvs, boolean hasNormals, boolean hasArmature) throws IOException {
        oos.writeChar('i');
        oos.writeInt(indices.size()*3);
        //write indices
        for(int i = 0;i<indices.size();i++) {
            Vector3i v = indices.get(i);
            oos.writeInt(v.x);
            oos.writeInt(v.y);
            oos.writeInt(v.z);
        }

        /*
        WRITE ALL ATTRIBUTES
         */
        oos.writeChar('v');
        int nAttributes = 1;
        if(hasUvs) nAttributes++;
        if(hasNormals) nAttributes++;
        if(hasUvs&&hasNormals) nAttributes++;
        if(hasArmature) nAttributes+=2;
        oos.writeInt(nAttributes);
        oos.writeInt(vertexList.size());

        //build vertices
        oos.writeInt(0);
        oos.writeInt(3);
        oos.writeBoolean(true);
        for(int i = 0;i<vertexList.size();i++) {
            ModelVertex v = vertexList.get(i);
            oos.writeFloat(v.getVertex().x);
            oos.writeFloat(v.getVertex().y);
            oos.writeFloat(v.getVertex().z);
        }

        //build uvs
        if(hasUvs) {
            //build uvs
            oos.writeInt(1);
            oos.writeInt(2);
            oos.writeBoolean(true);
            for(int i = 0;i<vertexList.size();i++) {
                ModelVertex v = vertexList.get(i);
                oos.writeFloat(v.getUV().x);
                oos.writeFloat(v.getUV().y);
            }
        }

        //build normals
        if(hasNormals) {
            oos.writeInt(2);
            oos.writeInt(3);
            oos.writeBoolean(true);
            for(int i = 0;i<vertexList.size();i++) {
                ModelVertex v = vertexList.get(i);
                oos.writeFloat(v.getNormal().x);
                oos.writeFloat(v.getNormal().y);
                oos.writeFloat(v.getNormal().z);
            }
        }

        //tangents
        if(hasUvs && hasNormals) {
            oos.writeInt(3);
            oos.writeInt(3);
            oos.writeBoolean(true);
            for(int i = 0;i<vertexList.size();i++) {
                ModelVertex v = vertexList.get(i);
                oos.writeFloat(v.getTangent().x);
                oos.writeFloat(v.getTangent().y);
                oos.writeFloat(v.getTangent().z);
            }
        }

        //vertex weight data
        if(hasArmature) {
            oos.writeInt(4);
            oos.writeInt(3);
            oos.writeBoolean(false);
            for(int i = 0;i<vertexList.size();i++) {
                ModelVertex v = vertexList.get(i);
                oos.writeInt(v.getVertexWeightData().getJointId(0));
                oos.writeInt(v.getVertexWeightData().getJointId(1));
                oos.writeInt(v.getVertexWeightData().getJointId(2));
            }

            oos.writeInt(5);
            oos.writeInt(3);
            oos.writeBoolean(true);
            for(int i = 0;i<vertexList.size();i++) {
                ModelVertex v = vertexList.get(i);
                oos.writeFloat(v.getVertexWeightData().getWeight(0));
                oos.writeFloat(v.getVertexWeightData().getWeight(1));
                oos.writeFloat(v.getVertexWeightData().getWeight(2));
            }
        }


    }


    /*

    OBJ

     */



    /**
     * Loads a model from an OBJ file format
     */
    public static void convertOBJ(String objFile, String bmmFile) {
        try {
            Scanner scan = new Scanner(new File(objFile));

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
                    int id1 = ModelLoader.processVertex(scan.next(),vertexMap,vertexList,vertices,uvs,normals,vertexWeightData);
                    int id2 = ModelLoader.processVertex(scan.next(),vertexMap,vertexList,vertices,uvs,normals,vertexWeightData);
                    int id3 = ModelLoader.processVertex(scan.next(),vertexMap,vertexList,vertices,uvs,normals,vertexWeightData);

                    //calculate tangent vectors if uvs and normals are both present
                    if(uvs.size()>0 && normals.size()>0) {
                        ModelLoader.processTangents(vertexList.get(id1),vertexList.get(id2),vertexList.get(id3));
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

            try {
                FileOutputStream outputStream = new FileOutputStream(bmmFile);
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeChars("BMM");
                //build the model
                buildModel(oos,indices,vertexList,uvs.size()>0,normals.size()>0,false);
                oos.writeChar('e');

                oos.close();
                outputStream.close();
            }catch(IOException e) {
                System.err.println("Unable to save model: " + bmmFile);
                e.printStackTrace();

            }

        }catch(Exception e) {
            System.err.println("Unable to load model: " + objFile);
            e.printStackTrace();
        }
    }


    /*

    DAE

     */


    /**
     * Loads an animated DAE Model
     */
    public static void convertDAE(String daeFile, String bmmFile) {
        try {
            FileOutputStream outputStream = new FileOutputStream(bmmFile);
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);

            oos.writeChars("BMMa");

            XmlNode rootNode = XmlParser.loadXmlFile(new File(daeFile));
            List<VertexWeightData> vertexWeightData = new ArrayList<>();

            if(!loadArmature(oos, rootNode, vertexWeightData)) {
                System.err.println("Could not load armature information for " + daeFile);
                return;
            }
            loadMesh(oos,rootNode,vertexWeightData);
            loadPoses(oos,rootNode,null);

            oos.close();
            outputStream.close();

        }catch(Exception e) {
            System.err.println("Unable to load model: " + daeFile);
            e.printStackTrace();
        }
    }


    /**
     * Loads the model from a DAE file
     */
    private static boolean loadMesh(ObjectOutputStream oos, XmlNode rootNode, List<VertexWeightData> vertexWeightData) throws IOException{
        XmlNode meshNode = rootNode.getChild("library_geometries");
        if(meshNode==null) return false;
        meshNode = meshNode.getChild("geometry");
        if(meshNode==null) return false;
        meshNode = meshNode.getChild("mesh");
        if(meshNode==null) return false;

        //get the sources of the texture coords and normals
        XmlNode polyList = meshNode.getChild("polylist");
        if(polyList==null) polyList = meshNode.getChild("triangles");
        if(polyList==null) return false;

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
        buildModel(oos,indices,vertexList,uvs.size()>0,normals.size()>0, vertexWeightData.size()>0);
        return true;
    }

    /**
     * Load armature from a DAE model
     */
    private static boolean loadArmature(ObjectOutputStream oos, XmlNode rootNode, List<VertexWeightData> vertexWeightData) throws IOException {
        XmlNode skinNode = rootNode.getChild("library_controllers");
        if(skinNode==null) return false;
        skinNode = skinNode.getChild("controller");
        if(skinNode==null) return false;
        skinNode = skinNode.getChild("skin");//only the first skin

        //figure out the source locations of the joints and weights
        XmlNode vWeights = skinNode.getChild("vertex_weights");
        String jointSource = null, weightSource = null;
        for(XmlNode input : vWeights.getChildren("input")) {
            if(input.getAttribute("semantic").equals("JOINT")) jointSource = input.getAttribute("source").substring(1);
            else if(input.getAttribute("semantic").equals("WEIGHT")) weightSource = input.getAttribute("source").substring(1);
        }
        if(jointSource==null && weightSource==null) return false;//not found

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
        if(visualScene == null) return false;
        visualScene = visualScene.getChild("visual_scene");
        if(visualScene==null) return false;

        XmlNode armatureNode = visualScene.getChildWithAttribute("node","id","Armature");
        XmlNode root = armatureNode.getChild("node");
        Joint rootJoint = parseJointHierarchy(root,joints);

        Armature armature = new Armature(joints,rootJoint);
        armature.calculateInverseBindTransforms();
        return true;

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

    /**
     * Load poses
     *
     * Each "pose" is one keyframe of the animation in the DAE file. These may not necessarily be in order or at useful timestamp values.
     * Each pose is given an ID which is not guaranteed to be in order of time.
     */
    private static void loadPoses(ObjectOutputStream oos, XmlNode root, AnimatedModel object) throws IOException{
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
    }
}
