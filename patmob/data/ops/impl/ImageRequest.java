package patmob.data.ops.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import patmob.data.ops.OpsRestClient;
import patmob.data.ops.OpsServiceRequest;

/**
 * This request does not extend parser, since it just displays images.
 * OPS Published-Data Services.
 * 
 * 20141117 ImageRequest crashes the OpdRestClient.
 * Remove from EquivalentsRequest
 * 
 * @author Piotr
 */
public class ImageRequest implements OpsServiceRequest {
    public static final int PNG_IMAGE  = 1,
                            PDF_IMAGE  = 2,
                            TIFF_IMAGE = 3;
    //for now we do only png
    String formatURL = ".png?Range=1";
    
    ArrayList<String> imageLinks;
    int imageFormat;
    HttpRequestBase[] requests;

    public ImageRequest(ArrayList<String> imageLinks, int imageFormat) {
        this.imageLinks = imageLinks;
        this.imageFormat = imageFormat;
    }
    
    public void submit() {
        requests = new HttpRequestBase[imageLinks.size()];
        for (int i=0; i<imageLinks.size(); i++) {
            String imageLink = imageLinks.get(i);
            HttpGet httpget = 
                    new HttpGet(OpsRestClient.OPS_URL + imageLink + formatURL);
            requests[i] = httpget;
        }
        OpsRestClient.submitServiceRequest(
                this, OpsRestClient.IMAGES_THROTTLE);
    }
    
    @Override
    public HttpRequestBase[] getRequests() {
        return requests;
    }

    @Override
    public void handleResponse(HttpResponse response) {
        if (response.getStatusLine().getStatusCode()==200) {
            HttpEntity resultEntity = response.getEntity();
            if (resultEntity!=null) {
                BufferedImage bi = null;
                try {
                    InputStream is = resultEntity.getContent();
                    bi = ImageIO.read(is);
                } catch (Exception ex) {ex.printStackTrace();}
                final JFrame f = new JFrame("Load Image Sample");
                f.addWindowListener(new WindowAdapter(){
                    @Override
                    public void windowClosing(WindowEvent e) {
                        f.dispose();
                    }
                });
                f.add(new ImageView(bi));
                f.pack();
                f.setVisible(true);
            }
        }
        else {
            //response status not OK
            System.out.println( "ImageRequest : " + response.getStatusLine());
            HttpClientUtils.closeQuietly(response);
        }
    }
    
    class ImageView extends Component {
        BufferedImage img;

        public ImageView(BufferedImage bi) {
            img = bi;
        }

        @Override
        public void paint(Graphics g) {
            g.drawImage(img, 0, 0, null);
        }

        @Override
        public Dimension getPreferredSize() {
            if (img == null) {
                 return new Dimension(100,100);
            } else {
               return new Dimension(img.getWidth(null), img.getHeight(null));
            }
        }
    }
}
