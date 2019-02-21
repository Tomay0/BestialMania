package com.bestialMania.collision;

import org.joml.Vector3f;

import java.io.File;
import java.util.*;

public class TriangleLoader {
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
