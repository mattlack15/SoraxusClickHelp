package com.soraxus.clickhelp.util;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class CuboidRegion {
    private Vector pos1;
    private Vector pos2;
    private World world;

    public CuboidRegion(Location pos1, Location pos2) {
        this(pos1.getWorld(), new Vector(pos1.getX(), pos1.getY(), pos1.getZ()), new Vector(pos2.getX(), pos2.getY(), pos2.getZ()));
    }

    public CuboidRegion(World world, Vector pos1, Vector pos2) {
        Preconditions.checkNotNull(pos1);
        Preconditions.checkNotNull(pos2);
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.world = world;
        this.recalculate();
    }

    public Vector getPos1() {
        return this.pos1;
    }

    public void setPos1(Vector pos1) {
        this.pos1 = pos1;
    }

    public Vector getPos2() {
        return this.pos2;
    }

    public void setPos2(Vector pos2) {
        this.pos2 = pos2;
    }

    public World getWorld(){
        return this.world;
    }

    public Vector getCenter() {
        return this.getMinimumPoint().add(this.getMaximumPoint()).divide(2);
    }

    public int getArea() {
        Vector min = this.getMinimumPoint();
        Vector max = this.getMaximumPoint();
        return (int)((max.getX() - min.getX() + 1.0D) * (max.getY() - min.getY() + 1.0D) * (max.getZ() - min.getZ() + 1.0D));
    }

    public int getWidth() {
        Vector min = this.getMinimumPoint();
        Vector max = this.getMaximumPoint();
        return (int)(max.getX() - min.getX() + 1.0D);
    }

    public int getHeight() {
        Vector min = this.getMinimumPoint();
        Vector max = this.getMaximumPoint();
        return (int)(max.getY() - min.getY() + 1.0D);
    }

    public int getLength() {
        Vector min = this.getMinimumPoint();
        Vector max = this.getMaximumPoint();
        return (int)(max.getZ() - min.getZ() + 1.0D);
    }

    private void recalculate() {
        this.pos1 = this.pos1.clampY(0, this.world == null ? 255 : this.world.getMaxHeight());
        this.pos2 = this.pos2.clampY(0, this.world == null ? 255 : this.world.getMaxHeight());
    }

    public Vector getMinimumPoint() {
        return new Vector(Math.min(this.pos1.getX(), this.pos2.getX()), Math.min(this.pos1.getY(), this.pos2.getY()), Math.min(this.pos1.getZ(), this.pos2.getZ()));
    }

    public Vector getMaximumPoint() {
        return new Vector(Math.max(this.pos1.getX(), this.pos2.getX()), Math.max(this.pos1.getY(), this.pos2.getY()), Math.max(this.pos1.getZ(), this.pos2.getZ()));
    }

    public int getMinimumY() {
        return Math.min(this.pos1.getBlockY(), this.pos2.getBlockY());
    }

    public int getMaximumY() {
        return Math.max(this.pos1.getBlockY(), this.pos2.getBlockY());
    }

    public boolean intersectsChunk(int x, int z){
        Vector max = getMaximumPoint();
        Vector min = getMinimumPoint();
        int minI = min.getBlockX() >> 4;
        int minJ = min.getBlockZ() >> 4;
        int maxI = max.getBlockX() >> 4;
        int maxJ = max.getBlockZ() >> 4;

        //If the no-unload region contains this chunk
        return x >= minI && x <= maxI && z >= minJ && z <= maxJ;
    }

    public void expand(Vector... changes) {
        Preconditions.checkNotNull(changes);
        Vector[] var2 = changes;
        int var3 = changes.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Vector change = var2[var4];
            if (change.getX() > 0.0D) {
                if (Math.max(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
                    this.pos1 = this.pos1.add(new Vector(change.getX(), 0.0D, 0.0D));
                } else {
                    this.pos2 = this.pos2.add(new Vector(change.getX(), 0.0D, 0.0D));
                }
            } else if (Math.min(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
                this.pos1 = this.pos1.add(new Vector(change.getX(), 0.0D, 0.0D));
            } else {
                this.pos2 = this.pos2.add(new Vector(change.getX(), 0.0D, 0.0D));
            }

            if (change.getY() > 0.0D) {
                if (Math.max(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
                    this.pos1 = this.pos1.add(new Vector(0.0D, change.getY(), 0.0D));
                } else {
                    this.pos2 = this.pos2.add(new Vector(0.0D, change.getY(), 0.0D));
                }
            } else if (Math.min(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
                this.pos1 = this.pos1.add(new Vector(0.0D, change.getY(), 0.0D));
            } else {
                this.pos2 = this.pos2.add(new Vector(0.0D, change.getY(), 0.0D));
            }

            if (change.getZ() > 0.0D) {
                if (Math.max(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
                    this.pos1 = this.pos1.add(new Vector(0.0D, 0.0D, change.getZ()));
                } else {
                    this.pos2 = this.pos2.add(new Vector(0.0D, 0.0D, change.getZ()));
                }
            } else if (Math.min(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
                this.pos1 = this.pos1.add(new Vector(0.0D, 0.0D, change.getZ()));
            } else {
                this.pos2 = this.pos2.add(new Vector(0.0D, 0.0D, change.getZ()));
            }
        }

        this.recalculate();
    }

    public void contract(Vector... changes) {
        Preconditions.checkNotNull(changes);
        Vector[] var2 = changes;
        int var3 = changes.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Vector change = var2[var4];
            if (change.getX() < 0.0D) {
                if (Math.max(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
                    this.pos1 = this.pos1.add(new Vector(change.getX(), 0.0D, 0.0D));
                } else {
                    this.pos2 = this.pos2.add(new Vector(change.getX(), 0.0D, 0.0D));
                }
            } else if (Math.min(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
                this.pos1 = this.pos1.add(new Vector(change.getX(), 0.0D, 0.0D));
            } else {
                this.pos2 = this.pos2.add(new Vector(change.getX(), 0.0D, 0.0D));
            }

            if (change.getY() < 0.0D) {
                if (Math.max(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
                    this.pos1 = this.pos1.add(new Vector(0.0D, change.getY(), 0.0D));
                } else {
                    this.pos2 = this.pos2.add(new Vector(0.0D, change.getY(), 0.0D));
                }
            } else if (Math.min(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
                this.pos1 = this.pos1.add(new Vector(0.0D, change.getY(), 0.0D));
            } else {
                this.pos2 = this.pos2.add(new Vector(0.0D, change.getY(), 0.0D));
            }

            if (change.getZ() < 0.0D) {
                if (Math.max(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
                    this.pos1 = this.pos1.add(new Vector(0.0D, 0.0D, change.getZ()));
                } else {
                    this.pos2 = this.pos2.add(new Vector(0.0D, 0.0D, change.getZ()));
                }
            } else if (Math.min(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
                this.pos1 = this.pos1.add(new Vector(0.0D, 0.0D, change.getZ()));
            } else {
                this.pos2 = this.pos2.add(new Vector(0.0D, 0.0D, change.getZ()));
            }
        }

        this.recalculate();
    }

    public void shift(Vector change) {
        this.pos1 = this.pos1.add(change);
        this.pos2 = this.pos2.add(change);
        this.recalculate();
    }

    public Set<Vector> getChunkCubes() {
        Set<Vector> chunks = new HashSet();
        Vector min = this.getMinimumPoint();
        Vector max = this.getMaximumPoint();

        for (int x = min.getBlockX() >> 4; x <= max.getBlockX() >> 4; ++x) {
            for (int z = min.getBlockZ() >> 4; z <= max.getBlockZ() >> 4; ++z) {
                for (int y = min.getBlockY() >> 4; y <= max.getBlockY() >> 4; ++y) {
                    chunks.add(new Vector(x, y, z));
                }
            }
        }

        return chunks;
    }

    public boolean contains(Vector position) {
        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();
        Vector min = this.getMinimumPoint();
        Vector max = this.getMaximumPoint();
        return x >= (double) min.getBlockX() && x <= (double) max.getBlockX() && y >= (double) min.getBlockY() && y <= (double) max.getBlockY() && z >= (double) min.getBlockZ() && z <= (double) max.getBlockZ();
    }

    public Iterator<Vector> iterator() {
        return new Iterator<Vector>() {
            private Vector min = CuboidRegion.this.getMinimumPoint();
            private Vector max = CuboidRegion.this.getMaximumPoint();
            private int nextX;
            private int nextY;
            private int nextZ;

            {
                this.nextX = this.min.getBlockX();
                this.nextY = this.min.getBlockY();
                this.nextZ = this.min.getBlockZ();
            }

            public boolean hasNext() {
                return this.nextX != -2147483648;
            }

            public Vector next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    Vector answer = new Vector(this.nextX, this.nextY, this.nextZ);
                    if (++this.nextX > this.max.getBlockX()) {
                        this.nextX = this.min.getBlockX();
                        if (++this.nextY > this.max.getBlockY()) {
                            this.nextY = this.min.getBlockY();
                            if (++this.nextZ > this.max.getBlockZ()) {
                                this.nextX = -2147483648;
                            }
                        }
                    }

                    return answer;
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public String toString() {
        return this.getMinimumPoint() + " - " + this.getMaximumPoint();
    }

    public CuboidRegion clone() {
        return new CuboidRegion(this.world, this.pos1, this.pos2);
    }

    public static CuboidRegion fromCenter(World world, Vector origin, int apothem) {
        Preconditions.checkNotNull(origin);
        Preconditions.checkArgument(apothem >= 0, "apothem => 0 required");
        Vector size = (new Vector(1, 1, 1)).multiply(apothem);
        return new CuboidRegion(world, origin.subtract(size), origin.add(size));
    }
}
