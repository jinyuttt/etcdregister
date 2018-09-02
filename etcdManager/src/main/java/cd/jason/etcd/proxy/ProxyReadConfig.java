/**    
 * 文件名：DBServerConfig.java    
 *    
 * 版本信息：    
 * 日期：2018年8月8日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.etcd.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**    
 *     
 * 项目名称：DBServer    
 * 类名称：DBServerConfig    
 * 类描述：   配置读取
 * 创建人：jinyu    
 * 创建时间：2018年8月8日 上午2:01:11    
 * 修改人：jinyu    
 * 修改时间：2018年8月8日 上午2:01:11    
 * 修改备注：    
 * @version     
 *     
 */
public class ProxyReadConfig {
public String logConfig="config/log4j2.xml";
private String confFile="config/config.properties";
public void loadConfig()
{
   File conf=new File(confFile);
   if(!conf.exists())
   {
       return;
   }
    Properties properties = new Properties();
     // 使用InPutStream流读取properties文件
    try
    {
    BufferedReader bufferedReader = new BufferedReader(new FileReader(confFile));
    properties.load(bufferedReader);
    // 获取key对应的value值
     String udpPort=properties.getProperty("udpport", "9999");
     String tcpPort=properties.getProperty("tcpPort", "9998");
     String cluster_addrress=properties.getProperty("cluster_addrress", "127.0.0.1:2379");
     String ischeckcluster=properties.getProperty("ischeckcluster", "true");
     String checkTimeLen=properties.getProperty("checkTimeLen", "3600000");
     String forceupdateday=properties.getProperty("forceupdateday", "7");
     String srvName=properties.getProperty("srvName", "XXXSrv");
     String logconf=properties.getProperty("logconfig", "config/log4j2.xml");
     this.logConfig=logconf;
     ClusterProxyConfig.checkTimeLen=Long.valueOf(checkTimeLen);
     ClusterProxyConfig.Client_Cluster_Addr=cluster_addrress;
     ClusterProxyConfig.ischeckCluster=Boolean.valueOf(ischeckcluster);
     ClusterProxyConfig.srvName=srvName;
     ClusterProxyConfig.srvUdpPort=Integer.valueOf(udpPort);
     ClusterProxyConfig.srvTcpPort=Integer.valueOf(tcpPort);
     ClusterProxyConfig.forceUpdateday=Integer.valueOf(forceupdateday);
    }
    catch(Exception ex)
    {
        ex.printStackTrace();
    }
}

}
