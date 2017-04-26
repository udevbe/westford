package org.westford.compositor.core.calc

import org.westford.compositor.core.Point
import org.westford.compositor.core.Rectangle
import org.westford.compositor.core.Region

import javax.inject.Inject
import java.util.HashSet

/*
 * Westford Wayland Compositor.
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
class Geo @Inject
internal constructor() {

    /**
     * Given: A line between between 2 points, one inside the region, the other outside.
     * Finds the point closest to the outside point, clamped by the given region, between the inside & outside point.

     * @param source point inside the region
     * *
     * @param target point outside the region
     * *
     * @param region the clamp region
     * *
     * *
     * @return a clamped point
     */
    fun clamp(source: Point,
              target: Point,
              region: Region): Point {

        if (region.contains(target)) {
            //target is inside, so no need to clamp it.
            return target
        }

        val targetX = target.x
        val targetY = target.y

        var sourceX = source.x
        var sourceY = source.y

        val rectWidth: Int
        if (targetX > sourceX) {
            //compensate for libpixman considering edge points not being inside, so we move the (potential) edge point a bit inside
            sourceX--
            rectWidth = targetX - sourceX
        } else {
            sourceX++
            rectWidth = sourceX - targetX
        }

        val rectHeight: Int

        if (targetY > sourceY) {
            //compensate for libpixman considering edge points not being inside, so we move the (potential) edge point a bit inside
            sourceY--
            rectHeight = targetY - sourceY
        } else {
            sourceY++
            rectHeight = sourceY - targetY
        }

        val rectX = if (targetX > sourceX) sourceX else targetX
        val rectY = if (targetY > sourceY) sourceY else targetY

        val intersect = region.intersect(Rectangle.create(rectX,
                rectY,
                rectWidth,
                rectHeight))
        var intersectionRects = intersect.asList()
        if (intersectionRects.isEmpty()) {
            //Both points fall completely outside the region. make the entire clamp region the intersection.
            //this way the closest corner point of the clamp region will be returned. Not perfect, but good enough.
            intersectionRects = region.asList()
        }

        val points = HashSet<Point>()
        for (intersectRect in intersectionRects) {
            points.add(Point.create(intersectRect.x,
                    intersectRect.y))
            points.add(Point.create(intersectRect.x + intersectRect.width,
                    intersectRect.y))
            points.add(Point.create(intersectRect.x + intersectRect.width,
                    intersectRect.y + intersectRect.height))
            points.add(Point.create(intersectRect.x,
                    intersectRect.y + intersectRect.height))
        }

        var clampPoint = source
        var clampDistance = 65535.0

        for (point in points) {
            val distance = distance(target,
                    point)
            if (distance < clampDistance) {
                clampDistance = distance
                clampPoint = point
            }
        }

        //compensate for libpixman return rectangles whose edge points fall outside the clamp region
        return compensateEdge(clampPoint,
                sourceX,
                sourceY,
                targetX,
                targetY)
    }

    fun distance(a: Point,
                 b: Point): Double {
        return Math.sqrt(Math.pow((a.x - b.x).toDouble(),
                2.0) + Math.pow((a.y - b.y).toDouble(),
                2.0))
    }

    private fun compensateEdge(clampPoint: Point,
                               sourceX: Int,
                               sourceY: Int,
                               targetX: Int,
                               targetY: Int): Point {
        val builder = clampPoint.toBuilder()

        //direction of the vector
        val right = targetX > sourceX
        val bottom = targetY > sourceY

        if (clampPoint.y == targetY) {
            //left-right edge

            if (right) {
                //right edge
                builder.x(clampPoint.x - 1)
            }
        } else if (clampPoint.x == targetX) {
            //top-bottom edge

            if (bottom) {
                //bottom edge
                builder.y(clampPoint.y - 1)
            }
        } else if (clampPoint.x != targetX && clampPoint.y != targetY) {
            //corner point

            if (right && bottom) {
                //bottom-right corner
                builder.x(clampPoint.x - 1)
                builder.y(clampPoint.y - 1)
            } else if (right) {
                //top-right corner
                builder.x(clampPoint.x - 1)
            } else if (bottom) {
                //bottom-left corner
                builder.y(clampPoint.y - 1)
            }
        }

        return builder.build()
    }
}
