import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage; 
import javax.imageio.ImageIO; 
import javax.swing.*; 
import java.awt.event.*; 
import javax.swing.filechooser.*; 
import java.util.concurrent.*;
public class GrayScale extends JFrame {
	JLabel label;
	String path;
	BufferedImage image = null;
	public static Semaphore semaphore = new Semaphore(0);
	public static void main(String[] args) throws IOException,InterruptedException {
		GrayScale g=new GrayScale();
		g.guigo();
		g.go();
	}
	public void guigo(){
		JFrame frame=new JFrame();
		JPanel panel=new JPanel();
		JButton open=new JButton("open");
		 label=new JLabel();
		open.addActionListener(new openListener());
		panel.add(open);
		panel.add(label);
		frame.add(panel);
		frame.setSize(500,500);
		frame.setVisible(true);
	}
	public class openListener implements ActionListener
	    {
		public void actionPerformed(ActionEvent e){
			JFileChooser j = new JFileChooser(); 
            int r = j.showOpenDialog(null);
			j.setAcceptAllFileFilterUsed(false); 
			FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only jpg jpeg png files", "jpg","jpeg","png"); 
            j.addChoosableFileFilter(restrict); 			
            if (r == JFileChooser.APPROVE_OPTION) 
              { 
				File f=j.getSelectedFile();
                path=f.getAbsolutePath();
				try{
					File input_file=new File(path);
					image=ImageIO.read(input_file); 
					System.out.println("copy"); 
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
				//label.setText(path);
            } 
            else{
                label.setText("kale aavjo");
			}
			semaphore.release();
		}
		//semaphore.release();
	}
	
	public void go() throws IOException,InterruptedException {
			semaphore.acquire();
			int width=image.getWidth();
			int height=image.getHeight();
			System.out.println(width);
			System.out.println(height);
			for(int i=0;i<height;i++){
				for(int j=0;j<width;j++){
					int p = image.getRGB(j,i);
					int a = (p>>24) & 0xff;
					int r = (p>>16) & 0xff;
					int g = (p>>8) & 0xff;
					int b = p & 0xff;
					int avg=(r+g+b)/3;
					 a = avg; 
					 r = avg; 
					 g = avg; 
					 b = avg; 
					 p = (a<<24) | (r<<16) | (g<<8) | b; 
					 image.setRGB(j, i, p);
					
				}
			}
	        try
	        { 
	             
	            File output_file1 = new File(path); 
	            ImageIO.write(image, "jpg", output_file1); 
	            System.out.println("paste"); 
	        } 
	        catch(IOException e) 
	        { 
	            e.printStackTrace();
	        } 
	  
		
		
	}

}

