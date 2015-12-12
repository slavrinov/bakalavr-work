import javax.swing.*;

/**
 * Created by Сергей on 30.10.2015.
 */
public class UITest extends JFrame {


   /* public Font timesNewRomanFont12 = new Font("Times New Roman", Font.PLAIN, 12);
    public Font timesNewRomanFont14b = new Font("Times New Roman", Font.BOLD, 14);

    public UITest(String name) {
        super(name);

    }

    public void addComponentsToPane(final Container pane) {
        //Frame window = new Frame();
        this.setBounds(10, 10, 800, 600);
        this.setTitle("Бакалаврская работа: Измерение объемного расхода газа в малых и сверхмалых объемах");
        this.setLocation(0, 0);
        this.setLayout(new BorderLayout());
        //FilterFrame center = new FilterFrame();

        final JPanel center = new JPanel();

        center.setLayout(new GridLayout(5, 1, 10, 10));
        JTextField field1 = new JTextField("БАКАЛАВРСКАЯ РАБОТА НА ТЕМУ:");
        field1.setFont(timesNewRomanFont14b);
        field1.setHorizontalAlignment(0);
        center.add(field1);
        JTextField field2 = new JTextField("Измерение объемного расхода газа в малых и сверхмалых объемах");
        field2.setFont(timesNewRomanFont12);
        field2.setHorizontalAlignment(0);
        center.add(field2);
        JTextField field3 = new JTextField("Автор: Лавринов С.В.");
        field3.setHorizontalAlignment(0);
        center.add(field3);
        JTextField field4 = new JTextField("Руководитель: Кюрджиев Ю.В.");
        field4.setHorizontalAlignment(0);
        center.add(field4);
        JButton startButton = new JButton("Старт");
        startButton.setHorizontalAlignment(0);
        startButton.setSize(100, 12);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    center.setVisible(false);
                    pane.setVisible(true);
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
        this.setVisible(true);
        //processFrame.drawImage
    }

    *//**
     * Create the GUI and show it.  For thread safety,
     * this method is invoked from the
     * event dispatch thread.
     *//*
    private static void createAndShowGUI() {
        //Create and set up the window.
        UITest frame = new UITest("Бакалаврская работа: Измерение объемного расхода газа в малых и сверхмалых объемах");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Set up the content pane.
        frame.addComponentsToPane(frame.getContentPane());
        //Display the window.
        frame.setBounds(10, 10, 800, 600);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {

        *//* Use an appropriate Look and Feel *//*
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        *//* Turn off metal's use of bold fonts *//*
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        //new UITest();
    }

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
                g.drawString("Добро пожаловать!", 10, 20);
                g.drawString("Данная программа предназначена для определения массового и объемного расхода газа в малых и сверхмалых процессах", 10, 20);
                g.drawString("Программа анализирует область и выделяет сферические объекты, которые являются пузырьками газа", 20, 20);
                g.drawString("и определяя радиус объектов вычисляет суммарный расход газа, проходящий через измерительное устройство ", 30, 20);
                g.drawString("Нажмите на кнопку старт", 10, 20);
                return;
            }

            buf = fgc.grabFrame();
            img = (new BufferToImage((VideoFormat) buf.getFormat())
                    .createImage(buf));

            g.drawImage(img, 0, 0, null);


            int w = img.getWidth(null);
            int h = img.getHeight(null);
            int avgcol = 0;
            int cts = 0;

            //System.out.println(w+","+h);
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
                        //System.out.println("pixels_all.length: " + pixels_all.length + ", w = " + w + " & h = " + h);
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
                //System.out.printf(">> %d, %d, %d, %d \n" , hf_region[0], hf_region[1], hf_region[2], hf_region[3]);
                //hf.setRegion(new int[]{0,0, maxx, maxy});
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
                        //.setColor(Color.white);
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

                boolean ballFound = true;
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
                //System.out.println("yDist: " + yDist);
                if (consecutiveNoFinds < 3) {
                    g.setColor(Color.blue);
                    g.drawOval((int) lastCenterX - lastRadius, (int) lastCenterY - lastRadius, 2 * lastRadius, 2 * lastRadius);

                    int cameraX = cameraPosition * 2;
                    if (trackBubble) {
                        if (lastCenterX > 170) {
                            int difference = 0;
                            if (serialMoveMethod == RADIUS)
                                difference = (int) ((double) (lastCenterX - 160) / (double) lastRadius * 2.25);
                            if (serialMoveMethod == XSPOT)
                                difference = (int) ((double) (lastCenterX - 160) / 2);

                            cameraPosition += difference;
                            if (cameraPosition > maxCameraPosition) cameraPosition = maxCameraPosition;
                        }
                        if (lastCenterX < 150) {
                            int difference = 0;
                            if (serialMoveMethod == RADIUS)
                                difference = (int) ((double) (lastCenterX - 160) / (double) lastRadius * 2.25);
                        *//*if(serialMoveMethod == XSPOT)
                            difference = (int)((double)(lastCenterX - 160)/2);*//*

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
                    ballFound = false;
                    //cameraPosition = (char)80;
                    if (trackBubble) {
                        if (cameraPosition > 80) cameraPosition -= 2;
                        if (cameraPosition < 70) cameraPosition += 2;
                        //todo
                        System.out.println("cameraPosition = " + cameraPosition);
                    }
                }
                currFPS++;
                g.setColor(Color.red);
                g.drawString("FPS: " + lastFPS, 10, 20);
                g.setColor(Color.black);
                g.drawString("Регион: " + regionList[preferredRegion], img.getWidth(null) + 10, 20);
                g.drawString("Радиус пузырька: " + bestRadius, img.getWidth(null) + 10, 35);
                g.drawString("Позиция: (" + bestLocationX + " , " + bestLocationY + ")", img.getWidth(null) + 10, 50);
                g.drawString("Аккумулятор: " + bestAccumulatorValue, img.getWidth(null) + 10, 65);
                g.drawString("Позиция камеры: " + (byte) cameraPosition, img.getWidth(null) + 10, 80);
                g.drawString("-------------------- ", img.getWidth(null) + 10, 80);
                g.drawString("Объемный Расход: " + (byte) cameraPosition, img.getWidth(null) + 10, 80);
            } catch (Exception exc) {
                System.out.println(exc);
                exc.printStackTrace();
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

        //System.out.println(min/49 + " , " + max/49 );

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
        //System.out.println(i+","+j);
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

    public int[] blobArea(int[][] hitMap, int hn) {
        int[] res = new int[hn];
        for (int j = 0; j < hitMap.length; j++) {
            for (int i = 0; i < hitMap[0].length; i++) {
                res[hitMap[j][i]]++;
            }
        }
        return res;
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
                System.out.println(fmts[i]);
                if (name.startsWith("vfw:")) {
                    System.out.println("" + fmts[i]);
                }
            }
        }
    }
*/
}
