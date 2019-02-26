package com.bestialMania.collision;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.File;
import java.util.*;

public class TriangleLoader {
    /**
     * Load a set of triangles from an obj model
     */
    public static Set<Triangle> loadTrianglesOBJ(String fileName) {
        Set<Triangle> triangles = new HashSet<>();
        try {
            Scanner scan = new Scanner(new File(fileName));

            List<Vector3f> vertices = new ArrayList<>();
            while(scan.hasNext()) {
                String head = scan.next();
                //vertices
                if(head.equals("v")) {
                    float x = scan.nextFloat();
                    float y = scan.nextFloat();
                    float z = scan.nextFloat();
                    vertices.add(new Vector3f(x,y,z));
                }
                //faces
                else if(head.equals("f")) {
                    int id1 = getVIndex(scan.next());
                    int id2 = getVIndex(scan.next());
                    int id3 = getVIndex(scan.next());

                    Triangle triangle = new Triangle(vertices.get(id1),vertices.get(id2),vertices.get(id3));
                    triangles.add(triangle);
                }
                else {
                    scan.nextLine();
                }
            }

            scan.close();
        }catch(Exception e) {
            e.printStackTrace();
        }


        return triangles;
    }

    /**
     * Load a set of triangles from an obj model with a matrix transformation
     */
    public static Set<Triangle> loadTrianglesOBJ(String fileName, Matrix4f matrix) {
        Set<Triangle> triangles = new HashSet<>();
        try {
            Scanner scan = new Scanner(new File(fileName));

            List<Vector3f> vertices = new ArrayList<>();
            while(scan.hasNext()) {
                String head = scan.next();
                //vertices
                if(head.equals("v")) {
                    float x = scan.nextFloat();
                    float y = scan.nextFloat();
                    float z = scan.nextFloat();
                    Vector4f v = new Vector4f(x,y,z,1);
                    v.mul(matrix);
                    vertices.add(new Vector3f(v.x,v.y,v.z));
                }
                //faces
                else if(head.equals("f")) {
                    int id1 = getVIndex(scan.next());
                    int id2 = getVIndex(scan.next());
                    int id3 = getVIndex(scan.next());

                    Triangle triangle = new Triangle(vertices.get(id1),vertices.get(id2),vertices.get(id3));
                    triangles.add(triangle);
                }
                else {
                    scan.nextLine();
                }
            }

            scan.close();
        }catch(Exception e) {
            e.printStackTrace();
        }


        return triangles;
    }
    private static int getVIndex(String string) {
        if(!string.contains("/")) {
            return Integer.parseInt(string)-1;
        }
        else{
            String[] split = string.split("/");
            return Integer.parseInt(split[0])-1;
        }
    }
}
