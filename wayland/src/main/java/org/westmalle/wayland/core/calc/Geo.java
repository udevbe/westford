package org.westmalle.wayland.core.calc;

import org.westmalle.wayland.core.Point;
import org.westmalle.wayland.core.Rectangle;
import org.westmalle.wayland.core.Region;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Geo {

    @Inject
    Geo() {
    }

    /**
     * Given: A line between between 2 points, one inside the region, the other outside.
     * Finds the point closest to the outside point, clamped by the given region, between the inside & outside point.
     *
     * @param inside  point inside the region
     * @param outside point outside the region
     * @param region  the clamp region
     *
     * @return a clamped point
     */
    public Point clamp(final Point inside,
                       final Point outside,
                       final Region region) {

        if (region.contains(outside)) {
            //outside is actually inside, so no need to clamp it.
            return outside;
        }

        final int outsideX = outside.getX();
        final int outsideY = outside.getY();

        int insideX = inside.getX();
        int insideY = inside.getY();

        final int rectWidth;
        if (outsideX > insideX) {
            rectWidth = outsideX - insideX;
            //compensate for libpixman considering edge points not being inside, so we move the (potential) edge point a bit inside
            insideX--;
        }
        else {
            rectWidth = insideX - outsideX;
            insideX++;
        }

        final int rectHeight;

        if (outsideY > insideY) {
            rectHeight = outsideY - insideY;
            //compensate for libpixman considering edge points not being inside, so we move the (potential) edge point a bit inside
            insideY--;
        }
        else {
            rectHeight = insideY - outsideY;
            insideY++;
        }

        final int rectX = outsideX > insideX ? insideX : outsideX;
        final int rectY = outsideY > insideY ? insideY : outsideY;

        //we moved a bit more inside, so increase the width & height
        final Region intersect = region.intersect(Rectangle.create(rectX,
                                                                   rectY,
                                                                   rectWidth + 1,
                                                                   rectHeight + 1));
        List<Rectangle> intersectionRects = intersect.asList();
        if (intersectionRects.isEmpty()) {
            //Both points fall completely outside the region. make the entire clamp region the intersection.
            //this way the closest corner point of the clamp region will be returned. Not perfect, but good enough.
            intersectionRects = region.asList();
        }

        final Set<Point> points = new HashSet<>();
        for (final Rectangle intersectRect : intersectionRects) {
            points.add(Point.create(intersectRect.getX(),
                                    intersectRect.getY()));
            points.add(Point.create(intersectRect.getX() + intersectRect.getWidth(),
                                    intersectRect.getY()));
            points.add(Point.create(intersectRect.getX() + intersectRect.getWidth(),
                                    intersectRect.getY() + intersectRect.getHeight()));
            points.add(Point.create(intersectRect.getX(),
                                    intersectRect.getY() + intersectRect.getHeight()));
        }

        Point  clampPoint    = inside;
        double clampDistance = 65535;

        for (final Point point : points) {
            final double distance = distance(outside,
                                             point);
            if (distance < clampDistance) {
                clampDistance = distance;
                clampPoint = point;
            }
        }

        return clampPoint;
    }

    public double distance(final Point a,
                           final Point b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(),
                                  2) + Math.pow(a.getY() - b.getY(),
                                                2));
    }
}
