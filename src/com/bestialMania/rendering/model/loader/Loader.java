package com.bestialMania.rendering.model.loader;

import com.bestialMania.animation.*;
import com.bestialMania.MemoryManager;
import com.bestialMania.animation.AnimatedModel;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.xml.XmlNode;
import com.bestialMania.xml.XmlParser;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.File;
import java.util.*;

public class Loader {
    /**
     * Loads a model from an OBJ file format
     */
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
    /**
     * Loads an animated DAE Model
     */
    public static AnimatedModel loadAnimatedModel(MemoryManager mm, String fileName) {
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

    /**
     * Loads a regular DAE Model without armature information
     */
    public static Model loadDAEModel(MemoryManager mm, String fileName) {
        XmlNode rootNode = XmlParser.loadXmlFile(new File(fileName));

        Model model = loadMesh(mm,rootNode,new ArrayList<>());

        if(model==null) {
            System.err.println("Could not load model: " + fileName + ". Invalid format.");
            System.exit(-1);
        }

        return model;
    }

    /**
     * Loads the model from a DAE file
     */
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
                ids[i] = processVertex(string,vertexMap,vertexList,vertices,uvs,normals,vertexWeightData);
            }

            //calculate tangent vectors if uvs and normals are both present
            if(uvs.size()>0 && normals.size()>0) {
                processTangents(vertexList.get(ids[0]),vertexList.get(ids[1]),vertexList.get(ids[2]));
            }
            //face using ids of vertices
            Vector3i face = new Vector3i(ids[0],ids[1],ids[2]);
            indices.add(face);

        }

        //build the model
        Model model = buildModel(mm,indices,vertexList,uvs.size()>0,normals.size()>0, vertexWeightData.size()>0);



        return model;
    }

    /**
     * Processes a string which represents some vertex/uv/normal set on a model.
     * Adds to the vertexMap/vertexList collections if not already in them.
     */
    private static int processVertex(String string, Map<String, ModelVertex> vertexMap, List<ModelVertex> vertexList,
                                    List<Vector3f> vertices, List<Vector2f> uvs, List<Vector3f> normals, List<VertexWeightData> vertexWeightData) {
        int id;

        //vertex already exists
        if(vertexMap.containsKey(string)) {
            id = vertexMap.get(string).getID();

        }
        //create new vertex/uv/normal set
        else{
            id = vertexList.size();
            ModelVertex mv = null;

            int vIndex = 0;
            /*
             * Formats:
             * v/t/n
             * v//n
             * v/t
             * v
             */
            if(!string.contains("/")) {
                vIndex = Integer.parseInt(string)-1;
                mv = new ModelVertex(id,vertices.get(vIndex));
            }
            else if(string.contains("//")) {
                String[] split = string.split("//");
                vIndex = Integer.parseInt(split[0])-1;
                mv = new ModelVertex(id,vertices.get(vIndex),normals.get(Integer.parseInt(split[1])-1));
            }
            else{
                String[] split = string.split("/");
                vIndex = Integer.parseInt(split[0])-1;
                if(split.length==2) {
                    mv = new ModelVertex(id,vertices.get(vIndex),uvs.get(Integer.parseInt(split[1])-1));
                }
                else if(split.length==3) {
                    mv = new ModelVertex(id,vertices.get(vIndex),
                            uvs.get(Integer.parseInt(split[1])-1),
                            normals.get(Integer.parseInt(split[2])-1));
                }
            }
            //add vertex weight data if it exists
            if(vertexWeightData.size()>0) {
                mv.setVertexWeightData(vertexWeightData.get(vIndex));
            }

            vertexMap.put(string,mv);
            vertexList.add(mv);
        }

        return id;
    }

    /**
     * Calculate tangent vectors for a given face
     *
     */
    private static void processTangents(ModelVertex mv1, ModelVertex mv2, ModelVertex mv3) {
        //Ugly code that I don't really understand
        Vector3f v1 = mv1.getVertex();
        Vector3f v2 = mv2.getVertex();
        Vector3f v3 = mv3.getVertex();
        Vector2f uv1 = mv1.getUV();
        Vector2f uv2 = mv2.getUV();
        Vector2f uv3 = mv3.getUV();
        Vector3f dv1 = new Vector3f(v2.x-v1.x,v2.y-v1.y,v2.z-v1.z);
        Vector3f dv2 = new Vector3f(v3.x-v1.x,v3.y-v1.y,v3.z-v1.z);
        Vector2f duv1 = new Vector2f(uv2.x-uv1.x,uv2.y-uv1.y);
        Vector2f duv2 = new Vector2f(uv3.x-uv1.x,uv3.y-uv1.y);

        dv1.mul(duv2.y);
        dv2.mul(duv1.y);

        Vector3f tangent = new Vector3f();
        dv1.sub(dv2,tangent);
        tangent.mul(1.0f / (duv1.x * duv2.y - duv1.y * duv2.x));

        mv1.setTangent(tangent);
        mv2.setTangent(tangent);
        mv3.setTangent(tangent);
    }


    /**
     * Builds the model from the given data
     */
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
        armature.calculateInverseBindTransforms();
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
    }
}
