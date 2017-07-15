package com.peter.climb;

import com.peter.Climb.Msgs;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MockGymData {

  public static void Main(String args[]) {
    try {
      FileWriter writer = new FileWriter(new File("gyms-java"));
      Msgs.Gyms gyms = fakeGymData();
      String string = gyms.toString();
      writer.write(string);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Msgs.Gyms fakeGymData() {
    return Msgs.Gyms.newBuilder().addGyms(
        Msgs.Gym.newBuilder().setName("Ascend PGH").addFloors(
            Msgs.Floor.newBuilder().addWalls(
                Msgs.Wall.newBuilder().setPolygon(
                    Msgs.Polygon.newBuilder().addPoints(
                        Msgs.Point2D.newBuilder().setX(0).setY(0)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(10).setY(0)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(15).setY(5)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(10).setY(10)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(0).setY(14)
                    ).setColor("#FFC107")
                ).addRoutes(
                    Msgs.Route.newBuilder().setName("Lappnor Project").setPosition(
                        Msgs.Point2D.newBuilder().setX(2).setY(2)
                    ).setGrade(17).setColor("#FFFF00")
                ).addRoutes(
                    Msgs.Route.newBuilder().setName("La Dura Dura").setPosition(
                        Msgs.Point2D.newBuilder().setX(13).setY(25)
                    ).setGrade(16).setColor("#FFFFFF")
                ).setName("The Dawn Wall")
            ).addWalls(
                Msgs.Wall.newBuilder().setPolygon(
                    Msgs.Polygon.newBuilder().addPoints(
                        Msgs.Point2D.newBuilder().setX(0).setY(30)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(5).setY(30)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(6).setY(35)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(4).setY(60)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(0).setY(67)
                    ).setColor("#9C27B0")
                ).addRoutes(
                    Msgs.Route.newBuilder().setName("Pikachu").setPosition(
                        Msgs.Point2D.newBuilder().setX(5).setY(32)
                    ).setGrade(7).setColor("#ff0000")
                ).addRoutes(
                    Msgs.Route.newBuilder().setName("Magikarp").setPosition(
                        Msgs.Point2D.newBuilder().setX(4).setY(38)
                    ).setGrade(10).setColor("#00ff00")
                ).setName("Slab")
            ).setWidth(25).setHeight(70)
        ).setLargeIconUrl(
            "https://www.ascendpgh.com/sites/all/themes/ascend_foundation/images/Ascend-Mobile-Logo.png"
        )
    ).build();
  }

}
