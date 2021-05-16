import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.event.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;

import java.io.*;

class MainImage { // class for management and operation specific to the image object
    private BufferedImage image; // image object

    private int imageHeight; // image height
    private int imageWidth; // image width
    private int canvasWidth; // editor panel height
    private int canvasHeight; // editor panel width

    private int x1 = 0; // top left x coordinte of the image in the editor panel
    private int y1 = 0; // top left y coordinale of the image in the editor panel

    MainImage(BufferedImage image, int panelWidth, int panelHeight) {
        this.image = image;
        canvasWidth = panelWidth;
        canvasHeight = panelHeight;

        imageWidth = image.getWidth();
        imageHeight = image.getHeight();

        resizeDimensions();
    }

    public void resizeDimensions() { // to obtain the resized dimention of the image and its loction after adjusting
                                     // to the panel dimentions

        double constructRatio;
        double imageAspectRatio = (double) imageHeight / imageWidth;
        double canvasAspectRatio = (double) canvasHeight / canvasWidth;

        if (imageWidth < canvasWidth && imageHeight < canvasHeight) { // image smaller than canvas/panel
            x1 = (canvasWidth - imageWidth) / 2; // adjust image to horizontal centre aigned
            y1 = (canvasHeight - imageHeight) / 2; // adjust image to vertical center aligned

        } else {
            if (canvasAspectRatio > imageAspectRatio) { // image is shorter than the canvas/panel when width is adjusted
                                                        // to be same

                constructRatio = (double) canvasWidth / imageWidth;
                imageWidth = canvasWidth;
                imageHeight *= constructRatio;
                y1 = (canvasHeight - imageHeight) / 2;

            } else { // image is wider than the canvas/panel after adjusting the heights
                constructRatio = (double) canvasHeight / imageHeight;
                imageHeight = canvasHeight;
                imageWidth *= constructRatio;
                x1 = (canvasWidth - imageWidth) / 2;

            }
        }

    }

    // accessor methods
    public int getTopx() {
        return x1;
    }

    public int getTopy() {
        return y1;
    }

    public int getHeight() {
        return imageHeight;
    }

    public int getWidth() {
        return imageWidth;
    }

    public BufferedImage getImage() {
        return image;
    }

}

// the canvas which will contain the image object
class DisplayCanvas extends Canvas {

    private int canvasHeight; // same as image height
    private int canvasWidth; // saqme as image width
    private int topleftx; // top left position of the canvas wrt the editor panel -> x coordinate
    private int toplefty; // top left position of the canvas wrt the editor panel -> y coordinate
    private MainImage image;

    DisplayCanvas(MainImage image) {
        this.image = image;
        canvasHeight = image.getHeight(); // adjusted height
        canvasWidth = image.getWidth(); // adjusted width
        topleftx = image.getTopx();
        toplefty = image.getTopy();
    }

    public void paint(Graphics g) { // draw the image on the canvas , covering the whole canvas
        g.drawImage(image.getImage(), 0, 0, canvasWidth, canvasHeight, this);
    }

    public void setSelf() { // position the canvas in the editor panel
        setBounds(topleftx, toplefty, canvasWidth, canvasHeight);
        Editor.panel.add(this);
    }
}

class CropComponent implements MouseListener, MouseMotionListener { // to implement the crop functionality using mouse //creator

    private Canvas canvas;
    private MainImage image;
    private BufferedImage croppedImage;
    boolean cropping = false;
    private int x1, x2, y1, y2; //the top left and bottom right coordinate of the rectangular area in the image to be cropped
    private Graphics g;
    //int refreshrate = 15; //refresh rate the canvas while cropping  
    private double cordx, cordy, width, height; //of the regtangle to be cropped from the actual image
    private double conversionFactor; //-> actual image height / canvas image height
    Editor editor;

    CropComponent(Canvas canvas, MainImage image, Editor editor) {
        this.canvas = canvas;
        this.image = image;
        this.editor = editor;

        //mouse listeners for the canvas
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        g = canvas.getGraphics();

        conversionFactor = (double) image.getImage().getHeight() / image.getHeight();
    }

    // public void drawRect() {
    //     // canvas.repaint();
    //     // g.draw3DRect(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1 - x2, x2 - x1),
    //     // Math.max(y1 - y2, y2 - y1), true);
    // }

    @Override
    public void mousePressed(MouseEvent e) { //get initial coordinate
        this.x1 = e.getX();
        this.y1 = e.getY();

    }

