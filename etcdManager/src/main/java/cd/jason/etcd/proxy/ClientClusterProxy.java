/**    
 * 文件名：ClientClusterProxy.java    
 *    
 * 版本信息：    
 * 日期：2018年8月19日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.etcd.proxy;


import cd.jason.BilSrv.ClientCluster;
import cd.jason.BilSrv.ServerMonitor;
import cd.jason.BilSrv.SrvAddress;

/**    
 *     
 * 项目名称：proxy    
 * 类名称：ClientClusterProxy    
 * 类描述：   初始化etcd集群
 * 创建人：SYSTEM    
 * 创建时间：2018年8月19日 上午9:10:22    
 * 修改人：SYSTEM    
 * 修改时间：2018年8月19日 上午9:10:22    
 * 修改备注：    
 * @version     
 *     
 */
public class ClientClusterProxy {
    private static class Sington
    {
        private  static  ClientClusterProxy instance=new ClientClusterProxy();
    }
    
    /**
     * 
    * @Title: getInstance
    * @Description: 单例
    * @return    参数
    * @return ClientCluster    返回类型
     */
public static ClientClusterProxy getInstance()
{
    return Sington.instance;
}

/**
 * 
* @Title: init
* @Description: 初始化信息
* @return void    返回类型
 */
public void init()
{
    ClientCluster.getInstance().setCheckCluster(ClusterProxyConfig.ischeckCluster);
    ClientCluster.getInstance().setEtcdCheck(ClusterProxyConfig.checkTimeLen, ClusterProxyConfig.forceUpdateday);
    ClientCluster.getInstance().setEtcdNode(ClusterProxyConfig.Client_Cluster_Addr);
    ServerMonitor.getInstance().setUpdateFile(false);//代理端没有文件
    server();
}

/**
 * 
* @Title: server
* @Description: 服务信息
* @param     参数
* @return void    返回类型
 */
private void server()
{
   String[] srv= ClusterProxyConfig.srvName.split(";");
   for(int i=0;i<srv.length;i++)
   {
       String[] servers=srv[i].split(":");
       if(servers.length==1)
       {
           ServerMonitor.getInstance().setServerName(servers[0]);
       }
       else if(servers.length==2)
       {
           ServerMonitor.getInstance().setServerVision(servers[0],servers[1]); 
       }
   }
}

/**
 * 
* @Title: getRegisterAddress
* @Description: 获取注册信息
* @param @param name
* @param @param address
* @param @return    参数
* @return SrvAddress    返回类型
 */
public SrvAddress getRegisterAddress(String name,String address)
{
    return ServerMonitor.getInstance().getSrvAddress(name, ServerMonitor.SYS_VISION, address);
}

/**
 * 
* @Title: getRegisterAddress
* @Description: 获取向etcd注册的服务地址
* @param @param name 服务系统名称
* @param @param vision 版本
* @param @param address 读取需要获取的客户端
* @param @return    参数
* @return SrvAddress    返回类型
 */
public SrvAddress getRegisterAddress(String name,String vision,String address)
{
    return ServerMonitor.getInstance().getSrvAddress(name, vision, address);
}

/**
 * 
* @Title: getgetRegisterService
* @Description: 获取注册的服务方法映射
* @param @param name
* @param @return    参数
* @return String    返回类型
 */
public String getgetRegisterService(String name)
{
    return ServerMonitor.getInstance().getService(name, null);
}

/**
 * 
* @Title: getgetRegisterService
* @Description: 获取注册的服务方法映射
* @param @param name
* @param @param vision
* @param @return    参数
* @return String    返回类型
 */
public String getgetRegisterService(String name,String vision)
{
    return ServerMonitor.getInstance().getService(name, vision);
}

}
