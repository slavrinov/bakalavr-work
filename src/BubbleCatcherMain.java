
/*
* Sergey Lavrinov 15-10-2015
* */

import javax.media.*;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.util.BufferToImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Enumeration;
import java.util.Vector;

public class BubbleCatcherMain {
    CaptureDeviceInfo device;
    MediaLocator ml;
    Player player;
    FrameGrabbingControl fgc;
    JPanel pPanel;
    JFrame window;

    public int currFPS = 0, lastFPS = 0;
    public int cameraPosition = 75;
    public int maxCameraPosition = 145;
    public boolean trackBubble = true;

    public boolean isCapturingVideo = false;

    public Double VolExpense = 0D;
    public Double VolExpenseSum = 0D;
    public int p0;
    public int p1;
    public int density;
    public static int density0 = 9982;
    public static Integer pDefaultValue = 100000;
    public static double gConst = 9.81;
    public double avgH = 0;

    /*
    t,  °C 	σ, 10–3 Н/м
Вода 	Анилин
0 	75,64 	–
10 	74,22 	44,10
20 	72,25 	42,7
30 	71,18 	–
40 	69,56 	–
50 	67,91 	39,4
60 	66,18 	–
70 	64,42 	–
80 	62,61 	–
90 	60,75 	–
100 	58,85
     */
    public double sigma;
    public double sigma0 = 0.07225;

    public Font timesNewRomanFont12 = new Font("Times New Roman", Font.PLAIN, 12);
    public Font timesNewRomanFont14b = new Font("Times New Roman", Font.BOLD, 14);

    public static void main(String args[]) {
        getList();
        new BubbleCatcherMain();
    }

