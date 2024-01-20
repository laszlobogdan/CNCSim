package Business_Logic;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

public class CNC extends JPanel {
    private int feedRate;
    private float currentX;
    private float currentY;
    private final Point2D.Float goal;
    private int type;
    private final Point2D.Float center;
    private final int[][] matrix;
    private final int height;
    private final int width;
    private static final int numPoints = 100;
    private Thread cuttingThread;

    private class CuttingRunnable implements Runnable {
        @Override
        public void run() {
            try {
                while (Math.abs(currentX - goal.x) >= 1 || Math.abs(currentY - goal.y) >= 1) {
                    if (type == 0) {
                        moveToGoal();
                    } else {
                        if (type == 1) {
                            calculateLinearInterpolatedPoints(currentX, currentY, goal.x, goal.y, numPoints);
                        } else {
                            calculateCircularInterpolationPoints(goal.x, goal.y, center.x, center.y, type);
                        }
                    }
                    checkGoalReached();
                    SwingUtilities.invokeLater(CNC.this::repaint);
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public CNC() {
        this.currentX = 10.0f;
        this.currentY = 10.0f;
        this.type = 0;
        this.height = 650;
        this.width = 500;
        this.matrix = new int[width][height];
        this.goal = new Point2D.Float();
        this.center = new Point2D.Float();
        cuttingThread = new Thread(new CuttingRunnable());
    }

    public void moveToGoal() {
        if (goal.x > currentX) {
            this.currentX += 1;
        } else if (goal.x < currentX) {
            this.currentX -= 1;
        }
        if (goal.y > currentY) {
            this.currentY += 1;
        } else if (goal.y < currentY) {
            this.currentY -= 1;
        }
    }

    public void reset() {
        currentX = 10;
        currentY = 10;
        System.out.println("new (X,Y): (" + currentX + "," + currentY + ")");
        goal.x = 0;
        goal.y = 0;
        resetMat();
        repaint();
        if (cuttingThread != null && cuttingThread.isAlive()) {
            cuttingThread.interrupt();
        }
    }

    private void resetMat() {
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                matrix[i][j] = 0;
            }
        }
    }

    private void setPixel(int x, int y) {

        if (x >= 0 && y >= 0 && matrix[x][y] == 0) {
            matrix[x][y] = 1;
        }
    }

    public void calculateLinearInterpolatedPoints(float startX, float startY, float endX, float endY, int numPoints) {
        float interpolatedX = 0;
        float interpolatedY = 0;
        for (int i = 0; i <= numPoints; i++) {
            float t = (float) i / numPoints;
            interpolatedX = startX + t * (endX - startX);
            interpolatedY = startY + t * (endY - startY);
            setPixel((int) interpolatedX, (int) interpolatedY);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            SwingUtilities.invokeLater(this::repaint);
        }

        currentX = interpolatedX;
        currentY = interpolatedY;
    }

    public void calculateCircularInterpolationPoints(double endX, double endY, double centerX, double centerY, int clockwise) {
        double radius = Math.sqrt(Math.pow(centerX-currentX, 2) + Math.pow(centerY-currentY, 2));
        double startAngle = Math.atan2(currentY-centerY,currentX -centerX);
        double endAngle = Math.atan2(endY - currentY, endX - currentX);

        if (clockwise == 2) {
            // Ensure end angle is greater than start angle for counterclockwise interpolation
            if (endAngle <= startAngle) {
                endAngle += 2 * Math.PI;
            }
        } else {
            // Ensure end angle is less than start angle for clockwise interpolation
            if (endAngle >= startAngle) {
                endAngle -= 2 * Math.PI;
            }
        }
        double angleIncrement = (endAngle - startAngle) / numPoints;
        double currentAngle;
        for (int i = 0; i <= numPoints; i++) {
            currentAngle = startAngle + i * angleIncrement;
            currentX = (float) (centerX + radius * Math.cos(currentAngle));
            currentY = (float) (centerY + radius * Math.sin(currentAngle));
            setPixel((int) currentX, (int) currentY);
            System.out.println(currentX + " " + currentY);
            if (checkGoalReached())
                break;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

            SwingUtilities.invokeLater(this::repaint);
        }
    }

    private Boolean checkGoalReached() {
        if ((Math.abs(goal.x - currentX) < 1 && Math.abs(goal.y - currentY) < 1)||(Math.abs(currentX - goal.x) < 1 && Math.abs(currentY - goal.y) < 1)) {
            System.out.println("Reached the goal!");
            System.out.println("new (X,Y): (" + (int)goal.x + "," + (int)goal.y + ")");
            if(type!=0) setPixel((int)goal.x,(int)goal.y);
            currentY=goal.y;
            currentX=goal.x;
            return true;
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int pixelSize = 10;

        // Draw the grid
        for (int i = 0; i < getWidth() / pixelSize; i++) {
            for (int j = 0; j < getHeight() / pixelSize; j++) {
                int x = i * pixelSize;
                int y = j * pixelSize;

                g.setColor(Color.WHITE);
                g.fillRect(x, y, pixelSize, pixelSize);
                g.setColor(Color.GRAY);
                g.drawRect(x, y, pixelSize, pixelSize);
            }
        }
        // Draw the points
        for (int i = 0; i < getWidth() / pixelSize; i++) {
            for (int j = 0; j < getHeight() / pixelSize; j++) {
                int x = i * pixelSize;
                int y = j * pixelSize;

                if (matrix[i][j] == 1) {
                    g.setColor(Color.BLACK);
                    g.fillRect(x, y, pixelSize, pixelSize);
                }
            }
        }
    }

    public void startCuttingAnimation() {
        if (cuttingThread != null && cuttingThread.isAlive()) {
            cuttingThread.interrupt();
        }
        cuttingThread = new Thread(new CuttingRunnable());
        cuttingThread.start();
    }

    public void setFeedRate(int feedRate) {
        this.feedRate = feedRate;
    }

    public float getCurrentX() {
        return currentX;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public float getCurrentY() {
        return currentY;
    }

    public void setXCenter(float center) {
        this.center.x = center;
    }

    public void setYCenter(float center) {
        this.center.y = center;
    }

    public void setXGoal(float xGoal) {
        this.goal.x = xGoal;
    }

    public void setYGoal(float yGoal) {
        this.goal.y = yGoal;
    }

    public void setType(int type) {
        this.type = type;
    }
}