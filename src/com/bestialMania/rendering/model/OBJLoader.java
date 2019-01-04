package com.bestialMania.rendering.model;

import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.rendering.MemoryManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OBJLoader {
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

            Map<String,ModelVertex> vertexMap = new HashMap<>();//map of vertex/uv/normals sets by a string to identify them
            List<ModelVertex> vertexList = new ArrayList<>();//list of the vertex/uv/normal sets in order

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
                    int id1 = processVertex(scan.next(),vertexMap,vertexList,vertices,uvs,normals);
                    int id2 = processVertex(scan.next(),vertexMap,vertexList,vertices,uvs,normals);
                    int id3 = processVertex(scan.next(),vertexMap,vertexList,vertices,uvs,normals);

                    //face using ids of vertices
                    Vector3i face = new Vector3i(id1,id2,id3);
                    indices.add(face);
                }
                else {
                    scan.nextLine();
                }
            }

            //build the model
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
            if(uvs.size()>0) {
                float[] newUVs = new float[vertexList.size()*2];
                for(int i = 0;i<vertexList.size();i++) {
                    ModelVertex v = vertexList.get(i);
                    newUVs[i*2] = v.getUV().x;
                    newUVs[i*2+1] = v.getUV().y;
                }
                model.genFloatAttribute(1,2, newUVs);

            }

            //build normals
            if(normals.size()>0) {
                float[] newNormals = new float[vertexList.size() * 3];
                for(int i = 0;i<vertexList.size();i++) {
                    ModelVertex v = vertexList.get(i);
                    newNormals[i*3] = v.getNormal().x;
                    newNormals[i*3+1] = v.getNormal().y;
                    newNormals[i*3+2] = v.getNormal().z;
                }
                model.genFloatAttribute(2,3, newNormals);
            }

            scan.close();


            return model;
        }catch(IOException e) {
            System.out.println("Unable to load model: " + file);
            System.exit(-1);
        }

        return null;
    }

    /**
     * Processes a string which represents some vertex/uv/normal set on a model.
     * Adds to the vertexMap/vertexList collections if not already in them.
     */
    private static int processVertex(String string, Map<String, ModelVertex> vertexMap, List<ModelVertex> vertexList,
                                      List<Vector3f> vertices, List<Vector2f> uvs, List<Vector3f> normals) {
        int id;

        //vertex already exists
        if(vertexMap.containsKey(string)) {
            id = vertexMap.get(string).getID();

        }
        //create new vertex/uv/normal set
        else{
            id = vertexList.size();
            ModelVertex mv = null;

            /*
             * TODO add more error catching incase one of these patterns isn't present
             * Formats:
             * v/t/n
             * v//n
             * v/t
             * v
             */
            if(!string.contains("/")) {
                mv = new ModelVertex(id,vertices.get(Integer.parseInt(string)-1));
            }
            else if(string.contains("//")) {
                String[] split = string.split("//");
                mv = new ModelVertex(id,vertices.get(Integer.parseInt(split[0])-1),normals.get(Integer.parseInt(split[1])-1));
            }
            else{
                String[] split = string.split("/");
                if(split.length==2) {
                    mv = new ModelVertex(id,vertices.get(Integer.parseInt(split[0])-1),uvs.get(Integer.parseInt(split[1])-1));
                }
                else if(split.length==3) {
                    mv = new ModelVertex(id,vertices.get(Integer.parseInt(split[0])-1),uvs.get(Integer.parseInt(split[1])-1),normals.get(Integer.parseInt(split[2])-1));
                }
            }

            vertexMap.put(string,mv);
            vertexList.add(mv);
        }

        return id;
    }

}