    BubbleCatcherMain() {
        try {

            window = new JFrame();
            window.setBounds(10, 10, 800, 600);
            window.setTitle(" Бакалаврская работа на тему: Определение расхода газа в сверхмалых процессах");
            window.setLocation(0, 0);
            window.setLayout(new BorderLayout());
            JPanel center = new JPanel();
            center.setLayout(new GridLayout(1, 1));
            pPanel = new FilterFrame();
            addComponentsToPane(pPanel);
            center.add(pPanel);
            window.add(center, BorderLayout.CENTER);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setVisible(true);

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void addComponentsToPane(final Container pane) {
        pane.setBounds(10, 10, 800, 600);
        pane.setName("Бакалаврская работа: Измерение объемного расхода газа в малых и сверхмалых объемах");
        pane.setLocation(0, 0);
        pane.setLayout(new BorderLayout());
        //FilterFrame center = new FilterFrame();

        final JPanel center = new JPanel();

        center.setLayout(new GridLayout(5, 1, 10, 10));
        JLabel field1 = new JLabel("БАКАЛАВРСКАЯ РАБОТА НА ТЕМУ:");
        field1.setFont(timesNewRomanFont14b);
        field1.setHorizontalAlignment(0);
        center.add(field1);
        JLabel field2 = new JLabel("Измерение объемного расхода газа в малых и сверхмалых объемах");
        field2.setFont(timesNewRomanFont12);
        field2.setHorizontalAlignment(0);
        center.add(field2);
        JLabel field3 = new JLabel("Автор: Лавринов С.В.");
        field3.setHorizontalAlignment(0);
        center.add(field3);
        JLabel field4 = new JLabel("Руководитель: Кюрджиев Ю.В.");
        field4.setHorizontalAlignment(0);
        center.add(field4);
/*        JSeparator sep1 = new JSeparator();
        center.add(sep1);*/
        JLabel field5 = new JLabel("Параметры:");
        JPanel paramPanel = new JPanel();
        field5.setHorizontalAlignment(0);
        center.add(field5);

        paramPanel.setLayout(new GridLayout(5, 3));
        JLabel l6 = new JLabel("Атмосферное давление:");
        final JTextField f6 = new JTextField();
        f6.setText("1000000");
        JLabel l6a = new JLabel(",Па");
        paramPanel.add(l6);
        paramPanel.add(f6);
        paramPanel.add(l6a);

        JLabel l7 = new JLabel("Давление компрессора2:");
        final JTextField f7 = new JTextField();
        f7.setText("0");
        JLabel l7a = new JLabel(",Па");
        paramPanel.add(l7);
        paramPanel.add(f7);
        paramPanel.add(l7a);

        JLabel l8 = new JLabel("Плотность жидкости:");
        final JTextField f8 = new JTextField();
        f8.setText("9982");
        JLabel l8a = new JLabel(",кг*м3");
        paramPanel.add(l8);
        paramPanel.add(f8);
        paramPanel.add(l8a);

        JLabel l9 = new JLabel("Коэффициент поверхностного натяжения:");
        final JTextField f9 = new JTextField();
        f9.setText("0.07225");
        JLabel l9a = new JLabel(",H/м3");
        paramPanel.add(l9);
        paramPanel.add(f9);
        paramPanel.add(l9a);

        JLabel l10 = new JLabel("Высота столба ж-ти от поверхности к средней линии:");
        final JTextField f10 = new JTextField();
        f9.setText("0.1");
        JLabel l10a = new JLabel(",м");
        paramPanel.add(l10);
        paramPanel.add(f10);
        paramPanel.add(l10a);

        center.add(paramPanel);
        final JButton startButton = new JButton("Старт");
        startButton.setHorizontalAlignment(0);
        startButton.setSize(100, 12);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (isCapturingVideo == false) {
                        isCapturingVideo = true;
                        //gets a list of devices how support the given videoformat
                        Vector deviceList = CaptureDeviceManager.getDeviceList(new YUVFormat());
                        device = (CaptureDeviceInfo) deviceList.firstElement();

                        System.out.println("Device: " + device);
                        ml = device.getLocator();
                        player = Manager.createRealizedPlayer(ml);
                        player.start();
                        fgc = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
                        bti = new BufferToImage((VideoFormat) (fgc.grabFrame()).getFormat());

                        javax.swing.Timer tu = new javax.swing.Timer(30, new Updater());
                        tu.start();
                        javax.swing.Timer fpsT = new javax.swing.Timer(1000, new FPS_Listener());
                        fpsT.start();

                        //read parameters values:
                        try {
                            p0 = Integer.valueOf(f6.getText());
                        } catch (NumberFormatException nfe) {
                            p0 = pDefaultValue;
                        }
                        System.out.println("p0 = " + p0);
                        try {
                            p1 = Integer.valueOf(f7.getText());
                        } catch (NumberFormatException nfe) {
                            p1 = 0;
                        }
                        System.out.println("p1 = " + p1);
                        try {
                            density = Integer.valueOf(f8.getText());
                        } catch (NumberFormatException nfe) {
                            density = density0;
                        }
                        System.out.println("density = " + density);
                        try {
                            sigma = Integer.valueOf(f9.getText());
                        } catch (NumberFormatException nfe) {
                            sigma = sigma0;
                        }
                        System.out.println("sigma = " + sigma);
                        try {
                            avgH = Integer.valueOf(f10.getText());
                        } catch (NumberFormatException nfe) {
                            avgH = 0;
                        }
                        System.out.println("avgH = " + avgH);


                        startButton.setText("Стоп");
                        pPanel.updateUI();
                        center.setVisible(false);
                        pane.setVisible(true);
                    } else {
                        window.setVisible(false);
                    }

                } catch (Exception exc) {
                    exc.printStackTrace();
                }
                //center.add(startButton);
            }

        });
        pane.add(center, BorderLayout.NORTH);
        pane.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.WEST);
        pane.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.EAST);
        pane.add(startButton, BorderLayout.SOUTH);
    }


    public class Updater implements ActionListener {
        public void actionPerformed(ActionEvent evt) {

            pPanel.repaint();
        }
    }

    public Buffer buf;
    public Image img;
    public BufferToImage bti;

    public int[][] sobelX = {{3, 10, 3}, {0, 0, 0}, {-3, -10, -3}};
    public int[][] sobelY = {{3, 0, -3}, {10, 0, -10}, {3, 0, -3}};
    public int[][] roiMap, refinedROImap;
    public int[][][] sobelMap;

    public int roiBasis;
    public double roiDeviation = .20;
    public HoughFilter hf = new HoughFilter(null, 15, 25, null);
    public int lastRadius;
    public int preferredRegion;
    public int lastCenterX, lastCenterY, lastDrawYBasis;
    public int radBegin, radEnd, radIncrement;
    public int consecutiveNoFinds;
    public int guessMinX, guessMaxX, guessMinY, guessMaxY;

    public static final int TOP = 0, BOTTOM = 1, CENTER = 2, ALL = 3, COMPRESS = 4;
    public static final String[] regionList = {"TOP", "BOTTOM", "CENTER", "ALL", "COMPRESS"};
    public CED_fast canny;
    public BufferedImage cannyImage;

    public class FilterFrame extends JPanel {
        public FilterFrame() {
            super();
            preferredRegion = BOTTOM;
            lastCenterX = -1;
            lastCenterY = -1;
            lastRadius = -1;
            lastDrawYBasis = 0;
            consecutiveNoFinds = 0;
            canny = new CED_fast();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (!isCapturingVideo) {
                g.setColor(Color.black);
                return;
            }
            buf = fgc.grabFrame();
            img = (new BufferToImage((VideoFormat) buf.getFormat()).createImage(buf));
            int w = 0;
            int h = 0;
            if (img != null) {
                g.drawImage(img, 0, 0, null);
                w = img.getWidth(null);
                h = img.getHeight(null);

                int[] pixels = new int[img.getWidth(null) * img.getHeight(null)];
                int[] pixels_all = new int[img.getWidth(null) * img.getHeight(null)];
                int[] pixels_region2 = new int[img.getWidth(null) * img.getHeight(null)];
                int[] pixels_region3 = new int[img.getWidth(null) * img.getHeight(null)];
                int[] pixels_compress = new int[img.getWidth(null) * img.getHeight(null)];
                int[][] map = new int[h / 2][w / 2];
                roiMap = new int[h / 2][w / 2];
                refinedROImap = new int[h / 2][w / 2];
                int[][] hitMap = new int[h / 2][w / 2];
                PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
                int reg3index = 0;
                int reg2index = 0;
                int reg1index = 0;
                int reg1index_compress = 0;
                try {
                    pg.grabPixels();
                    int increment = 1;
                    for (int j = 0; j < h; j += increment) {
                        for (int i = 0; i < w; i += increment) {
                            //int alpha = (pixels[i] >> 24) & 0xff;
                            float red = (pixels[i + j * w] >> 16) & 0xff;
                            float green = (pixels[i + j * w] >> 8) & 0xff;
                            float blue = (pixels[i + j * w]) & 0xff;
                            int gray = (int) (red * .3 + green * .59 + blue * .11);
                            map[j / 2][i / 2] = gray;
                            hitMap[j / 2][i / 2] = 0;
                            if ((j % 2) == 0 && (i % 2) == 0) {
                                pixels_compress[reg1index_compress] = Math.round(0.299f * red + 0.587f * green + 0.114f * blue);
                                reg1index_compress++;
                            }

                            pixels_all[j / increment * w / increment + i / increment] = Math.round(0.299f * red + 0.587f * green + 0.114f * blue);
                            //pixels[j/increment * w/increment + i/increment] = Math.round(0.299f * red + 0.587f * green + 0.114f * blue);
                            if (j < h / 2) {
                                pixels_region2[reg2index] = Math.round(0.299f * red + 0.587f * green + 0.114f * blue);
                                reg2index++;
                            } else {
                                pixels_region3[reg3index] = Math.round(0.299f * red + 0.587f * green + 0.114f * blue);
                                reg3index++;
                            }
                            if (j > h / 4 && j < 3 * h / 4) {
                                pixels[reg1index] = Math.round(0.299f * red + 0.587f * green + 0.114f * blue);
                                reg1index++;
                            }

                        }
                    }
                    sobelMap = sobelMapping(map);
                    int averageVal = fiveRegions(map);
                    for (int j = h / 2 - 1; j >= 0; j--) {
                        points = 0;
                        recursiveCatch(map, sobelMap, hitMap, averageVal, w / 4, j);
                    }
                    Polygon[] gons = createPolygon(hitMap);
                    g.setColor(Color.yellow);
                    g.drawPolygon(gons[1]);
                    int drawY_basis = 0;
                    switch (preferredRegion) {
                        case ALL:
                            canny.process_fast(pixels_all, w, h);
                            radBegin = 10;
                            radEnd = 80;
                            radIncrement = 10;
                            drawY_basis = 0;

                            break;
                        case TOP:
                            canny.process_fast(pixels_region2, w, h / 2);
                            radBegin = 10;
                            radEnd = 26;
                            radIncrement = 4;
                            drawY_basis = 0;
                            break;
                        case BOTTOM:
                            canny.process_fast(pixels_region3, w, h / 2);
                            radBegin = 45;
                            radEnd = 80;
                            radIncrement = 5;
                            drawY_basis = h / 2;
                            break;
                        case CENTER:
                            canny.process_fast(pixels, w, h / 2);
                            radBegin = 25;
                            radEnd = 45;
                            radIncrement = 5;
                            drawY_basis = h / 4;
                            break;
                        case COMPRESS:
                            canny.process_fast(pixels_compress, w / 2, h / 2);
                            radBegin = 5;
                            radEnd = 40;
                            radIncrement = 5;
                            drawY_basis = 0;
                            break;
                        default:
                            break;
                    }
                    cannyImage = canny.getEdgesImage();
                    g.drawImage(cannyImage, 0, img.getHeight(null), null);

                    int bestAccumulatorValue = 0;
                    int bestLocationX = 0;
                    int bestLocationY = 0;
                    int bestRadius = 0;
                    int maxx = canny.getWidth();
                    int maxy = canny.getHeight();
                    int hf_width = canny.getWidth();
                    int hf_height = canny.getHeight();

                    guessMinX = Math.max(0, lastCenterX - 3 * lastRadius);
                    guessMaxX = Math.min(w, lastCenterX + 3 * lastRadius);
                    guessMinY = Math.max(0, lastCenterY - 3 * lastRadius);
                    guessMaxY = Math.min(h, lastCenterY + 3 * lastRadius);

                    int[] scaledRegion = new int[]{guessMinX, Math.max(0, lastCenterY - 3 * lastRadius - drawY_basis), guessMaxX, Math.min(hf_height, lastCenterY + 3 * lastRadius - drawY_basis)};

                    g.setColor(Color.orange);
                    g.drawRect(guessMinX, guessMinY, guessMaxX - guessMinX, guessMaxY - guessMinY);
                    g.setColor(Color.green);
                    g.drawRect(scaledRegion[0], scaledRegion[1] + img.getHeight(null), scaledRegion[2] - scaledRegion[0], scaledRegion[3] - scaledRegion[1]);
                    int[] hf_region = new int[]{scaledRegion[0], scaledRegion[1], scaledRegion[2] - scaledRegion[0], scaledRegion[3] - scaledRegion[1]};

                    if (preferredRegion == COMPRESS) hf_region = new int[]{0, 0, maxx, maxy};
                    hf.setRegion(hf_region);
                    hf.createSourceArray(canny.getData(), hf_width, hf_height);
                    for (int rad = radBegin; rad <= radEnd; rad += radIncrement) {
                        hf.setParams(rad, rad + 7);
                        hf.resetChangeableArrays(hf_region);
                        hf.process(false);
                        java.util.LinkedList<Point> ps = hf.getPossibilityList();
                        for (int i = 0; i < ps.size(); i++) {
                            Point p = ps.get(i);
                            double drawX = p.getX();
                            double drawY = p.getY() + drawY_basis;
                            if (preferredRegion == COMPRESS) {
                                drawX = drawX * 2;
                                drawY = (drawY - drawY_basis) * 2 + drawY_basis;
                            }
                            if (hf.valList.get(i) > bestAccumulatorValue && gons[1].contains(drawX, drawY)) {
                                bestLocationX = (int) drawX;
                                bestLocationY = (int) drawY;
                                bestRadius = rad;
                                bestAccumulatorValue = hf.valList.get(i);
                            }
                        }
                    }
                    boolean bubbleFound = true;
                    if (bestRadius > 0) {
                        if (preferredRegion == COMPRESS) {
                            lastRadius = bestRadius * 2;
                            lastCenterX = bestLocationX;
                            lastCenterY = bestLocationY;
                            lastDrawYBasis = drawY_basis;
                            consecutiveNoFinds = 0;
                        } else {
                            lastRadius = bestRadius;
                            lastCenterX = bestLocationX;
                            lastCenterY = bestLocationY;
                            lastDrawYBasis = drawY_basis;
                            consecutiveNoFinds = 0;
                        }

                        if (preferredRegion != COMPRESS)
                            if (lastCenterY < 2 * h / 5) {
                                preferredRegion = TOP;
                            } else if (lastCenterY > 3 * h / 5) {
                                preferredRegion = BOTTOM;
                            } else
                                preferredRegion = CENTER;

                    } else {
                        preferredRegion = (preferredRegion == COMPRESS) ? ALL : COMPRESS; //COMPRESS;
                        consecutiveNoFinds++;

                    }
                    int yDist = (int) (-0.46667 * (double) lastRadius + 34.6667);
                    if (consecutiveNoFinds < 3) {
                        g.setColor(Color.blue);
                        g.drawOval((int) lastCenterX - lastRadius, (int) lastCenterY - lastRadius, 2 * lastRadius, 2 * lastRadius);

                        int cameraX = cameraPosition * 2;
                        if (trackBubble) {
                            if (lastCenterX > 170) {
                                int difference = 0;
                                cameraPosition += difference;
                                if (cameraPosition > maxCameraPosition) cameraPosition = maxCameraPosition;
                            }
                            if (lastCenterX < 150) {
                                int difference = 0;

                                cameraPosition += difference;
                                if (cameraPosition < 10) cameraPosition = 10;
                            }
                        }
                    } else {
                        g.setColor(Color.red);
                        g.drawString("не найден!!!", img.getWidth(null) + 20, img.getHeight(null));
                        lastCenterX = img.getWidth(null) / 2;
                        lastCenterY = img.getHeight(null) / 2;
                        lastRadius = img.getWidth(null);
                        //cameraPosition = (char)80;
                        if (trackBubble) {
                            if (cameraPosition > 80) cameraPosition -= 2;
                            if (cameraPosition < 70) cameraPosition += 2;
                            //todo
                            System.out.println("cameraPosition = " + cameraPosition);
                        }
                    }
                    double r = bestRadius / 1000D;
                    double t1 = 4 * Math.PI * r * r * r * (p0 - p1);
                    double t2 = 3D * (p0 - p1 - density * gConst * avgH - sigma * r / 2D);
                    VolExpense = t1 / t2;

                    VolExpenseSum = VolExpenseSum + VolExpense;
                    currFPS++;
                    g.setColor(Color.red);
                    g.drawString("FPS: " + lastFPS, 10, 20);
                    g.setColor(Color.black);
                    g.drawString("Регион: " + regionList[preferredRegion], img.getWidth(null) + 10, 20);
                    g.drawString("Радиус пузырька: " + bestRadius, img.getWidth(null) + 10, 35);
                    g.drawString("Позиция: (" + bestLocationX + " , " + bestLocationY + ")", img.getWidth(null) + 10, 50);
                    g.drawString("Аккумулятор: " + bestAccumulatorValue, img.getWidth(null) + 10, 65);
                    //g.drawString("Позиция камеры: " + (byte) cameraPosition, img.getWidth(null) + 10, 80);
                    g.drawString("-------------------- ", img.getWidth(null) + 10, 95);
                    g.drawString("Текущий объем газа:" + VolExpense, img.getWidth(null) + 10, 110);
                    g.drawString("Объемный Расход: " + VolExpenseSum, img.getWidth(null) + 10, 125);
                } catch (Exception exc) {
                    System.out.println(exc);
                    exc.printStackTrace();
                }
            }
        }
    }

    public int fiveRegions(int[][] grayImage) {
        int w = grayImage[0].length;
        int h = grayImage.length;
        int avgMid = 0, avgLeft = 0, avgRight = 0, avgUp = 0, avgDown = 0;

        for (int j = h / 2 - 3; j <= h / 2 + 3; j++) {
            for (int i = w / 2 - 3; i <= w / 2 + 3; i++) {
                avgMid += grayImage[j][i];
            }

            for (int i = w / 3 - 3; i <= w / 3 + 3; i++) {
                avgLeft += grayImage[j][i];
            }

            for (int i = 2 * w / 3 - 3; i <= 2 * w / 3 + 3; i++) {
                avgRight += grayImage[j][i];
            }
        }

        for (int i = w / 2 - 3; i <= w / 2 + 3; i++) {
            for (int j = h / 3 - 3; j <= h / 3 + 3; j++) {
                avgUp += grayImage[j][i];
            }

            for (int j = 2 * h / 3 - 3; j <= 2 * h / 3 + 3; j++) {
                avgDown += grayImage[j][i];
            }
        }
        int min = Math.min(avgUp, Math.min(avgDown, Math.min(avgLeft, Math.min(avgRight, avgMid))));
        int max = Math.max(avgUp, Math.max(avgDown, Math.max(avgLeft, Math.max(avgRight, avgMid))));
        int avg = ((avgUp + avgDown + avgLeft + avgRight + avgMid) - (min + max)) / 3 / 49;

        return avg;
    }

    public void findBlob(int[][] redMap, int[][] hitMap, int i, int j, int hn) {
        if (redMap[j][i] == 1 && hitMap[j][i] == 0) hitMap[j][i] = hn;
        else return;
        if (i + 1 < redMap[0].length) findBlob(redMap, hitMap, i + 1, j, hn); //right
        if (i - 1 >= 0) findBlob(redMap, hitMap, i - 1, j, hn); //left
        if (j + 1 < redMap.length) findBlob(redMap, hitMap, i, j + 1, hn); //down
        if (j - 1 >= 0) findBlob(redMap, hitMap, i, j - 1, hn); //up

        return;
    }

    public int points = 0;

    public Polygon[] createPolygon(int[][] hitmap) {
        Polygon toReturn = new Polygon();
        Polygon toReturn2 = new Polygon();
        int num_in_required = 4;
        for (int j = hitmap.length - 1; j >= 0; j--) {
            int inwards = 0;
            for (int i = 0; i < 2 * hitmap[0].length / 3; i++) {
                if (hitmap[j][i] == 1) {
                    inwards++;
                    if (inwards == num_in_required) {
                        toReturn.addPoint(i, j);
                        toReturn2.addPoint(i * 2, j * 2);
                        break;
                    }
                }
            }
        }
        for (int j = 0; j < hitmap.length; j++) {
            int inwards = 0;
            for (int i = hitmap[0].length - 1; i > hitmap[0].length / 3; i--) {

                if (hitmap[j][i] == 1) {
                    inwards++;
                    if (inwards == num_in_required) {
                        toReturn.addPoint(i, j);
                        toReturn2.addPoint(i * 2, j * 2);
                        break;
                    }
                }
            }
        }
        return new Polygon[]{toReturn, toReturn2};
    }

    public void recursiveCatch(int[][] map, int[][][] gradient, int[][] hitMap, int avg, int i, int j) {
        if (points > 6000) return;
        points++;
        if (hitMap[j][i] == 0) {
            if (map[j][i] <= (double) avg * 1.25 && Math.abs(gradient[0][j][i]) < 200 && Math.abs(gradient[1][j][i]) < 200) {
                hitMap[j][i] = 1;
                if (i + 1 < map[0].length) recursiveCatch(map, gradient, hitMap, avg, i + 1, j);
                if (j - 1 >= 0) recursiveCatch(map, gradient, hitMap, avg, i, j - 1);
                if (i - 1 >= 0) recursiveCatch(map, gradient, hitMap, avg, i - 1, j);
                if (j + 1 < map.length) recursiveCatch(map, gradient, hitMap, avg, i, j + 1);
            } else {
                if (Math.abs(gradient[0][j][i]) < 120 && Math.abs(gradient[1][j][i]) < 120 && map[j][i] <= (double) avg * 1.4) {
                    hitMap[j][i] = 1;

                    if (i + 1 < map[0].length) recursiveCatch(map, gradient, hitMap, avg, i + 1, j);
                    if (i - 1 >= 0) recursiveCatch(map, gradient, hitMap, avg, i - 1, j);
                    if (j + 1 < map.length) recursiveCatch(map, gradient, hitMap, avg, i, j + 1);
                    if (j - 1 >= 0) recursiveCatch(map, gradient, hitMap, avg, i, j - 1);
                } else
                    return;
            }
        } else {
            return;
        }
    }

    public void tryRecur(int[][] map, int[][] sobel, int[][] out, int i, int j, int[][] hitmap) {
        if (i < map[0].length && i >= 0 && j >= 0 && j < map.length && hitmap[j][i] == 0) {
            out[j][i] = 0;

            if (map[j][i] <= roiBasis + (int) ((double) roiBasis * roiDeviation)) {
                out[j][i] = 1;
                hitmap[j][i] = 1;
                tryRecur(map, sobel, out, i + 1, j, hitmap);//right
                tryRecur(map, sobel, out, i - 1, j, hitmap);//left
                tryRecur(map, sobel, out, i, j + 1, hitmap); //down
                tryRecur(map, sobel, out, i, j - 1, hitmap);//up

            } else if (sobel[j][i] <= 200) {
                hitmap[j][i] = 1;
                out[j][i] = 1;
                tryRecur(map, sobel, out, i + 1, j, hitmap);//right
                tryRecur(map, sobel, out, i - 1, j, hitmap);//left
                tryRecur(map, sobel, out, i, j + 1, hitmap); //down
                tryRecur(map, sobel, out, i, j - 1, hitmap);//up
            }
        }
        return;
    }

    public int[][][] sobelMapping(int[][] grayImage) {
        int[][][] result = new int[2][grayImage.length][grayImage[0].length];
        //int[][] resultY = new int[grayImage.length][grayImage[0].length];

        for (int j = 0; j < grayImage.length; j++) {
            for (int i = 0; i < grayImage[0].length; i++) {

                if (!(j - 1 < 0 || j + 1 >= grayImage.length
                        || i - 1 < 0 || i + 1 >= grayImage[0].length)) {
                    result[0][j][i] = (grayImage[j - 1][i - 1] * sobelX[0][0] +
                            grayImage[j - 1][i] * sobelX[0][1] +
                            grayImage[j - 1][i + 1] * sobelX[0][2] +
                            grayImage[j][i - 1] * sobelX[1][0] +
                            grayImage[j][i] * sobelX[1][1] +
                            grayImage[j][i + 1] * sobelX[1][2] +
                            grayImage[j + 1][i - 1] * sobelX[2][0] +
                            grayImage[j + 1][i] * sobelX[2][1] +
                            grayImage[j + 1][i + 1] * sobelX[2][2]);

                    result[1][j][i] = (grayImage[j - 1][i - 1] * sobelY[0][0] +
                            grayImage[j - 1][i] * sobelY[0][1] +
                            grayImage[j - 1][i + 1] * sobelY[0][2] +
                            grayImage[j][i - 1] * sobelY[1][0] +
                            grayImage[j][i] * sobelY[1][1] +
                            grayImage[j][i + 1] * sobelY[1][2] +
                            grayImage[j + 1][i - 1] * sobelY[2][0] +
                            grayImage[j + 1][i] * sobelY[2][1] +
                            grayImage[j + 1][i + 1] * sobelY[2][2]);
                } else {
                    result[0][j][i] = 0;
                    result[1][j][i] = 0;
                }
            }
        }

        return result;
    }

    public class FPS_Listener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            lastFPS = currFPS;
            currFPS = 0;
        }
    }

    private static void getList() {
        // TODO Auto-generated method stub
        Vector devices =
                (Vector) CaptureDeviceManager.getDeviceList(null);
        System.out.println(devices.size());
        Enumeration enumz = devices.elements();
        while (enumz.hasMoreElements()) {
            CaptureDeviceInfo cdi = (CaptureDeviceInfo) enumz.nextElement();
            String name = cdi.getName();
            javax.media.Format[] fmts = cdi.getFormats();

            System.out.println(name);

            for (int i = 0; i < fmts.length; i++) {
                //System.out.println(fmts[i]);
                System.out.println(fmts[i]);

                if (name.startsWith("vfw:")) {

                    System.out.println("" + fmts[i]);
                }
            }
        }
    }
}
