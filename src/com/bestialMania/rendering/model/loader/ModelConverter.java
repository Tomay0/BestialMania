package com.bestialMania.rendering.model.loader;

import com.bestialMania.MemoryManager;
import com.bestialMania.animation.*;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.xml.XmlNode;
import com.bestialMania.xml.XmlParser;
import org.joml.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class ModelConverter {



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

            oos.writeChars("BMM");

            XmlNode rootNode = XmlParser.loadXmlFile(new File(daeFile));
            List<VertexWeightData> vertexWeightData = new ArrayList<>();

            Armature armature = loadArmature(oos,rootNode,vertexWeightData);
            if(armature==null) {
                System.err.println("Could not load armature information for " + daeFile);
                return;
            }
            loadMesh(oos,rootNode,vertexWeightData);
            loadPoses(oos,rootNode,armature);

            oos.writeChar('e');

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
        buildModel(oos,indices,vertexList,uvs.size()>0,normals.size()>0, vertexWeightData.size()>0);
        return true;
    }

    /**
     * Load armature from a DAE model
     */
    private static Armature loadArmature(ObjectOutputStream oos, XmlNode rootNode, List<VertexWeightData> vertexWeightData) throws IOException {
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

        //write to the file
        //joints
        oos.writeChar('j');
        int nJoints = armature.size();
        oos.writeInt(nJoints);
        for(int i = 0;i<nJoints;i++) {
            //each joint
            Joint joint = armature.getJoint(i);
            oos.writeInt(joint.getName().length());
            oos.writeChars(joint.getName());
            Matrix4f inverseBindTransform = joint.getInverseBindTransform();
            float[] matrixArray = new float[16];
            inverseBindTransform.get(matrixArray);
            for(float f : matrixArray) {
                oos.writeFloat(f);
            }
        }
        //hierarchy
        oos.writeInt(rootJoint.getId());
        oos.writeInt(-1);
        writeJointHierarchy(oos,armature.getRootJoint());
        oos.writeInt(-2);


        return armature;
    }

    /**
     * Recursively write to a BMM file the hierarchy structure
     */
    private static void writeJointHierarchy(ObjectOutputStream oos, Joint joint) throws IOException {
        for(Joint j2 : joint) {
            oos.writeInt(j2.getId());
            if(j2.nChildren()>0) {
                oos.writeInt(-1);
                writeJointHierarchy(oos,j2);
                oos.writeInt(-2);
            }
        }
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
    private static void loadPoses(ObjectOutputStream oos, XmlNode root, Armature armature) throws IOException{
        XmlNode node = root.getChild("library_animations");
        if(node==null) return;

        Map<Float, Integer> timestamps = new HashMap<>();//currently used timestamps with their respective pose ids
        List<Pose> poses = new ArrayList<>();

        int id = 0;//current pose id
        //loop through all animations
        for(XmlNode animation : node.getChildren("animation")) {

            //only look for "transform" animations
            String[] split = animation.getChild("channel").getAttribute("target").split("/");
            if(split.length<2) continue;
            if(!split[1].equals("transform")) continue;

            String jointName = split[0];
            Joint joint = armature.getJoint(jointName);
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
                    poses.add(pose);
                    posesToAddTo.add(pose);
                    id++;
                }else{
                    posesToAddTo.add(poses.get(timestamps.get(timestamp)));
                }
            }
            scan.close();

            //get the matrix for each timestamp
            XmlNode output =animation.getChildWithAttribute("source","id",outputSource);
            scan = new Scanner(output.getChild("float_array").getData());
            float[] floats = new float[16];
            for(Pose pose : poses) {
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

        //save all poses to the file

        oos.writeChar('p');
        oos.writeInt(poses.size());
        for(Pose pose : poses) {
            //for each pose
            int nJoints = pose.getJointTransforms().size();
            oos.writeInt(nJoints);
            for(JointTransform jointTransform : pose.getJointTransforms()) {
                oos.writeInt(jointTransform.getJoint().getId());
                //position
                Vector3f pos = jointTransform.getPosition();
                oos.writeFloat(pos.x);
                oos.writeFloat(pos.y);
                oos.writeFloat(pos.z);
                //rotation
                Vector4f rot = jointTransform.getRotation();
                oos.writeFloat(rot.x);
                oos.writeFloat(rot.y);
                oos.writeFloat(rot.z);
                oos.writeFloat(rot.w);
                //matrix
                float[] matrixArray = new float[16];
                jointTransform.getMatrix().get(matrixArray);
                for(float f : matrixArray) oos.writeFloat(f);
            }
        }
    }

    /*

    SHARED METHODS

     */

    /**
     * Processes a string which represents some vertex/uv/normal set on a model.
     * Adds to the vertexMap/vertexList collections if not already in them.
     */
    public static int processVertex(String string, Map<String, ModelVertex> vertexMap, List<ModelVertex> vertexList,
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
    public static void processTangents(ModelVertex mv1, ModelVertex mv2, ModelVertex mv3) {
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
}