    @Override
    public void mouseReleased(MouseEvent e) { //when mouse released
        //show the final regtangle on the canvas for 1 second
        g.draw3DRect(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1 - x2, x2 - x1), Math.max(y1 - y2, y2 - y1), true); 
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
            // pass
        }
        //calculation of the coordinates of the cropping rectangle on the actual image.
        cordx = Math.min(x1, x2) * conversionFactor;
        cordy = Math.min(y1, y2) * conversionFactor;
        width = Math.max(x1 - x2, x2 - x1) * conversionFactor;
        height = Math.max(y1 - y2, y2 - y1) * conversionFactor;
        try {
            croppedImage = image.getImage().getSubimage((int) cordx, (int) cordy, (int) width, (int) height);//get cropped image
            MainImage new_img = new MainImage(croppedImage, Editor.panelWidth, Editor.panelHeight); //cropped image transformation
            editor.repaintPanel(new_img); //repaint panel with the cropped image
            editor.actionPanel.removeAll();
            editor.actionPanel.repaint();
            
        } catch (Exception ex) {//when mouse is released out of the image canvas area.
            System.out.println("Cant crop outside the image!");
            canvas.repaint(); //remove unwanted rectangles in the canvas
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) { //get current coordinates (while mouse is being dragged)
        this.x2 = e.getX();
        this.y2 = e.getY();
    }
    //following are not required in this scope
    @Override
    public void mouseEntered(MouseEvent e) {
        // pass
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // pass
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // press
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // pass
    }

}

public class Editor { // main editor class-> the GUI part

    private static JFrame frame; // main frame object
    static JPanel panel; // editor panel
    //frame dimensions
    static int frameHeight = 800;
    static int frameWidth = 614;
    //panel dimensions
    static int panelHeight = 600;
    static int panelWidth = 600;

    private final double MAXLIMIT = 1600; 

    private Color btnColor = new Color(200,200,200); //button color
    private Border b = new BevelBorder(BevelBorder.RAISED); //button border type

    private MainImage originalImage; //a seperate copy of the original image is kept throughout the time while editing of an image is done 
    // private MainImage BWImage; //black and white image
    private MainImage previousImage; //current image is stored here every time an new filter is added. 
    public JPanel actionPanel; // additional extended panel required for brightness, contrast , sharpness and to follow crop instructions.

    // image object to be scanned from file dialog
    BufferedImage img; 

    MainImage image; //contains buffered image with added functionalities for processing
    DisplayCanvas canvas; //main display canvas

    String filepath = ""; //absolute location image opened 

    static BufferedImage currentImage; //used for the RGB adjustment
    int colorpalette[][][]; // contains real value of the pixel during RGB adjustments (values are not adjusted)
    int oldblue, oldgreen, oldred; //used for RED, GREEN , BLUE adjustment

    public Editor() {
        setMainFrame();
    }
    //get method
    public Editor getEditor() {
        return this;
    }

    //scale image with height or width > MAXLIMIT
    public BufferedImage scale(BufferedImage img){
        double max_wh = Math.max(img.getWidth(), img.getHeight());
        if(max_wh>MAXLIMIT){
            double scalefactor = MAXLIMIT/max_wh;
            System.out.println("Scalefactor: "+scalefactor);
            double new_w = img.getWidth()*scalefactor;
            double new_h = img.getHeight()*scalefactor;
            int w = (int) new_w;
            int h = (int) new_h;
            System.out.println("Corrected width, height: "+w+" "+h);
            BufferedImage newimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            
            AffineTransform at = new AffineTransform();
            at.scale(scalefactor, scalefactor);

            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            newimg = scaleOp.filter(img, newimg);
            return newimg;
        }
        return img;

    }

    public void scanImage() throws IOException { // loading image form the user

        BufferedImage got = ImageIO.read(new File(filepath));
        img = scale(got);
        image = new MainImage(img, panelWidth, panelHeight); //store original image - for editing
        previousImage = null; //currently none
        BufferedImage new_img = getCopyOf(img);
        originalImage = new MainImage(new_img, panelWidth, panelHeight); //store original image - a copy for reset
        
        //repaint the action panel in a new image is opened
        actionPanel.removeAll();
        actionPanel.repaint();
        frame.setTitle("Image Editor: "+filepath); //set title for currently opened image
    }

    public BufferedImage getCopyOf(BufferedImage img) { //create and return true copy of the a BufferedImage 
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = copy.createGraphics();
        g.drawImage(img, 0, 0, null);
        return copy;
    }

