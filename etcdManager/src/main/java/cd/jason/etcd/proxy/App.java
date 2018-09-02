package cd.jason.etcd.proxy;

import java.io.IOException;

import cd.strommq.log.LogFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        ProxyReadConfig proxtConf=new ProxyReadConfig();
        proxtConf.loadConfig();
        LogFactory.getInstance().addInfo("初始化配置服务");
        ProxyServer server=new ProxyServer();
        server.start();
        LogFactory.getInstance().addInfo("代理服务启动");
        try {
            System.in.read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        server.close();
    }
}
