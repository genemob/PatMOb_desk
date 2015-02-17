package patmob.util;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Not needed for OPS, but PatBaseSOAP plug-in still needs it...
 * Sets the proxy for Internet access through firewall,
 * 7/1/2012 if defined in properties file.
 * @author piotr
 */
public class PatmobProxySelector extends ProxySelector {
    private HashMap<SocketAddress, Proxy> proxies =
            new HashMap<SocketAddress, Proxy>();

    public PatmobProxySelector(String proxyAddress) {
        System.out.println("PROXY ADDRESS: " + proxyAddress);
        Proxy p;
        if (proxyAddress==null) {
            p = Proxy.NO_PROXY;
            proxies.put(p.address(), p);
        } else {
            // patmobProxy=globalproxy-amer.pharma.aventis.com:3129; globalproxy-amer.pharma.aventis:3443
            try {
                StringTokenizer st = new StringTokenizer(proxyAddress,";"), st2;
                while (st.hasMoreTokens()) {
                    st2 = new StringTokenizer(st.nextToken().trim(), ":");
                    p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress
                        (st2.nextToken(), Integer.parseInt(st2.nextToken())));
                    proxies.put(p.address(), p);
                }
            } catch (Exception x) {x.printStackTrace();}
        }
    }
    
//    public PatmobProxySelector() {
//        //5-31-2012 Sanofi proxy change
////        p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress
////                    ("prx-brw-vip.net.sanofi-aventis.com", 3129));
////        proxies.put(p.address(), p);
////        p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress
////                    ("prx-res-vip.net.sanofi-aventis.com", 3129));
////        proxies.put(p.address(), p);
//        Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress
//                    ("globalproxy-amer.pharma.aventis.com", 3129));
//        proxies.put(p.address(), p);
//        //var https_proxy = "PROXY globalproxy-amer.pharma.aventis.com:3443";
//        p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress
//                    ("globalproxy-amer.pharma.aventis", 3443));
//        proxies.put(p.address(), p);
////        p = Proxy.NO_PROXY;
////        proxies.put(p.address(), p);
//}

    @Override
    public List<Proxy> select(URI uri) {
//        System.out.println("Returning proxies " + proxies.values());
        ArrayList<Proxy> l = new ArrayList<Proxy>();
        for (Proxy p : proxies.values()) {l.add(p);}
        return l;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        //is this ever called?
        proxies.remove(sa);
    }

}