    public void setmenubar(){
        JMenuBar jmen = new JMenuBar();
        JMenu moreOp = new JMenu("More");
        moreOp.setBorder(b);
        //-------------------More Items -----------------//

        //Adjust RGB
        JMenuItem rgb= new JMenuItem("Adjust RGB");
        rgb.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                //store current image im previous image before applying filters
                BufferedImage prev = getCopyOf(image.getImage());
                previousImage = new MainImage(prev, panelWidth, panelHeight);
                
                showRGBBar();
                }catch(Exception ex){
                    //pass
                }
            }
        });

        //black and white filter option
        JMenuItem bw= new JMenuItem("Black & White");
        bw.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                //store current image im previous image before applying filters
                BufferedImage prev = getCopyOf(image.getImage());
                previousImage = new MainImage(prev, panelWidth, panelHeight);
                convertToBlackWhite();
                }catch(Exception ex){
                    //pass
                }
            }
        });

        //Emboss filter option
        JMenuItem em= new JMenuItem("Emboss");
        em.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                //store current image im previous image before applying filters
                BufferedImage prev = getCopyOf(image.getImage());
                previousImage = new MainImage(prev, panelWidth, panelHeight);
                embossImage(); //emboss image
                }catch(Exception ex){
                    //pass
                    System.out.println("Error");
                }
            }
        });

        //sepia filter option
        JMenuItem sepia= new JMenuItem("Sepia");
        sepia.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                //store current image im previous image before applying filters
                BufferedImage prev = getCopyOf(image.getImage());
                previousImage = new MainImage(prev, panelWidth, panelHeight);
                imageSepia(); //apply sepia filter
                }catch(Exception ex){
                    //pass
                    System.out.println("Error");
                    System.out.println(ex.getMessage());
                }
            }
        });
        
        //add the menu-items to menu
        moreOp.add(rgb);
        moreOp.add(bw);
        moreOp.add(em);
        moreOp.add(sepia);


        jmen.add(moreOp);
        jmen.setBorder(b);
        jmen.setVisible(true);
        frame.setJMenuBar(jmen);

    }

    public void showRGBBar(){

        JFrame tframe = new JFrame("Adjust RGB");
        tframe.setBounds(300,200,465,210);
        tframe.setResizable(false);
        tframe.setLayout(null);

        int width = previousImage.getImage().getWidth();
        int height = previousImage.getImage().getHeight();

        oldblue = oldred = oldgreen = 0;

        //copy the current pixel data into a 3D color matrix
        colorpalette = new int[width][height][3];
        for(int i = 0; i< width; i++){
            for(int j = 0; j<height; j++){
                Color c =  new Color(image.getImage().getRGB(i, j));
                colorpalette[i][j][0] = c.getRed();
                colorpalette[i][j][1] = c.getGreen();
                colorpalette[i][j][2] = c.getBlue();
            }
        }

        currentImage = getCopyOf(previousImage.getImage());

        //red adjustment slider
        JSlider redbar = new JSlider(-60, 60, 0); //create JSlider range -> -50 to +50
        redbar.setMajorTickSpacing(5); //ticks
        redbar.setMinorTickSpacing(1);
        redbar.setPaintTicks(true);
        redbar.setPaintLabels(true);
        redbar.setBounds(10, 5, 430, 50);
        redbar.setBackground(new Color(250,0,0));
        redbar.setForeground(new Color(255,255,255));
        redbar.setBorder(b);
        redbar.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent c) {
                //BufferedImage clone = getCopyOf(currentImage); //make a clone of the previous image 
                
                int val = redbar.getValue();

                int change = val - oldred; // change the slider value 
                MainImage newimg = changeColor(new MainImage(currentImage, panelWidth, panelHeight), change, 1); 
                repaintPanel(newimg); // set the new filtered image in the canvas and repaint panel
                oldred = val; // previous value is the new value
                currentImage = getCopyOf(newimg.getImage());
            }
        });


        JSlider greenbar = new JSlider(-60, 60, 0); //create JSlider range -> -50 to +50
        greenbar.setMajorTickSpacing(5); //ticks
        greenbar.setMinorTickSpacing(1);
        greenbar.setPaintTicks(true);
        greenbar.setPaintLabels(true);
        greenbar.setBounds(10, 60, 430, 50);
        greenbar.setBackground(new Color(0,250,0));
        greenbar.setForeground(new Color(255,255,255));
        greenbar.setBorder(b);
        greenbar.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent c) {
                //BufferedImage clone = getCopyOf(currentImage); //make a clone of the previous image 

                int val = greenbar.getValue();
                int change = val - oldgreen;
                MainImage newimg = changeColor(new MainImage(currentImage, panelWidth, panelHeight), change, 2); 
                repaintPanel(newimg); // set the new filtered image in the canvas and repaint panel
                oldgreen = val;

                currentImage = getCopyOf(newimg.getImage());
            }
        });

        JSlider bluebar = new JSlider(-60, 60, 0); //create JSlider range -> -50 to +50
        bluebar.setMajorTickSpacing(5); //ticks
        bluebar.setMinorTickSpacing(1);
        bluebar.setPaintTicks(true);
        bluebar.setPaintLabels(true);
        bluebar.setBounds(10, 115, 430, 50);
        bluebar.setBackground(new Color(0,0,250));
        bluebar.setForeground(new Color(255,255,255));
        bluebar.setBorder(b);
        bluebar.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent c) {
                //BufferedImage clone = getCopyOf(currentImage); //make a clone of the previous image 

                int val = bluebar.getValue();
                int change = val -oldblue;
                MainImage newimg = changeColor(new MainImage(currentImage, panelWidth, panelHeight), change, 3); 
                repaintPanel(newimg); // set the new filtered image in the canvas and repaint panel
                oldblue = val;
                currentImage = getCopyOf(newimg.getImage());
            }
        });

        tframe.getContentPane().add(redbar);
        tframe.getContentPane().add(greenbar);
        tframe.getContentPane().add(bluebar);
        tframe.setVisible(true);
    }


    public void setMainFrame() { // setting up the GUI part
        //the frame of the editor application
        frame = new JFrame("Image Editor ");
        frame.setResizable(false); //frame not resizable
        frame.setBounds(400, 10, frameWidth, frameHeight);
        frame.setLayout(null);

        //setmenubar for adjusting rgb values
        //more 
        setmenubar();
        //panel (fixed size)-> holds the main canvas(variable size)
        panel = new JPanel();
        panel.setBounds(0, 0, panelWidth, panelHeight);

        setPanel(); //setpanel and adjust canvas(if image is opened)

        //set the action panel at the bottom of the frame
        actionPanel = new JPanel();
        actionPanel.setBounds(5, panelHeight + 75, panelWidth - 10, 60);
        actionPanel.setBackground(Color.gray);
        frame.add(actionPanel);

        frame.add(panel);


        //----------first row functionalities----------//
        // open image
        JButton open = new JButton("OPEN");
        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openNewFile();
            }
        });
        open.setBounds(5, panelHeight + 5, 90, 30);
        open.setBackground(btnColor);
        open.setFocusPainted(false);
        open.setBorder(b);
        frame.add(open);

        // crop image
        JButton crop = new JButton("CROP");
        crop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    BufferedImage prev = getCopyOf(image.getImage());
                    previousImage = new MainImage(prev, panelWidth, panelHeight);
                    new CropComponent(canvas, image, getEditor());
                    JLabel info = new JLabel("Click and drag your mouse over the area in the image to crop");
                    actionPanel.removeAll();
                    info.setBounds(5, 5, panelWidth - 20, 70);
                    info.setFont(new Font("Verdana", Font.BOLD, 15));
                    info.setHorizontalAlignment(SwingConstants.CENTER);
                    info.setVerticalAlignment(SwingConstants.CENTER);
                    actionPanel.add(info);
                    actionPanel.repaint();

                } catch (Exception ex) {
                    System.out.println("Coudn't Initialize Crop Component!");
                }
            }
        });
        crop.setBounds(100, panelHeight + 5, 90, 30);
        crop.setBackground(btnColor);
        crop.setFocusPainted(false);
        crop.setBorder(b);
        frame.add(crop);


        //reset original image in the canvas 
        JButton revert = new JButton("Reset");
        revert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                previousImage = null;
                try {
                    BufferedImage im = getCopyOf(originalImage.getImage());
                    MainImage oldImage = new MainImage(im, panelWidth, panelHeight);
                    repaintPanel(oldImage);
                    actionPanel.removeAll();
                    actionPanel.repaint();
                } catch (Exception ex) {
                    System.out.println("Cant restore image!");
                }
            }
        });
        revert.setBounds(195, panelHeight + 5, 90, 30);
        revert.setBackground(btnColor);
        revert.setFocusPainted(false);
        revert.setBorder(b);
        frame.add(revert);

        //convert image to grayscale
        JButton bw = new JButton("GrayScale");
        bw.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedImage prev = getCopyOf(image.getImage());
                    previousImage = new MainImage(prev, panelWidth, panelHeight);
                    actionPanel.removeAll();
                    actionPanel.repaint();
                    converttoBW();
                } catch (Exception ex) {
                    System.out.println("None !");
                }
            }
        });
        bw.setBounds(290, panelHeight + 5, 90, 30);
        bw.setBackground(btnColor);
        bw.setFocusPainted(false);
        bw.setBorder(b);
        frame.add(bw);

        //invert the pixel values in the image--negative of the image
        JButton invert = new JButton("Invert");
        invert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedImage prev = getCopyOf(image.getImage());
                    previousImage = new MainImage(prev, panelWidth, panelHeight);
                    actionPanel.removeAll();
                    actionPanel.repaint();

                    invertColor();
                } catch (Exception ex) {
                    System.out.println("None !");
                }
            }
        });
        invert.setBounds(385, panelHeight + 5, 90, 30);
        invert.setBackground(btnColor);
        invert.setFocusPainted(false);
        invert.setBorder(b);
        frame.add(invert);
        

        //restore the recent previous version of the image 
        JButton previous = new JButton("Previous");
        previous.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionPanel.removeAll();
                actionPanel.repaint();
                setPreviousImage();
            }
        });
        previous.setBounds(480, panelHeight + 5, 110, 30);
        previous.setBackground(btnColor);
        previous.setFocusPainted(false);
        previous.setBorder(b);
        frame.add(previous);
        

        //------------------second row -----------------------//
        //Additional filters for the second row 
        addBrightness();
        addContrast();
        addSharpner();
        addRotate();
        addMirror();

        //save file button
        addSave();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }

    public void setPanel() { //called with main method and also when a new image is opened
        //setting up the fixed panel
        panel.setBackground(Color.BLACK);
        panel.setLayout(null);
        panel.removeAll(); // remove currently present canvas from the panel
        if (!filepath.equals("")) { //only allow if image path exists
            try {
                scanImage(); //scan image by file location and filename using ImageIO
                canvas = new DisplayCanvas(image); //create new image canvas with loaded image
                canvas.repaint(); //repaint canvas 
                canvas.setSelf(); //add canvas to the panel

            } catch (IOException ex) {
                System.out.println("Cant Load Image");
            }
        }
    }
    // set previous image in the canvas
    public void setPreviousImage() {
        try {
            BufferedImage old = getCopyOf(previousImage.getImage());
            repaintPanel(new MainImage(old, panelWidth, panelHeight));
        } catch (Exception e) {
            // pass
        }

    }

    //add brightness button
    public void addBrightness() {
        JButton brightness = new JButton("Bright");
        brightness.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    //store current image im previous image before applying filters
                    BufferedImage prev = getCopyOf(image.getImage());
                    previousImage = new MainImage(prev, panelWidth, panelHeight);

                    shyowBrightnessBar();

                } catch (Exception ex) {
                    // pass
                }
            }
        });
        brightness.setBounds(5, panelHeight + 40, 90, 30);
        brightness.setBackground(btnColor);
        brightness.setFocusPainted(false);
        brightness.setBorder(b);
        frame.add(brightness); //add button to frame
    }

    //show brightness bar (JSlider) in the actionpanel area
    public void shyowBrightnessBar() {
        //clear action panel
        actionPanel.removeAll();
        actionPanel.repaint();

        JSlider bbar = new JSlider(-20, 20, 0); //create JSlider range -> -20 to +20
        bbar.setMajorTickSpacing(5); //ticks
        bbar.setMinorTickSpacing(1);
        bbar.setPaintTicks(true);
        bbar.setPaintLabels(true);
        bbar.setBounds(10, 5, 430, 50);
        bbar.setBackground(new Color(200, 200,250));
        bbar.setBorder(b);
        bbar.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent c) {
                BufferedImage clone = getCopyOf(previousImage.getImage()); //make a clone of the previous image 
                float factor = 1f + 0.03f * bbar.getValue();
                float offset = 0f + 2f * bbar.getValue(); //get offset value 
                MainImage newimg = changeBrightness(new MainImage(clone, panelWidth, panelHeight), offset, factor); //change brightness of cloned image
                repaintPanel(newimg); // set the new filtered image in the canvas and repaint panel
            }
        });

        //close button for closing brightness bar
        JButton close = new JButton("Close");
        close.setBounds(450, 5, 100, 50);
        close.setBackground(Color.red);
        close.setForeground(Color.white);
        close.setBorder(b);
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionPanel.removeAll();
                actionPanel.repaint();
            }
        });
        actionPanel.setLayout(null);
        //add brightness bar and close button to the frame
        actionPanel.add(bbar);
        actionPanel.add(close);
        actionPanel.repaint();
        frame.setVisible(true);
    }
    

    //adding a contrast button
    public void addContrast() {
        JButton contrast = new JButton("Contrast");
        contrast.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedImage prev = getCopyOf(image.getImage());
                    previousImage = new MainImage(prev, panelWidth, panelHeight);

                    showContrastBar();

                } catch (Exception ex) {
                    // pass
                }
            }
        });
        contrast.setBounds(100, panelHeight + 40, 90, 30);
        contrast.setBackground(btnColor);
        contrast.setFocusPainted(false);
        contrast.setBorder(b);
        frame.add(contrast);
    }


    //a constrast bar with a close button -> structurally similar to the brightness bar
    public void showContrastBar() {
        actionPanel.removeAll();
        actionPanel.repaint();

        JSlider cbar = new JSlider(-20, 20, 0);
        cbar.setMajorTickSpacing(5);
        cbar.setMinorTickSpacing(1);
        cbar.setPaintTicks(true);
        cbar.setPaintLabels(true);
        cbar.setBounds(10, 5, 430, 50);
        cbar.setBackground(new Color(200, 250,200));
        cbar.setBorder(b);
        cbar.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent c) {
                BufferedImage clone = getCopyOf(previousImage.getImage());
                float factor = 1f + 0.02f * cbar.getValue(); //calculating the contrast factor
                float scale = 0f + 2f * cbar.getValue(); //get offset value 
                MainImage newimg = changeContrast(new MainImage(clone, panelWidth, panelHeight), factor, -1*scale); //change contrast ang get filtered image
                repaintPanel(newimg); //repaint canvas and panel
            }
        });
        //add close button
        JButton close = new JButton("Close");
        close.setBounds(450, 5, 100, 50);
        close.setBackground(Color.red);
        close.setForeground(Color.white);
        close.setBorder(b);
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionPanel.removeAll();
                actionPanel.repaint();
            }
        });
        actionPanel.setLayout(null);
        actionPanel.add(cbar);
        actionPanel.add(close);
        actionPanel.repaint();
        frame.setVisible(true);
    }


    //the sharpen button
    public void addSharpner() {
        JButton sharpen = new JButton("Sharpen");
        sharpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedImage prev = getCopyOf(image.getImage());
                    previousImage = new MainImage(prev, panelWidth, panelHeight);
                    showSharpnessBar();
                } catch (Exception ex) {
                    // pass
                }
            }
        });
        sharpen.setBounds(195, panelHeight + 40, 90, 30);
        sharpen.setBackground(btnColor);
        sharpen.setFocusPainted(false);
        sharpen.setBorder(b);
        frame.getContentPane().add(sharpen);
    }

    //the sharpness bar ->JSlider with a close button in the action panel
    public void showSharpnessBar() {
        actionPanel.removeAll();
        actionPanel.repaint();

        JSlider sbar = new JSlider(0, 10, 0); //sharpnes ranges from 1 to 10
        sbar.setMajorTickSpacing(1);
        sbar.setMinorTickSpacing(1);
        sbar.setPaintTicks(true);
        sbar.setPaintLabels(true);
        sbar.setBounds(10, 5, 430, 50);
        sbar.setBackground(new Color(250, 200,200));
        sbar.setBorder(b);
        sbar.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent c) {
                BufferedImage clone = getCopyOf(previousImage.getImage());
                float factor = 1 - 0.1f * sbar.getValue(); //sharpness factor calculation (1-> no sharpness, 0->maximum sharpness)
                MainImage newimg = sharpenImage(new MainImage(clone, panelWidth, panelHeight), factor); // filter image
                repaintPanel(newimg); // repaint whole panel with new image
            }
        });
        JButton close = new JButton("Close"); // close button for action panel
        close.setBounds(450, 5, 100, 50);
        close.setBackground(Color.red);
        close.setForeground(Color.white);
        close.setBorder(b);
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionPanel.removeAll();
                actionPanel.repaint();
            }
        });
        actionPanel.setLayout(null);
        actionPanel.add(sbar);
        actionPanel.add(close);
        actionPanel.repaint();
        frame.setVisible(true);
    }

    //Adding a rotate button 
    public void addRotate() {
        JButton rotate = new JButton("Rotate");
        rotate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedImage prev = getCopyOf(image.getImage());
                    previousImage = new MainImage(prev, panelWidth, panelHeight);
                    RotateCurrentImage();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        rotate.setBounds(290, panelHeight + 40, 90, 30);
        rotate.setBackground(btnColor);
        rotate.setFocusPainted(false);
        rotate.setBorder(b);
        frame.getContentPane().add(rotate);
    }

    //----------------rotate image-------------------// 
    void RotateCurrentImage() {

        int h = image.getImage().getHeight(); // get original height of the current image
        int w = image.getImage().getWidth(); // ,,      ,,     width    ,,      ,,      ,,
        BufferedImage currImg = image.getImage(); //get the BufferedImage

        BufferedImage newImg = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB); // create a BufferedImage Template for 90 degree rotated image

        //rotate current image 90 degree clockwise
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                newImg.setRGB(h - i - 1, j, currImg.getRGB(j, i)); 
            }
        }
        //clear action panel (if anything exists)
        actionPanel.removeAll(); 
        actionPanel.repaint();
        repaintPanel(new MainImage(newImg, panelWidth, panelHeight)); // repaint canvas with the new rotated image
    }

    // mirror button
    public void addMirror() {
        JButton mirror = new JButton("Mirror");
        mirror.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedImage prev = getCopyOf(image.getImage());
                    previousImage = new MainImage(prev, panelWidth, panelHeight);
                    mirrorImage();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        mirror.setBounds(385, panelHeight + 40, 90, 30);
        mirror.setBackground(btnColor);
        mirror.setFocusPainted(false);
        mirror.setBorder(b);
        frame.getContentPane().add(mirror);
    }

    //--------Get and Set mirror reflection of the current image ---------------//
    public void mirrorImage(){

        int h = image.getImage().getHeight(); //get original height
        int w = image.getImage().getWidth(); //get original width
        BufferedImage currImg = image.getImage();

        BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB); // create bufferedimage template
        //create reflecton of current image in the new templete
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                newImg.setRGB(w - i - 1, j, currImg.getRGB(i, j)); // reverse each row of the current image to new image
            }
        }
        //clear action panel
        actionPanel.removeAll();
        actionPanel.repaint();
        repaintPanel(new MainImage(newImg, panelWidth, panelHeight)); // repaint canvas with new image
    }

    //open file functionality -> called when open button is clicked
    public void openNewFile() {
        FileDialog fd = new FileDialog((Frame) null, "Select file"); // show file dialog fo opening file
        fd.setMode(FileDialog.LOAD); // load file 
        fd.setVisible(true);
        filepath = fd.getDirectory() + fd.getFile(); // get the absolute path to the file as a string
        System.out.println(filepath);//observer
        if (!filepath.equals("nullnull")) //set panel only if a file is selected
            setPanel();
    }

    //save button
    public void addSave(){
        JButton save = new JButton("SAVE");
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if(image!=null)
                        saveCurrentImage();

                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        save.setBounds(480, panelHeight + 40, 110, 30);
        save.setBackground(btnColor);
        save.setFocusPainted(false);
        save.setBorder(b);
        frame.getContentPane().add(save);
    }

    //save current image to a new file
    public void saveCurrentImage(){

        FileDialog fd = new FileDialog((Frame) null, "Save File"); //open file dialog for saving
        fd.setMode(FileDialog.SAVE);
        fd.setVisible(true);
        String savepath = fd.getDirectory() + fd.getFile()+".jpg"; //absolute path to the new file to be created
        // System.out.println(savepath);
        try{

            ImageIO.write(image.getImage(), "jpg", new File(savepath)); //save new file as .jpg format

        }catch(IOException ex){

            System.out.println("Cant be saved!");
        }
    }

    //----called when B & W button is clicked----// converts image to grayscale tone
    public void converttoBW() {

        int wd = image.getImage().getWidth(); //get image dimensions
        int ht = image.getImage().getHeight();
        BufferedImage temp = image.getImage(); // convert current image to B/W image
        for (int i = 0; i < ht; i++) {
            for (int j = 0; j < wd; j++) {
                Color c = new Color(temp.getRGB(j, i)); // color of the pixel j,i of temp
                int r = (int) (c.getRed() * 0.299); // ajusting red
                int g = (int) (c.getGreen() * 0.587); // adjusting green
                int b = (int) (c.getBlue() * 0.114); // adjusting blue

                Color newCol = new Color(r + g + b, r + g + b, r + g + b); // create a new color for the pixel 
                image.getImage().setRGB(j, i, newCol.getRGB()); //set RGB value for the pixel
            }
        }
        repaintPanel(image); // repaint panel/canvas with filtered image
    }

    //-------------Convert image to only black and white------------//

    public void convertToBlackWhite(){
        int avg = 255/2; //half way to rgb
        int pixel  = 0;
        int wd = image.getImage().getWidth();
        int ht = image.getImage().getHeight();
        BufferedImage temp = image.getImage();
        for(int i = 0; i<wd; i++){
            for(int j  =0; j<ht; j++){
                Color c = new Color(temp.getRGB(i, j));
                if(((c.getRed() + c.getBlue()+ c.getGreen())/3)<avg) // if avg of all the pixels is lesser than 127
                    pixel = 0; //set current pixel to black
                else pixel = 255;   //set pixel to white 

                Color newCol = new Color(pixel, pixel, pixel);
                image.getImage().setRGB(i,j, newCol.getRGB());
            }
        }
        repaintPanel(image);
    }

    //convert image to sepia
    public void imageSepia(){
        BufferedImage temp = image.getImage();

        //dimensions
        int w = temp.getWidth();
        int h = temp.getHeight();

        //sepia intensity
        int intensity = 60;
        //sepia depth
        int sepiaDepth = 27;

        for(int i = 0;i < w;i++){
            for(int j  = 0; j< h; j++){
                Color newCol = new Color(temp.getRGB(i, j));
                
                int r = newCol.getRed();
                int g = newCol.getGreen();
                int b = newCol.getBlue();

                int avg = (r+g+b)/3;
                r=g=b=avg;

                r+= sepiaDepth*2;
                g+= sepiaDepth*0.62;
                b-= intensity;

                //Adjusting values to their limits
                r = Math.min(r, 255);
                g = Math.min(g, 255);
                b = Math.max(b, 0);

                image.getImage().setRGB(i, j, new Color(r, g, b).getRGB());

            }
        }
        //increas the contrast
        RescaleOp resc = new RescaleOp(1.28f,-28, null);
        temp = resc.filter(image.getImage(), null);
        repaintPanel(new MainImage(temp, panelWidth, panelHeight));
    }
    //Following two funtions are used for the Emboss filter
    //returns a color matrix containing the RGB values if every pixel of the current image 
    public int[][][] getRGB_buffer(){
        //extract width and height
        int w = image.getImage().getWidth();
        int h = image.getImage().getHeight();
        
        BufferedImage temp = image.getImage();
        int[][][] rgb_buffer = new int[3][w][h]; //color matrix

        for(int i  =0; i<w; i++){
            for(int j = 0; j<h; j++){
                Color pixelColor = new Color(temp.getRGB(i, j));
                rgb_buffer[0][i][j] = pixelColor.getRed(); //red color for (i,j)th pixel
                rgb_buffer[1][i][j] = pixelColor.getGreen(); //green color ,,
                rgb_buffer[2][i][j] = pixelColor.getBlue(); //blue color ,,
            }
        }

        return rgb_buffer; //return color matrix

    }
    //----------------------------Emboss---------------------//
    public void embossImage(){
        //get rgb buffer of the image
        int[][][] rgb_buffer = getRGB_buffer();

        int w = image.getImage().getWidth();
        int h = image.getImage().getHeight();

        for(int i  =1; i<w; i++){
            for(int j = 1; j<h; j++){
                
                int r=0,g=0,b=0;
                // /deduct the current pixel value from its ajacent pixel value
                r = Math.min(Math.abs(rgb_buffer[0][i][j] - rgb_buffer[0][i-1][j-1])+100, 255);
                g = Math.min(Math.abs(rgb_buffer[1][i][j] - rgb_buffer[1][i-1][j-1])+100, 255);
                b = Math.min(Math.abs(rgb_buffer[2][i][j] - rgb_buffer[2][i-1][j-1])+100, 255);

                image.getImage().setRGB(i, j, new Color(r, g, b).getRGB());
                
            }
        }
        //repaint panel
        repaintPanel(image);

    }

    //--------------Invert color of a given image-------------//
    public void invertColor() {
        int wd = image.getImage().getWidth(); //get absolute width of the image 
        int ht = image.getImage().getHeight(); // get absolute height of the image
        BufferedImage temp = image.getImage(); // get image
        for (int i = 0; i < ht; i++) {
            for (int j = 0; j < wd; j++) {
                Color c = new Color(temp.getRGB(j, i)); // get color of the current pixel
                //calculate new rgb values 
                int r = 255 - c.getRed(); 
                int g = 255 - c.getGreen();
                int b = 255 - c.getBlue();

                //create new color out of the r,g,b vales
                Color newCol = new Color(r, g, b);
                image.getImage().setRGB(j, i, newCol.getRGB()); // set pixel color for the current image
            }
        }
        repaintPanel(image); // repaint panel
    }   
    
    //------------------Adjust the RGB Valaues of the image (col = 1 for red, 2 for green , 3 for blue)-------------------//
    
    public MainImage changeColor(MainImage mimg, int change, int col){ //adjust red green blue 

        BufferedImage temp = mimg.getImage(); //true copy of the original image
        BufferedImage newImage = getCopyOf(temp); // temporary copy
        int width = temp.getWidth();
        int heigth = temp.getHeight();

        if(col==1){ //adjust red
            for(int i = 0; i< width; i++){
                for(int j= 0; j< heigth; j++){
                    colorpalette[i][j][0] += change; //makes changes for red channel in the colorpalette
                    Color c = new Color(temp.getRGB(i, j));
                    int red = c.getRed();
                    if(colorpalette[i][j][0]<0) //if colorpalette gives negative R value
                        red = 0;
                    else if(colorpalette[i][j][0]>255) // if R value exceeds 255 limit
                        red = 255;
                    else
                        red = colorpalette[i][j][0]; // set r value same as the colorpalette value

                    //no changes for the other two channels    
                    int green = c.getGreen();
                    int blue = c.getBlue();

                    newImage.setRGB(i, j, new Color(red, green, blue).getRGB());
                }
            }
            return new MainImage(newImage, panelWidth, panelHeight);
        }

        else if(col==2){ //adjust green
            for(int i = 0; i< width; i++){
                for(int j= 0; j< heigth; j++){
                    colorpalette[i][j][1] += change;
                    Color c = new Color(temp.getRGB(i, j));
                    int green = c.getGreen();
                    if(colorpalette[i][j][1]<0)
                        green = 0;
                    else if(colorpalette[i][j][1]>255)
                        green = 255;
                    else
                        green = colorpalette[i][j][1];
                    int red = c.getRed();
                    int blue = c.getBlue();

                    newImage.setRGB(i, j, new Color(red, green, blue).getRGB());
                }
            }
            return new MainImage(newImage, panelWidth, panelHeight);
        }

        else{ //adjust blue 
            for(int i = 0; i< width; i++){
                for(int j= 0; j< heigth; j++){
                    colorpalette[i][j][2] += change;
                    Color c = new Color(temp.getRGB(i, j));
                    int blue = c.getBlue();
                    if(colorpalette[i][j][2]<0)
                        blue = 0;
                    else if(colorpalette[i][j][2]>255)
                        blue = 255;
                    else
                        blue = colorpalette[i][j][2];
                    int green = c.getGreen();
                    int red = c.getRed();

                    newImage.setRGB(i, j, new Color(red, green, blue).getRGB());
                }
            }
            return new MainImage(newImage, panelWidth, panelHeight);
        }

    }

    //----------------Brightness------------------------//
    //change brightness by given factor
    //returns a MainImage
    public MainImage changeBrightness(MainImage mimg, float factor, float scale) {

        RescaleOp resc = new RescaleOp(scale, factor, null); //Create a Rescale operator with altered offset Value (scalefactor = 1 for keeping constant contrast)
        BufferedImage new_img = resc.filter(mimg.getImage(), null); // get filtered image by applying the Rescale Oprator on the original image
        return new MainImage(new_img, panelWidth, panelHeight); // return filtered image
    }

    //----------------contrast--------------------------//
    public MainImage changeContrast(MainImage mimg, float factor, float scale) {

        RescaleOp resc = new RescaleOp(factor, scale, null);//Create a Rescale operator with altered scalefactor (offsetvalue=0 for keeping constant brightness)
        BufferedImage new_img = resc.filter(mimg.getImage(), null);// get filtered image by applying the Rescale Oprator on the original image
        return new MainImage(new_img, panelWidth, panelHeight); // return new image
    }

    //-----------------sharpness-------------------------//
    public MainImage sharpenImage(MainImage mimg, float factor) {


        //create new 3 x 3 kernel function for changing each pixel value
        // 1 current pixel and 8 surrounding pixels 
        //normal image kernel - [0,0,0,0,1,0,0,0,0]
        //fully sharp kernel - [-1,-1,-1,-1,9,-1,-1,-1,-1]
        //calculation:
        // pixel value for sharp kernel= (9*currentpixel - (weighted sum of neighboring pixels))/9
        Kernel kernel = new Kernel(3, 3, new float[] { -1 + factor, -1 + factor, -1 + factor, -1 + factor,
                9 - factor * 8, -1 + factor, -1 + factor, -1 + factor, -1 + factor }); // total weight adjusted to 1 for mantaining the brightness of teh image
        BufferedImageOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null); // create convolve operator with prepared kernel
        BufferedImage newImg = cop.filter(mimg.getImage(), null); // apply the operator on the image
        return new MainImage(newImg, panelWidth, panelHeight); // return image 
    }

    // clear the whole editor panel , sets a new canvas with newly supplied image
    public void repaintPanel(MainImage image) {

        this.image = image; // set image to current image
        panel.removeAll(); // clear panel
        canvas = new DisplayCanvas(image); //create new canvas 
        canvas.repaint(); 
        canvas.setSelf(); // set canvas on the panel
    }
    //----------main method------------------//
    public static void main(String[] args) {
        new Editor();
    }
}