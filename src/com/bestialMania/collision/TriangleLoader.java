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

    /**
     * Automatically pick the right kinds of collisions from an OBJ file and load the triangles to the collision handler
     */
    public static void autoLoadTriangleCollisionsOBJ(String fileName, Matrix4f matrix, CollisionHandler collisionHandler, float wallBias) {
        try {
            Scanner scan = new Scanner(new File(fileName));

            Set<Triangle> floorTriangles = new HashSet<>();
            Set<Triangle> wallTriangles = new HashSet<>();
            Set<Triangle> ceilingTriangles = new HashSet<>();

            List<Vector3f> vertices = new ArrayList<>();
            List<Vector3f> normals = new ArrayList<>();
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
                else if(head.equals("vn")) {
                    float x = scan.nextFloat();
                    float y = scan.nextFloat();
                    float z = scan.nextFloat();
                    Vector4f v = new Vector4f(x,y,z,1);
                    v.mul(matrix);
                    normals.add(new Vector3f(v.x,v.y,v.z));
                }
                //faces
                else if(head.equals("f")) {
                    if(normals.size()==0) {
                        System.err.println("No normals present in this object: " + fileName);
                        return;
                    }
                    String s1 = scan.next();
                    String s2 = scan.next();
                    String s3 = scan.next();

                    int id1 = getVIndex(s1);
                    int id2 = getVIndex(s2);
                    int id3 = getVIndex(s3);
                    int ni1 = getNIndex(s1);
                    int ni2 = getNIndex(s2);
                    int ni3 = getNIndex(s3);

                    Vector3f n1 = normals.get(ni1);
                    Vector3f n2 = normals.get(ni2);
                    Vector3f n3 = normals.get(ni3);
                    Vector3f normalAverage = new Vector3f(n1.x+n2.x+n3.x,n1.y+n2.y+n3.y,n1.z+n2.z+n3.z);
                    normalAverage.normalize();

                    Triangle triangle = new Triangle(vertices.get(id1),vertices.get(id2),vertices.get(id3));
                    if(Math.abs(normalAverage.y)<wallBias) {
                        wallTriangles.add(triangle);
                    }else if(normalAverage.y>0) {
                        floorTriangles.add(triangle);
                    }else{
                        ceilingTriangles.add(triangle);
                    }
                }
                else {
                    scan.nextLine();
                }
            }

            collisionHandler.addWalls(wallTriangles);
            collisionHandler.addCeiling(ceilingTriangles);
            collisionHandler.addFloor(floorTriangles);

            scan.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
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
    private static int getNIndex(String string) {
        if(!string.contains("/")) {
            return 0;
        }
        else{
            String[] split = string.split("/");
            if(split.length==2) return Integer.parseInt(split[1])-1;
            else return Integer.parseInt(split[2])-1;
        }
    }
}
