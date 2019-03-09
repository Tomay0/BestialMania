package com.bestialMania.collision;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.*;
import java.util.*;

public class CollisionLoader {
    /**
     * Load collisions from a file
     */
    public static CollisionHandler loadCollisionHandler(String fileName) {

        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(inputStream);

            //get the header
            char[] chars = new char[3];
            for (int i = 0; i < 3; i++) chars[i] = ois.readChar();
            if (chars[0] != 'B' || chars[1] != 'M' || chars[2] != 'C') {
                System.err.println("Invalid format for the collisions " + fileName + " must be bmc!");
                return null;
            }

            int minX,minY,minZ,maxX,maxY,maxZ;
            minX = ois.readInt();
            minY = ois.readInt();
            minZ = ois.readInt();
            maxX = ois.readInt();
            maxY = ois.readInt();
            maxZ = ois.readInt();

            BoundingBox boundingBox = new BoundingBox(minX,minY,minZ,maxX,maxY,maxZ);
            CollisionHandler collisionHandler = new CollisionHandler(boundingBox);

            //faces
            char sectionHeader = ois.readChar();
            if(sectionHeader=='f') {
                int nTriangles = ois.readInt();
                Set<Triangle> triangles = new HashSet<>();
                for(int i = 0;i<nTriangles;i++) {
                    float x1 = ois.readFloat();
                    float y1 = ois.readFloat();
                    float z1 = ois.readFloat();
                    float x2 = ois.readFloat();
                    float y2 = ois.readFloat();
                    float z2 = ois.readFloat();
                    float x3 = ois.readFloat();
                    float y3 = ois.readFloat();
                    float z3 = ois.readFloat();

                    triangles.add(new Triangle(new Vector3f(x1,y1,z1),new Vector3f(x2,y2,z2),new Vector3f(x3,y3,z3)));
                }
                collisionHandler.addFloor(triangles);

                sectionHeader = ois.readChar();
            }
            //ceiling
            if(sectionHeader=='c') {
                int nTriangles = ois.readInt();
                Set<Triangle> triangles = new HashSet<>();
                for(int i = 0;i<nTriangles;i++) {
                    float x1 = ois.readFloat();
                    float y1 = ois.readFloat();
                    float z1 = ois.readFloat();
                    float x2 = ois.readFloat();
                    float y2 = ois.readFloat();
                    float z2 = ois.readFloat();
                    float x3 = ois.readFloat();
                    float y3 = ois.readFloat();
                    float z3 = ois.readFloat();

                    triangles.add(new Triangle(new Vector3f(x1,y1,z1),new Vector3f(x2,y2,z2),new Vector3f(x3,y3,z3)));
                }
                collisionHandler.addCeiling(triangles);

                sectionHeader = ois.readChar();
            }
            //walls
            if(sectionHeader=='w') {
                int nTriangles = ois.readInt();
                Set<Triangle> triangles = new HashSet<>();
                for(int i = 0;i<nTriangles;i++) {
                    float x1 = ois.readFloat();
                    float y1 = ois.readFloat();
                    float z1 = ois.readFloat();
                    float x2 = ois.readFloat();
                    float y2 = ois.readFloat();
                    float z2 = ois.readFloat();
                    float x3 = ois.readFloat();
                    float y3 = ois.readFloat();
                    float z3 = ois.readFloat();

                    triangles.add(new Triangle(new Vector3f(x1,y1,z1),new Vector3f(x2,y2,z2),new Vector3f(x3,y3,z3)));
                }
                collisionHandler.addWalls(triangles);

                sectionHeader = ois.readChar();
            }


            if(sectionHeader=='e') {
                System.out.println("Loaded collisions: " + fileName + " successfully!");
            }

            ois.close();
            inputStream.close();

            return collisionHandler;

        }catch(FileNotFoundException e) {
            System.err.println("Could not find the file for the collisions: " + fileName);
            return null;
        }catch(EOFException  e) {
            System.err.println("Collisions file " + fileName + " appears to be incomplete, end of file reached.");
            e.printStackTrace();
            return null;
        }catch(IOException e) {
            System.err.println("Unable to load collisions: " + fileName);
            e.printStackTrace();
            return null;
        }
    }




    private Set<Triangle> walls = new HashSet<>();
    private Set<Triangle> floors = new HashSet<>();
    private Set<Triangle> ceilings = new HashSet<>();

    private float minX=0,minY=0,minZ=0,maxX=0,maxY=0,maxZ=0;

    /**
     * Save collisions to a file
     */
    public void saveToFile(String fileName) {
        try {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);

            oos.writeChars("BMC");
            //bounding box
            oos.writeInt((int)Math.floor(minX));
            oos.writeInt((int)Math.floor(minY));
            oos.writeInt((int)Math.floor(minZ));
            oos.writeInt((int)Math.ceil(maxX));
            oos.writeInt((int)Math.ceil(maxY));
            oos.writeInt((int)Math.ceil(maxZ));

            //floors
            oos.writeChar('f');
            oos.writeInt(floors.size());
            for(Triangle triangle : floors) {
                oos.writeFloat(triangle.getV1().x);
                oos.writeFloat(triangle.getV1().y);
                oos.writeFloat(triangle.getV1().z);
                oos.writeFloat(triangle.getV2().x);
                oos.writeFloat(triangle.getV2().y);
                oos.writeFloat(triangle.getV2().z);
                oos.writeFloat(triangle.getV3().x);
                oos.writeFloat(triangle.getV3().y);
                oos.writeFloat(triangle.getV3().z);
            }
            //ceilings
            oos.writeChar('c');
            oos.writeInt(ceilings.size());
            for(Triangle triangle : ceilings) {
                oos.writeFloat(triangle.getV1().x);
                oos.writeFloat(triangle.getV1().y);
                oos.writeFloat(triangle.getV1().z);
                oos.writeFloat(triangle.getV2().x);
                oos.writeFloat(triangle.getV2().y);
                oos.writeFloat(triangle.getV2().z);
                oos.writeFloat(triangle.getV3().x);
                oos.writeFloat(triangle.getV3().y);
                oos.writeFloat(triangle.getV3().z);
            }

            //walls
            oos.writeChar('w');
            oos.writeInt(walls.size());
            for(Triangle triangle : walls) {
                oos.writeFloat(triangle.getV1().x);
                oos.writeFloat(triangle.getV1().y);
                oos.writeFloat(triangle.getV1().z);
                oos.writeFloat(triangle.getV2().x);
                oos.writeFloat(triangle.getV2().y);
                oos.writeFloat(triangle.getV2().z);
                oos.writeFloat(triangle.getV3().x);
                oos.writeFloat(triangle.getV3().y);
                oos.writeFloat(triangle.getV3().z);
            }

            oos.writeChar('e');

            oos.close();
            outputStream.close();
        }catch(Exception e) {
            System.err.println("Unable to write to file: " + fileName);
            e.printStackTrace();
        }
    }


    /**
     * add floors
     */
    public void loadFloors(String fileName, Matrix4f matrix) {
        floors.addAll(getTrianglesOBJ(fileName,matrix));
    }
    /**
     * add ceilings
     */
    public void loadCeilings(String fileName, Matrix4f matrix) {
        ceilings.addAll(getTrianglesOBJ(fileName,matrix));
    }
    /**
     * add walls
     */
    public void loadWalls(String fileName, Matrix4f matrix) {
        walls.addAll(getTrianglesOBJ(fileName,matrix));
    }

    /**
     * Load a set of triangles from an obj model with a matrix transformation
     */
    private Set<Triangle> getTrianglesOBJ(String fileName, Matrix4f matrix) {
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
                    if(x<minX) minX=x;
                    if(y<minY) minY=y;
                    if(z<minZ) minZ=z;
                    if(x>maxX) maxX=x;
                    if(y>maxY) maxY=y;
                    if(z>maxZ) maxZ=z;
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
    public void autoLoadTriangleCollisionsOBJ(String fileName, Matrix4f matrix, float wallBias) {
        try {
            Scanner scan = new Scanner(new File(fileName));

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
                    if(x<minX) minX=x;
                    if(y<minY) minY=y;
                    if(z<minZ) minZ=z;
                    if(x>maxX) maxX=x;
                    if(y>maxY) maxY=y;
                    if(z>maxZ) maxZ=z;
                }
                else if(head.equals("vn")) {
                    float x = scan.nextFloat();
                    float y = scan.nextFloat();
                    float z = scan.nextFloat();
                    Vector4f v = new Vector4f(x,y,z,0);
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
                        walls.add(triangle);
                    }else if(normalAverage.y>0) {
                        floors.add(triangle);
                    }else{
                        ceilings.add(triangle);
                    }
                }
                else {
                    scan.nextLine();
                }
            }

            scan.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    private int getVIndex(String string) {
        if(!string.contains("/")) {
            return Integer.parseInt(string)-1;
        }
        else{
            String[] split = string.split("/");
            return Integer.parseInt(split[0])-1;
        }
    }
    private int getNIndex(String string) {
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
